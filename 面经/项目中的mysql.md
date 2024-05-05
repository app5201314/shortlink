# 该项目的表是怎么设计的？
短链接分组表设计：
t_group

用户表设计：
t_user

短链接表设计：
t_link, t_link_goto（短链接跳转表，用来存储full_short_url和gid的映射关系）

短链接访问记录表设计：（统计某天某条短链接各个维度的访问量，如地域、网络、操作系统、设备、浏览器等）
t_link_locale_stats, t_link_network_stats, t_link_os_stats, t_link_device_stats, t_link_browser_stats

短链接用户访问记录表设计：（统计某天某条短链接的访问量、访问ip数、访问uv数）
t_link_stats_today

短链接访问统计表，用来记录某个短链接的访问量、访问ip数、访问uv数，时间控制粒度可根据需要自行调整，根据日期、周、小时来划分
t_link_access_stats

用户访问短链接日志表设计：
t_link_access_logs

# 表设计的思路
## 为什么t_link表以gid来进行分表，而不是full_short_url？

如果是分组查询短链接，前端不会传full_short_url，只会传gid，所以我们需要通过gid来查询短链接信息

显然，gid能更好得满足各种查询需求，而full_short_url只能满足单一查询需求

## 为什么t_link_os_stats、t_link_browser_stats、t_link_device_stats等表要以包含日期的几个字段作为索引？
因为我们统计的是单日的数据，所以需要以日期为索引，方便查询

## 为什么t_link_access_stats表可以查询单日的数据，还要单独建一张t_link_stats_today表？
![img_158.png](img_158.png)
t_link_stats_today这张表是为了优化查询性能而设计的。

在数据库设计中，为了提高查询性能，常常会创建汇总表来存储频繁访问的数据。

在这个例子中，t_link_stats_today表是一个汇总表，它存储了每个full_short_url、gid和date的唯一组合的每日统计数据（today_pv、today_uv、today_uip）。

虽然这些信息确实可以从t_link_access_stats表中查询得到，但这可能需要在数量庞大的行上进行数据聚合。

这在计算上可能会非常昂贵和慢，特别是如果需要频繁或实时地访问这些数据。  

相比之下，使用t_link_stats_today表，每日的统计数据已经预先计算并存储在每个full_short_url、gid和date的单行中。

t_link_stats_today表、t_link_access_stats表是不用分表的，因为这两张表后面要做冷热数据分离

## 为什么要在短链接的日志表中加入del_time字段，这个字段有什么用？为什么不只用full_short_url和gid作为索引而要加入del_time字段？full_short_url, gid, del_flag这三个字段作为索引可以吗？有什么问题？
首先解答为什么会提出这个问题（问题背景），因为gid是分片键，所以当我们修改短链接的gid后，需要删除原表中的短链接记录，然后以新的gid经过hash定位到对应的表中插入修改后的短链接数据。

在这个过程中只以short_url, gid做为索引是肯定有问题的，如果用户又把gid改回来了，那短链接就会回写到原表中，但因为索引是short_url, gid，所以会插入失败

那么，以short_url, gid, del_flag这三个字段作为索引可以吗？

先说结论，也是不可以的，当用户修改gid -> 将gid改回来 -> 再次修改gid，这个时候就会出现问题，因为del_flag是0，所以会插入失败

因此，为了解决这个问题，我们需要加入del_time字段，每次删除短链接时，将del_time字段更新为当前时间，这样就可以保证short_url, gid, del_time这三个字段作为索引是可以的

# 项目中使用到了mysql的哪些特性？
使用了大量的联合索引，优化了查询性能

使用了分表，提高了查询性能

使用了事务，保证了数据的一致性

使用了冷热数据分离，提高了查询性能

# mysql的核心知识
## mysql的慢查询是如何定位的？
在mysql中开启了慢日志查询，我们设置的值就是2秒，一旦sql执行超过2秒就会记录到日志中

