## 0. JVM, Java Virtual Machine
JVM（Java Virtual Machine，Java 虚拟机）本身不是一个操作系统进程的概念，但它通常运行在一个操作系统进程之内。
当你用 java -jar MyApp.jar 启动一个 Java 应用时，操作系统会创建一个新的进程，这个进程内部运行的就是 JVM 的具体实现（比如 HotSpot VM）。在这个进程中：
JVM 管理自己的内存空间（如堆、元空间）
JVM 可以创建多个 Java 线程，这些线程通常映射到操作系统的线程（在现代 JVM 中是 1:1 映射）。
#### JVM是一个抽象的运行环境
JVM定义了：
1. 类加载机制
2. 字节码执行引擎
3. 内存模型（堆、栈、方法区等）
4. 垃圾回收机制
5. 安全模型等

## 1、JVM 运行时内存

**答：**
JVM 运行时内存从逻辑上主要分为以下区域：
线程私有区域：
    1. 程序计数器（Program Counter Register）
    2. 虚拟机栈（Java Virtual Machine Stack）
    3. 本地方法栈（Native Method Stack）
线程共享区域：
    1. 堆（Heap）
    2. 方法区（Method Area），其中包含运行时常量池（Runtime Constant Pool）
注：这些区域在虚拟地址空间中没有固定的高低地址顺序，也不一定连续。JVM 内存模型是逻辑模型，不是物理内存映射图。

### 线程私有区域：
#### **程序计数器（Program Counter Register）**：
记录当前线程执行的字节码指令地址
#### **虚拟机栈（Java Virtual Machine Stacks）**：
存储局部变量表、操作数栈、动态链接、方法出口等信息。
#### **本地方法栈（Native Method Stacks）**：为native方法服务。
##### 1. 栈内存分配
每一个线程在创建时，JVM会为其分配一块固定大小的栈内存，可以通过-Xss调整通常为1MB。所以方法如果调用过深，可能抛出StackOverflowError; 线程数量如果过多，也有可能导致总的栈内存耗尽，导致OutOfMemoryError。
##### 2. 主流JVM（例如HotSpot）选择合并栈，虚拟机栈和本地方法栈公用一个占空间。
JNI（Java Native Interface）调用不会特别深，合并分配可以节省栈使用，简化内存管理。

### 线程共享区域：
- **堆（Heap）**：存放对象实例和数组，是GC的主要区域。
- **方法区（Method Area）**：存储类信息、常量、静态变量、即时编译器编译后的代码等。
- **运行时常量池（Runtime Constant Pool）**：方法区的一部分，存放编译期生成的各种字面量和符号引用。
##### 运行时常量池和静态变量的区别
1. 运行时常量池
每个类或接口在加载时，由 JVM 为其创建的一个常量表，用于存放编译期生成的各种字面量和符号引用。
2. 静态变量
类中用 static 修饰的字段，属于类本身而非实例，所有对象共享同一份。

## 2、64 位 JVM 中，int 的长度是多数？

**答：**
在64位JVM中，int类型的长度仍然是**4个字节（32位）**。

Java中的基本数据类型大小是固定的，不依赖于JVM的位数，不依赖于编译器和目标平台：
- byte: 1字节
- short: 2字节  
- int: 4字节
- long: 8字节
- float: 4字节
- double: 8字节
- char: 2字节
- boolean: 1字节（实际实现可能有所不同）
### Char为什么是两字节
C/C++中是一字节是因为使用ASCII； JAVA采用Unicode编码，不能使用1字节char
### Boolean为什么是一字节
- boolean: 1字节（实际实现可能有所不同, 例如：JVM为了提高访问效率，会对字段进行对齐； JVM也可能对相邻的boolean进行压缩，但一般不会跨字节压缩。即，不会把8个boolean压缩到一个字节。除非使用java.util.BitSet进行特殊优化。）
### Java没有原生的Unsigned支持，怎么看
Java为了简化语言、避免常见bug，有意设计为不支持Unsigned。但是Java提供了支持的类型和对应的方法，在Integer\Long\Byte\Short中，提供了各种无符号方法。以Integer为例:
1. 无符号比较: Integer.compareUnsigned(a,b)
2. 无符号除法：Integer.divideUnsigned(a,b)
3. 无符号取模：Integer.remainderUnsigned(a,b)
4. 无符号转字符串：Integer.toUnsignedString(a)
5. 解析无符号字符串：Integer.parseUnsignedInt("12312312312313")


## 3、新生代、老年代、永久代

**答：**
这是JVM堆内存的分代结构：

### 新生代（Young Generation）
- 存放新创建的对象
- 占堆内存的1/3左右
- 分为三个区域：
  - Eden区：新对象首先分配在这里
  - Survivor 0区和Survivor 1区：经过一次GC后存活的对象会移动到这里

