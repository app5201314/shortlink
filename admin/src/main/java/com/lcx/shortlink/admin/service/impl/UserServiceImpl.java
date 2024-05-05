/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lcx.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcx.shortlink.admin.common.biz.user.UserContext;
import com.lcx.shortlink.admin.common.convention.exception.ClientException;
import com.lcx.shortlink.admin.common.convention.exception.ServiceException;
import com.lcx.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.lcx.shortlink.admin.dao.entity.UserDO;
import com.lcx.shortlink.admin.dao.mapper.UserMapper;
import com.lcx.shortlink.admin.dto.req.UserLoginReqDTO;
import com.lcx.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.lcx.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.lcx.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.lcx.shortlink.admin.dto.resp.UserRespDTO;
import com.lcx.shortlink.admin.service.GroupService;
import com.lcx.shortlink.admin.service.UserService;
import com.lcx.shortlink.admin.toolkit.HashUtil;
import com.lcx.shortlink.admin.toolkit.JwtTools;
import com.lcx.shortlink.admin.toolkit.TripleDES;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.lcx.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.lcx.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.lcx.shortlink.admin.common.enums.UserErrorCodeEnum.USER_EXIST;
import static com.lcx.shortlink.admin.common.enums.UserErrorCodeEnum.USER_NAME_EXIST;
import static com.lcx.shortlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);

        if (userDO == null) {
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }

        // 将用户信息解密
        try {
            userDO.setRealName(TripleDES.decrypt(userDO.getRealName()));
            userDO.setPhone(TripleDES.decrypt(userDO.getPhone()));
            userDO.setMail(TripleDES.decrypt(userDO.getMail()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (!hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            // 加密
            requestParam.setRealName(TripleDES.encrypt(requestParam.getRealName()));
            requestParam.setPhone(TripleDES.encrypt(requestParam.getPhone()));
            requestParam.setMail(TripleDES.encrypt(requestParam.getMail()));

            // 将密码hash后存入数据库
            requestParam.setPassword(HashUtil.hash(requestParam.getPassword()));

            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if (inserted < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            groupService.saveGroup(requestParam.getUsername(), "默认分组");
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } catch (Exception e) {
            throw new ClientException(USER_SAVE_ERROR);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        if (!Objects.equals(requestParam.getUsername(), UserContext.getUsername())) {
            throw new ClientException("当前登录用户修改请求异常");
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, HashUtil.hash(requestParam.getPassword()))
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }

        if (stringRedisTemplate.opsForValue().get(USER_LOGIN_KEY + requestParam.getUsername()) == null) {
            stringRedisTemplate.opsForValue().set(USER_LOGIN_KEY + requestParam.getUsername(), JSON.toJSONString(userDO));
        }

        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);

        String username = requestParam.getUsername();
        // 将用户名哈希后再签名
        String signature = JwtTools.genSignature(Map.of("username", HashUtil.hash(username)));

        return new UserLoginRespDTO(signature);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }
}
