# Java集合框架体系
![img_162.png](img_162.png)

![img_163.png](img_163.png)

![img_164.png](img_164.png)

![img_165.png](img_165.png)

![img_166.png](img_166.png)

![img_167.png](img_167.png)

![img_168.png](img_168.png)

![img_169.png](img_169.png)

![img_170.png](img_170.png)

# List
## ArrayList
![img_171.png](img_171.png)

![img_172.png](img_172.png)

![img_173.png](img_173.png)

![img_174.png](img_174.png)

![img_175.png](img_175.png)

![img_176.png](img_176.png)

![img_177.png](img_177.png)

如何实现数组和List的转换？
![img_178.png](img_178.png)

现在有一个数组num[]
当我们使用Arrays.asList(num)方法将数组转换为List时，返回的List是一个Arrays的内部类ArrayList，而不是我们常用的java.util.ArrayList
如果我们修改num的值，那么返回的List中的值也会被修改，这是一个浅拷贝
![img_179.png](img_179.png)

对于List转换为数组，我们可以使用toArray()方法
可以设置int[] arr = new int[list.size()]，这样可以保证数组的大小和List的大小一致
然后再使用list.toArray(arr)方法将List转换为数组，这是一个深拷贝
![img_180.png](img_180.png)

![img_181.png](img_181.png)

链表
![img_182.png](img_182.png)

ArrayList和LinkedList的区别
![img_183.png](img_183.png)

![img_184.png](img_184.png)

![img_185.png](img_185.png)

# 二叉树
## 二叉树的常见分类
![img_186.png](img_186.png)

BST
![img_187.png](img_187.png)

BST正常情况下时间复杂度是O(logn)
![img_188.png](img_188.png)

BST的极端情况，退化为链表，时间复杂度是O(n)
![img_189.png](img_189.png)

![img_190.png](img_190.png)

红黑树
![img_191.png](img_191.png)

![img_192.png](img_192.png)

哈希表
哈希表的时间复杂度是O(1)，但是在极端情况下，哈希表的时间复杂度会退化为O(n)，比如哈希冲突严重时，某个哈希槽的链表长度过长
![img_193.png](img_193.png)

![img_194.png](img_194.png)

![img_195.png](img_195.png)

![img_196.png](img_196.png)

![img_197.png](img_197.png)

![img_198.png](img_198.png)

![img_199.png](img_199.png)

在Java中，`equals()`和`hashcode()`方法是定义在`Object`类中的两个方法，它们在很多情况下都需要被重写，特别是在使用集合类（如`HashSet`, `HashMap`等）时。

`equals()`方法用于比较两个对象是否相等。默认情况下，`equals()`方法比较的是两个对象的内存地址，也就是说，只有当两个引用指向同一个对象时，`equals()`方法才会返回`true`。但在实际应用中，我们通常认为两个对象的内容相同就应该视为相等，因此需要重写`equals()`方法。

`hashcode()`方法用于返回对象的哈希码，这个哈希码在哈希表中用于确定对象的存储位置。默认情况下，`hashcode()`方法返回的是对象的内存地址经过转换得到的一个值。如果两个对象的`equals()`方法返回`true`，但它们的`hashcode()`返回的值不同，那么哈希表就会认为它们是两个不同的对象，这显然是不正确的。因此，当你重写`equals()`方法时，也需要重写`hashcode()`方法，以保证相等的对象返回相同的哈希码。

这是Java的一个约定，如果违反这个约定，那么在使用Java的集合类时可能会出现错误的结果。例如，如果你将一个对象添加到`HashSet`中，然后修改这个对象使得它的`hashcode()`返回的值改变，那么你可能就无法再从`HashSet`中找到这个对象，因为`HashSet`会根据`hashcode()`的值来查找对象。

![img_200.png](img_200.png)

![img_201.png](img_201.png)

![img_202.png](img_202.png)

![img_203.png](img_203.png)
比如哈希值是19，在大小16的数组中计算出索引应该在3，如果扩容为32，索引就应该在19

![img_204.png](img_204.png)
这样回答：如果是红黑树，就走红黑树移动到新的数组的逻辑，这里面的具体就比较复杂了，没有太多去研究

![img_205.png](img_205.png)

![img_206.png](img_206.png)

![img_207.png](img_207.png)

![img_208.png](img_208.png)