### 老年代（Old Generation/Tenured Generation）
- 存放存活时间较长的对象
- 占堆内存的2/3左右
- 当对象在新生代经历多次GC后仍然存活，会被晋升到老年代（默认最大年龄是15，-XX:MaxTenuringThreshold）

### 永久代（Permanent Generation）- Java 8之前
- 存储类的元数据、常量池、静态变量等
- Java 8开始被元空间（Metaspace）替代

### $ 提前晋升到老年代的情况
1. Survivor空间不足，多余对象直接进入老年代（担保机制）
2. 大对象直接分配到老年代。
3. HotSpot实现了动态年龄判断：如果Survivor中相同年龄的所有对象大小总和大于Survivor空间的一半，则该年龄及以上的对象直接晋升。

### $ 老年代GC（Major GC / Full GC）
老年区（或Java8之前的永久代）元空间不足，会触发Full GC。FUll GC 通常会清理整个堆，部分GC算法会对堆空间碎片进行整理。在Full GC期间暂停时间较长（long STW, Stop-The-World）。

### $ Survivor区为什么分为0区和1区
1. Young GC采用Stop and Copy，对于Survivor区也一样。在GC时，其中一个Surviror的区会作为拷贝的目标区，另一个Survivor区和Eden区则会作为拷贝来源。
2. 两个区实际上是两个缓冲区，在每次GC时身份互换。
3. 注意，如果在一次Stop and Copy时，目标的Survivor区满了，剩余的内存则会直接复制到Old区。

### $ 如果young GC过程中触发了担保机制，但是old区恰好又不够装,接下来会发生什么？
1. JVM 会立即触发一次 Full GC（通常是压缩式 Full GC），试图腾出老年代空间。
2. GC 是原子性操作（在 Stop-The-World 下完成），不会出现“复制到一半”的脏状态——要么全部成功，要么回退并 Full GC。

## 4、GC的分代收集算法

**答：**
分代收集算法是基于对象生命周期的不同特点采用不同的垃圾收集算法：

### 核心思想：
1. **弱分代假说**：绝大多数对象都是朝生夕死的
2. **强分代假说**：熬过越多次垃圾收集的对象越难以消亡
3. **跨代引用假说**：跨代引用相对于同代引用仅占极少数

### 实现方式：
#### 1. **新生代收集（Minor GC）**：针对新生代，使用复制算法
将内存分为两块（如 Eden + To Survivor），每次只使用一块。GC 时，将存活对象复制到另一块，然后清空原区域。
##### 优点：
无内存碎片；
只需移动少量存活对象（因大部分已死亡）；
速度快。
##### 实现：
Eden 区满 → 触发 Minor GC；
存活对象从 Eden + From Survivor 复制到 To Survivor；
若对象太大或 Survivor 空间不足，直接晋升到老年代。
#### 2. **老年代收集（Major GC/Full GC）**：针对老年代，使用标记-清除或标记-整理算法
##### a. 标记-清除（Mark-Sweep）
步骤：
标记所有存活对象；
清除未标记的对象。
缺点：产生内存碎片，可能导致后续大对象分配失败（即使总空间足够）。
使用场景：CMS（Concurrent Mark Sweep）GC 的主要算法。
##### b. 标记-整理（Mark-Compact）
步骤：
标记存活对象；
将所有存活对象向一端移动；
清理边界外的内存。
优点：无碎片；
缺点：整理过程需 Stop-The-World，耗时较长。
使用场景：Serial Old、Parallel Old、Full GC（在 Parallel GC 中）。

## 5. 怎样通过 Java 程序来判断 JVM 是 32 位 还是 64 位？
```java
    // 1.System.getProperty() -> os.arch
    String pointerSize = System.getProperty("os.arch").contains("64") ? "64" : "32";
    // 2. Runtime类 -> getRuntime().maxMemory();
    long maxMemory = Runtime.getRuntime().maxMemory();
    if (maxMemory > Integer.MAX_VALUE) {
        System.out.println("64位JVM");
    } else {
        System.out.println("32位JVM");
    }
    // 3. Object地址长度
    Object obj = new Object();
    int hashCode = obj.hashCode();
    System.out.println("对象哈希码长度暗示JVM位数");
```

## 6. JRE、JDK、JVM 及 JIT 之间有什么不同？

**答：**

### JVM（Java Virtual Machine）
- Java虚拟机，是Java程序运行的核心环境
- 负责执行字节码
- 提供内存管理、垃圾回收等功能
- 是平台无关性的核心实现

### JRE（Java Runtime Environment）
- Java运行时环境
- 包含JVM和运行Java程序所需的核心类库
- 用于运行已编译的Java程序
- 不包含开发工具