我还了解可以通过一些运维工具来实现慢查询的定位，比如阿里的阿尔萨斯还有skywalking等

## 那这个SQL语句执行很慢，怎么解决呢？
查看sql执行计划

第一步，可以查询这条sql的执行计划，直接在select前加上explain关键字，可以查看这条sql的执行计划
![img_39.png](img_39.png)

执行计划的关键字段：
![img_40.png](img_40.png)

type字段表示了索引的使用情况
![img_41.png](img_41.png)
NULL和SYSTEM很少用，一般都是const、eq_ref、ref、range，最少也要维持在range以前
使用eq_ref表示只会查询出一条数据
使用ref表示使用了索引，但是可能会查询出多条数据

如果查询出来是INDEX和ALL，那就说明需要优化了

![img_42.png](img_42.png)

## 索引
那么你提到索引了，什么是索引呢？
索引是一种数据结构，可以帮助我们快速定位到数据，类似于书的目录，可以快速找到书的内容

mysql的索引底层是什么数据结构呢？
mysql的索引底层是B+树

B+树的特性如下：  
所有关键字都出现在叶子节点的链表中（链表中的关键字即是有序的），同时叶子节点包含了指向记录的指针，非叶子节点中的关键字只是作为索引使用，不包含指向记录的指针。  
所有叶子节点都位于同一层，并且用双向链表维护，方便按区间查找和遍历。

为什么不用B树呢？
B树和B+树都是常用的数据结构，主要用于大量数据的存储系统如数据库和文件系统。然而，B+树相比B树有几个优点使其在某些情况下更受欢迎：

1. B+树的所有关键字都出现在叶子节点，这使得在进行大范围查询时更加高效。因为一旦找到需要的关键字范围，就可以在叶子节点链表中顺序遍历，而不需要像B树那样回溯。这在数据库中尤其重要，因为经常需要进行范围查询。

2. B+树的非叶子节点不存储数据，这意味着每个非叶子节点可以存储更多的键，从而使树的高度更低，进一步提高了查询效率。

3. B+树的叶子节点之间通过链表连接，这使得对整个数据集的全扫描变得更加高效。

4. B+树的结构更加稳定。在B树中，插入和删除可能会导致节点分裂或合并，进而引发从叶子到根的大量路径变化。而在B+树中，所有数据都在叶子节点，插入和删除只会影响叶子节点和其父节点，不会引发大量路径变化。

因此，虽然B树也有其优点，如每个节点都包含数据，可以更快地访问单个元素，但在需要大量范围查询和全扫描的数据库系统中，B+树通常是更好的选择。

![img_43.png](img_43.png)

聚簇索引和二级索引
![img_44.png](img_44.png)

![img_45.png](img_45.png)

![img_46.png](img_46.png)

![img_47.png](img_47.png)

覆盖索引（不需要进行回表查询的索引）
![img_48.png](img_48.png)

![img_49.png](img_49.png)
实际开发中，覆盖索引使用的场景并不多，我们的索引一般是由查询条件确定的，需要查询的结果往往不止条件中的几个字段，
可能还包含记录中的其他字段，所以大部分情况还是会回表查询

mysql超大分页问题的解决方案：
![img_50.png](img_50.png)
因为直接分页查会让大量数据进出内存，导致IO时间长吧，使用子查询只让id进内存，减少IO时间
![img_51.png](img_51.png)

索引的创建原则有哪些？
![img_52.png](img_52.png)
下面几点是比较重要的：
![img_53.png](img_53.png)

索引失效的情况你有遇到过吗？怎么解决的？
可以使用执行计划explain来查看索引是否生效
![img_54.png](img_54.png)
1.如果使用的是联合索引，且违反了最左前缀法则，会导致索引失效
2.范围查询右边的字段，会导致索引部分失效
3.在索引列上进行了运算操作
4.字符串不加单引号，会导致对应列的索引不生效
![img_55.png](img_55.png)
没有对字符串加单引号，MySQL的查询优化器，会自动的进行类型转换，会导致索引失效
只要索引上发生了任何的类型转换，都会导致索引失效
5.模糊查询时%在前，索引失效
![img_56.png](img_56.png)

