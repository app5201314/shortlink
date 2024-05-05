# JVM概述
![img_311.png](img_311.png)

![img_312.png](img_312.png)

![img_313.png](img_313.png)

# java堆
![img_314.png](img_314.png)

![img_315.png](img_315.png)

![img_316.png](img_316.png)

![img_317.png](img_317.png)

![img_318.png](img_318.png)
方法内的局部变量一定是线程安全的吗？
不一定，必须要进行逃逸分析，看一下有没有外星引用和外星方法调用，如果没有，那么就是线程安全的。

逃逸分析（Escape Analysis）是一种编译器优化技术，主要用于确定程序中的对象的动态作用域。通过逃逸分析，编译器可以确定一个对象是否只能被一个线程访问（不逃逸），还是可能被多个线程访问（逃逸）。

如果一个对象只在创建它的线程中使用，并且永远不会逃逸出这个线程，那么这个对象就可以被认为是线程私有的。这样的对象可以在栈上分配内存，而不是在堆上。这种优化可以减少垃圾收集的压力，因为栈上的对象在方法返回时就会被自动清理，不需要垃圾收集器介入。

另外，如果编译器通过逃逸分析确定一个对象的所有引用都不会逃逸出当前方法，那么这个对象就可以被认为是方法私有的。这样的对象可以进行标量替换，即将对象的字段直接嵌入到方法中，而不是通过引用访问。这种优化可以减少内存访问的开销，提高程序的运行效率。

在Java中，HotSpot虚拟机从Java 6开始支持逃逸分析。你可以通过JVM参数`-XX:+DoEscapeAnalysis`开启这个功能。

![img_319.png](img_319.png)

![img_320.png](img_320.png)

# 方法区
![img_321.png](img_321.png)

![img_322.png](img_322.png)

![img_323.png](img_323.png)

![img_324.png](img_324.png)

# 直接内存
![img_325.png](img_325.png)

![img_326.png](img_326.png)

![img_327.png](img_327.png)

![img_328.png](img_328.png)

![img_329.png](img_329.png)

# 类加载器
![img_330.png](img_330.png)

![img_331.png](img_331.png)

![img_332.png](img_332.png)

![img_333.png](img_333.png)

![img_334.png](img_334.png)

# 类装载的执行过程
![img_335.png](img_335.png)

![img_336.png](img_336.png)

![img_337.png](img_337.png)

父静子静，父代父构，子代子构

![img_338.png](img_338.png)
子类调用父类的静态变量，只会初始化父类，子类不会初始化

![img_339.png](img_339.png)

# 垃圾回收
![img_340.png](img_340.png)

![img_341.png](img_341.png)

![img_342.png](img_342.png)

![img_343.png](img_343.png)

![img_344.png](img_344.png)

# JVM垃圾回收算法有哪些？
![img_345.png](img_345.png)

![img_346.png](img_346.png)

![img_347.png](img_347.png)

![img_348.png](img_348.png)

# JVM的分代回收算法
![img_349.png](img_349.png)

![img_350.png](img_350.png)

![img_351.png](img_351.png)

![img_352.png](img_352.png)

# 垃圾回收器
![img_353.png](img_353.png)

![img_354.png](img_354.png)

![img_355.png](img_355.png)

![img_356.png](img_356.png)

![img_357.png](img_357.png)

# G1垃圾回收器
![img_358.png](img_358.png)

![img_359.png](img_359.png)

![img_360.png](img_360.png)

![img_361.png](img_361.png)

将伊甸园区和幸存者区中的对象存入一个新的幸存者区中，然后将到达年龄界限的对象存入老年代中，释放掉选取的老年代和新生代的空间，然后再进行下一次的垃圾回收。
![img_362.png](img_362.png)

![img_363.png](img_363.png) 

在g1的垃圾回收算法中，新生代对象的容量是限定在5%~6%之间的，所以超过这个区间就会触发新生代的垃圾回收，然后将存活的对象存入幸存者区

# 几种引用的区别
![img_364.png](img_364.png)

![img_365.png](img_365.png)

![img_366.png](img_366.png)

![img_367.png](img_367.png)

# JVM调优
![img_368.png](img_368.png)

![img_369.png](img_369.png)

![img_370.png](img_370.png)

![img_371.png](img_371.png)

![img_372.png](img_372.png)

![img_373.png](img_373.png)

# JVM调优的工具
![img_374.png](img_374.png)