### JDK（Java Development Kit）
- Java开发工具包
- 包含JRE的所有内容
- 额外提供编译器(javac)、调试器、文档生成工具等开发工具
- 用于Java程序的开发

### JIT（Just-In-Time Compiler）
- 即时编译器
- 是JVM的一个组件
- 将热点代码（频繁执行的代码）从字节码编译为本地机器码
- 提高程序执行效率

**关系图：**
```
JDK = JRE + 开发工具
JRE = JVM + 类库
JVM 包含 JIT 编译器
```

## 7. 堆内存空间与GC的关系

### Java堆空间：
- **作用**：存放所有对象实例和数组
- **特点**：
  - 线程共享的内存区域
  - 是GC管理的主要区域
  - 物理上可以不连续，逻辑上连续

### 堆空间结构：
```
堆内存
├── 新生代 (Young Generation)
│   ├── Eden区
│   ├── Survivor 0区
│   └── Survivor 1区
└── 老年代 (Old Generation)
```

## 8、JAVA 强引用、软引用、弱引用、虚引用

**答：**

### 强引用（Strong Reference）
```java
Object obj = new Object(); // 强引用
// 只要强引用存在，对象就不会被回收
```

### 软引用（Soft Reference）
```java
SoftReference<String> softRef = new SoftReference<>(new String("软引用"));
// 内存不足时才会被回收，适合做缓存
```

### 弱引用（Weak Reference）
```java
WeakReference<String> weakRef = new WeakReference<>(new String("弱引用"));
// 下一次GC时就会被回收
```

### 虚引用（Phantom Reference）
```java
PhantomReference<String> phantomRef = new PhantomReference<>(obj, queue);
// 无法通过虚引用来获取对象，主要用于跟踪对象被回收的活动
```

### 回收优先级：
强引用 > 软引用 > 弱引用 > 虚引用

## 9. 虚拟机栈(线程私有)

**答：**
虚拟机栈是线程私有的内存区域，生命周期与线程相同。

### 主要特点：
- **存储内容**：每个方法执行时创建栈帧，存储局部变量表、操作数栈、动态链接、方法出口等
- **栈帧结构**：
  ```
  栈帧1 ← 栈顶
  栈帧2
  栈帧3
  ```

### 局部变量表：
- 存放方法参数和局部变量
- 以slot为单位存储（long和double占2个slot）
- 编译期确定大小

### 异常情况：
- **StackOverflowError**：栈深度超过限制
- **OutOfMemoryError**：栈扩展时无法申请到足够内存

### 参数设置：
```
-Xss128k  # 设置每个线程的栈大小
```

## 10. System.gc()会发生什么
一句话来说，建议进行Full GC。
具体实现取决于具体配置：
1. 是否禁用了显式GC（未设置-XX:+DisableExplicitGC会触发Full GC）
2. JVM可以根据当前负载、内存状态决定是否忽略请求。（虽然HotSpot几乎从不忽略）
3. 需要确保GC收集器支持显示GC，才可能执行GC。
*4. 如果必须触发GC，可以使用JVM工具`jcmd <pid> GC.run`

## 11. JVM加载class文件的原理机制

**答：**
JVM类加载机制遵循"双亲委派模型"：

### 类加载过程：
1. **加载（Loading）**
   - 通过类的全限定名获取定义此类的二进制字节流
   - 将字节流所代表的静态存储结构转化为方法区的运行时数据结构
   - 在内存中生成代表这个类的java.lang.Class对象

2. **验证（Verification）**
   - 文件格式验证
   - 元数据验证
   - 字节码验证
   - 符号引用验证

3. **准备（Preparation）**
   - 为类变量分配内存并设置初始值
   - 不包括实例变量

4. **解析（Resolution）**
   - 将常量池内的符号引用替换为直接引用

5. **初始化（Initialization）**
   - 执行类构造器<clinit>()方法
   - 按照代码顺序执行static变量赋值和static块

### 双亲委派模型：
1. 当一个类加载器收到类加载请求时，先委托给父加载器去加载；
2. 只有当父加载器无法完成加载（即在其搜索范围内找不到该类）时，子加载器才尝试自己加载；
3. 最终若所有父加载器都无法加载，则抛出 ClassNotFoundException。

### 优势：
- 避免类的重复加载（保证同一个类只被同一个Loader加载）
- 保证Java核心库的安全性

### 触发类加载的时机（初始化时机）
1. JVM 规范规定，只有在以下 主动使用 情况下才会触发类的初始化（从而触发加载）：
2. 创建类的实例（new）；
3. 访问类的静态字段（除 final 常量）；
4. 调用类的静态方法；
5. 使用反射（如 Class.forName()）；
6. 初始化一个类的子类（会先初始化父类）；
7. 启动类（包含 main 方法的类）。