![img_57.png](img_57.png)

项目中索引失效的情况

## SQL优化
![img_58.png](img_58.png)

表设计优化
![img_60.png](img_60.png)

SQL语句优化
![img_59.png](img_59.png)

针对第五点

![img_61.png](img_61.png)
Join优化能用innerjoin 就不用left join right join，如必须使用一定要以小表为驱动，内连接会对两个表进行优化，优先把小表放到外边，把大表放到里边。
eft join或right join，不会重新调整顺序

从架构的角度优化
![img_62.png](img_62.png)

我们项目里用到了分库分表

## 事务
事务是什么？
事务是一组操作，要么全部成功，要么全部失败

事务的特性是什么？
![img_63.png](img_63.png)
事务的一致性：
如果由于某种原因（如系统故障、程序错误等）事务无法完成，那么事务应该被回滚（撤销），数据库应该恢复到事务开始之前的状态，以保持数据的一致性。
例如，假设有一个银行转账的事务，从账户A向账户B转账100元。这个事务包含两个操作：从账户A扣除100元，和向账户B添加100元。
这两个操作必须都成功，或者都不成功。如果只执行了一个操作，就会导致数据的不一致——总金额发生了变化。
因此，如果在执行这个事务的过程中发生了错误，那么已经执行的操作必须被撤销，以保持数据的一致性。
![img_64.png](img_64.png)
介绍事务特性的时候，最好是举例子来讲，比如银行转账的例子

并发事务可能会产生哪些问题呢？
![img_65.png](img_65.png)

怎么解决并发事务的问题呢？
设置隔离级别
以下是四种隔离级别：
![img_66.png](img_66.png)
mysql默认的隔离级别是可重复读，但是在高并发的情况下，会导致幻读的问题
![img_67.png](img_67.png)

## undo日志和redo日志
![img_68.png](img_68.png)
我们在执行dml操作时，实际上是对缓冲池中的数据进行操作，然后再写入到磁盘中，
这个过程中，可能会出现异常，导致缓冲池中的数据丢失

为了解决这个问题，mysql引入了undo日志和redo日志
![img_70.png](img_70.png)

![img_69.png](img_69.png)

![img_71.png](img_71.png)

![img_72.png](img_72.png)

## MVCC实现原理
![img_73.png](img_73.png)

![img_74.png](img_74.png)

![img_75.png](img_75.png)

## mysql的主从同步原理
![img_76.png](img_76.png)

![img_77.png](img_77.png)

## 分库分表
![img_78.png](img_78.png)

垂直分库
![img_79.png](img_79.png)

垂直分表
![img_80.png](img_80.png)
这样拆分的好处是，分开后检索商品信息时，磁盘IO会减少（不用查询详情信息），提高检索速度
而且可以实现冷热数据分离

水平分库
![img_82.png](img_82.png)

水平分表
![img_81.png](img_81.png)

![img_83.png](img_83.png)

# mysql核心知识汇总
## mysql的函数
![img_377.png](img_377.png)

## 约束
![img_378.png](img_378.png)

## sql优化
![img_379.png](img_379.png)

![img_380.png](img_380.png)

![img_381.png](img_381.png)

![img_382.png](img_382.png)

![img_383.png](img_383.png)

为什么批量插入数据时要顺序插入？
因为mysql的索引是有序的，如果乱序插入，会导致页分裂，降低插入性能

主键一般要设置短小，过长会增大io开销

主键尽量选择有序的，可以减少页分裂

![img_388.png](img_388.png)

![img_385.png](img_385.png)
这里违背了最左前缀法则

![img_386.png](img_386.png)

解决方案
![img_387.png](img_387.png)

![img_389.png](img_389.png)

![img_390.png](img_390.png)

![img_391.png](img_391.png)

limit分页优化
![img_392.png](img_392.png)

count函数优化
![img_393.png](img_393.png)

![img_394.png](img_394.png)

![img_395.png](img_395.png)

update的优化

innodb ：事务 外键 行锁
![img_396.png](img_396.png)
在执行update语句时，一定要根据索引字段来作为查询条件，否则会出现行锁升级为表锁的情况

innodb对count()的优化其实是不太方便的，需要遍历整张表，而isam是直接存储了表的行数，所以isam对count()的优化是很好的
innodb中count()的优化主要考虑外部优化，比如缓存，比如使用redis缓存，每次增减数据时，都要更新缓存，即在缓存中维护一个count值

![img_397.png](img_397.png)

## 存储过程
![img_398.png](img_398.png)

![img_399.png](img_399.png)

![img_400.png](img_400.png)

![img_401.png](img_401.png)

![img_405.png](img_405.png)

![img_403.png](img_403.png)

![img_404.png](img_404.png)

![img_407.png](img_407.png)

![img_406.png](img_406.png)
局部变量的作用范围是在begin和end之间

![img_408.png](img_408.png)

![img_409.png](img_409.png)     

![img_410.png](img_410.png)

![img_411.png](img_411.png)

![img_412.png](img_412.png)

# mysql的锁
## 表锁
![img_413.png](img_413.png)

![img_414.png](img_414.png)

![img_415.png](img_415.png)

![img_416.png](img_416.png)
修改表数据时，会加上共享元数据写锁，这个锁是为了防止其他事务修改表结构

![img_417.png](img_417.png)

![img_418.png](img_418.png)

![img_419.png](img_419.png)

![img_420.png](img_420.png)
意向锁主要解决的是在innodb中的行锁和表锁的冲突问题

## 行锁
![img_421.png](img_421.png)

![img_422.png](img_422.png)

![img_423.png](img_423.png)

在提交事务后，（表、行）锁会被释放

![img_424.png](img_424.png)

在执行update语句时，一定要根据索引字段来作为查询条件，否则会出现行锁升级为表锁的情况

![img_425.png](img_425.png)

在MySQL中，如果UPDATE语句的WHERE子句中的条件不使用索引，那么MySQL无法确定具体需要更新哪些行，因此它必须扫描整个表以找到需要更新的行。在这个过程中，为了保证数据的一致性和防止其他事务在此期间修改数据，MySQL会对整个表加上表锁。

表锁会阻塞其他所有试图修改表数据的操作，直到当前的UPDATE操作完成。这样可以确保在UPDATE操作执行期间，表的数据不会被其他事务修改，从而保证了数据的一致性。

但是，这种方式的缺点是并发性能较差，因为在表锁定期间，其他所有试图修改表数据的操作都必须等待，这在高并发环境下可能会成为性能瓶颈。

因此，为了提高并发性能和减少锁冲突，我们通常建议在UPDATE操作的WHERE子句中使用索引，这样MySQL可以快速定位到需要更新的行，只锁定这些行，而不是整个表。这种行级锁定可以大大提高并发性能，因为它只阻塞访问被锁定行的其他事务，而不是整个表的所有操作。

![img_426.png](img_426.png)

![img_427.png](img_427.png)

![img_428.png](img_428.png)

## 存储引擎
![img_429.png](img_429.png)

![img_430.png](img_430.png)

![img_431.png](img_431.png)

![img_432.png](img_432.png)

![img_433.png](img_433.png)

# 事务
![img_434.png](img_434.png)

![img_435.png](img_435.png)

![img_436.png](img_436.png)

## MVCC
![img_437.png](img_437.png)

![img_438.png](img_438.png)

记录中有三个隐藏字段，分别是DB_TRX_ID、DB_ROLL_PTR、DB_ROW_ID，其中DB_ROW_ID只有在表中没有主键的时候才会有，用来唯一标识一行记录

![img_439.png](img_439.png)

![img_440.png](img_440.png)

![img_441.png](img_441.png)