## 12、Serial Old 收集器（单线程标记整理算法）

**答：**
Serial Old是Serial收集器的老年代版本：

### 特点：
- **单线程**：只有一个GC线程工作
- **标记-整理算法**：解决内存碎片问题
- **Stop-The-World**：执行时会暂停所有用户线程

### 适用场景：
- Client模式下的默认老年代收集器
- 作为CMS收集器的后备预案
- 小内存应用

### 配置参数：
```
-XX:+UseSerialGC  # 新生代和老年代都使用串行收集器
```

### 与其他收集器对比：
| 收集器 | 线程数 | 算法 | 适用场景 |
|--------|--------|------|----------|
| Serial Old | 单线程 | 标记-整理 | Client模式 |
| Parallel Old | 多线程 | 标记-整理 | Server模式，注重吞吐量 |
| CMS(Concurrent Mark-Sweep) | 多线程 | 标记-清除 | 注重响应时间 |

## 13、G1 收集器

**答：**
G1（Garbage-First）是面向服务端应用的垃圾收集器：

### 设计目标：
- 可预测的停顿时间模型(每一个线程GC收集固定范围的垃圾，STW时间可控。)
- 避免在整个Java堆进行全区域的垃圾收集

### 核心特点：
1. **Region分区**
   - 将整个堆划分为多个大小相等的独立区域（Region）
   - 每个Region都可以是Eden、Survivor或Old区域

2. **优先级回收**
   - 跟踪各个Region里的垃圾堆积价值大小
   - 优先回收价值最大的Region（Garbage First）

3. **并发与并行**
   - 并发：与用户线程同时工作
   - 并行：多线程并行处理

### 工作过程：
1. **初始标记**（STW）
2. **并发标记**
3. **最终标记**（STW）
4. **筛选回收**（STW）

### 优势：
- 可预测的停顿时间
- 不会产生内存碎片
- 可以设置期望的GC停顿时间

### 配置参数：
```
-XX:+UseG1GC              # 启用G1收集器
-XX:MaxGCPauseMillis=200   # 最大GC停顿时间
-XX:G1HeapRegionSize=16m   # Region大小
```

## 14、Parallel Old 收集器（多线程标记整理算法）

**答：**
Parallel Old是Parallel Scavenge收集器的老年代版本：

### 特点：
- **多线程并行**：多个GC线程同时工作
- **标记-整理算法**：避免内存碎片
- **注重吞吐量**：适合后台计算服务

### 适用场景：
- 注重吞吐量的应用
- 多核CPU服务器
- 大内存应用

### 配置参数：
```
-XX:+UseParallelGC      # 新生代使用Parallel Scavenge
-XX:+UseParallelOldGC    # 老年代使用Parallel Old
-XX:ParallelGCThreads=8  # GC线程数
```

### G1(Garbage-First)和Parallel Old都是并行， 他们有什么区别
1. 目标不同
Parallel Old：高吞吐量（适合后台计算）
G1：低且可预测的停顿时间（适合 Web/交互式应用）
2. 内存布局
Parallel Old：传统分代（连续老年代）
G1：堆划分为 Region，逻辑分代、物理不分代
3. 老年代回收方式
Parallel Old：Full GC + STW + 压缩（停顿长）
G1：Mixed GC，只回收部分老年代 Region（停顿短）
4. 并发能力
Parallel Old：无并发，全部 STW
G1：有并发标记阶段，减少停顿
5. 适用场景
Parallel Old：中小堆、吞吐优先
G1：大堆（>6GB）、延迟敏感

## 15、垃圾收集算法归纳

**答：**
JVM主要有四种垃圾收集算法：

### 1. 标记-清除算法（Mark-Sweep）
**过程：**
1. 标记所有需要回收的对象
2. 统一回收被标记的对象

**缺点：**
- 效率不高
- 产生大量内存碎片

### 2. 复制算法（Copying）
**过程：**
- 将内存分为两块
- 每次只使用其中一块
- GC时将存活对象复制到另一块

**优点：**
- 实现简单
- 运行高效
- 无碎片问题

**缺点：**
- 内存利用率低

### 3. 标记-整理算法（Mark-Compact）
**过程：**
1. 标记所有存活对象
2. 将存活对象向一端移动
3. 清理边界以外的内存

**优点：**
- 无碎片问题
- 内存利用率高

**缺点：**
- 移动对象开销大

### 4. 分代收集算法（Generational Collection）
**核心思想：**
- 根据对象存活周期不同，将内存划分为几块
- 新生代采用复制算法
- 老年代采用标记-清除或标记-整理算法

