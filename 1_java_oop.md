## 1. Java 的类型系统在表达式运算中遵循类型提升（type promotion）规则，并且对于可能导致精度丢失或范围缩小的赋值操作，要求显式类型转换（强制转换）.
### 1. short s1 = 1; s1 = s1 + 1;有错吗? short s1 = 1; s1 += 1; 有错吗？

**答案：**
- `s1 = s1 + 1;` 有错误
- `s1 += 1;` 没有错误

**解释：**
```java
short s1 = 1;
s1 = s1 + 1;  // 编译错误：需要强制类型转换
s1 += 1;      // 正确：复合赋值运算符会自动进行类型转换
```

`s1 + 1` 的结果是 int 类型，不能直接赋值给 short 类型变量，需要强制转换：
```java
s1 = (short)(s1 + 1);  // 正确写法
```

## 2. 重载和重写的区别

| 特征 | 重载(Overloading) | 重写(Overriding) |
|------|------------------|------------------|
| 发生位置 | 同一个类中 | 子类与父类之间 |
| 方法名 | 相同 | 相同 |
| 参数列表 | 必须不同 | 必须相同 |
| 返回类型 | 可以不同 | 不能缩小返回类型范围 |
| 访问修饰符 | 无要求 | 不能降低访问权限 |
| 异常 | 可以不同 | 不能抛出更多或更宽泛异常 |
| 绑定 | 编译时绑定(静态绑定) | 运行时绑定(动态绑定) |

## 3. 数组实例化有几种方式？

**三种方式：**

```java
// 1. 声明并初始化
int[] arr1 = {1, 2, 3, 4, 5};

// 2. 先声明后初始化
int[] arr2;
arr2 = new int[]{1, 2, 3, 4, 5};

// 3. 指定长度初始化
int[] arr3 = new int[5];  // 默认值为0
```


## 5. Object 类常用方法有哪些？

```java
public class Object {
    public native int hashCode();           // 返回对象哈希码
    public boolean equals(Object obj);      // 判断对象相等
    protected native Object clone();        // 创建并返回对象副本（浅拷贝）
    public String toString();               // 返回对象字符串表示
    public final Class<?> getClass();       // 返回运行时类
    protected void finalize();              // 垃圾回收前调用(已废弃)
    public final void notify();             // 唤醒等待线程
    public final void notifyAll();          // 唤醒所有等待线程
    public final void wait();               // 当前线程等待
    public final void wait(long timeout);   // 带超时等待
    public final void wait(long timeout, int nanos); // 精确等待
}
```

注：在 Java 中，native 是一个方法修饰符，用于声明一个方法是由非 Java 语言（通常是 C 或 C++）实现的本地方法（Native Method）。这类方法的实现不在 Java 代码中，而是通过 Java Native Interface（JNI） 在底层平台（如操作系统或硬件）上完成。

### 6. notify, notifyAll 和 wait 的示例

notify，notifyAll用于唤醒一个或所有在该对象上等待的线程。
必须在同步块或同步方法中调用

### 示例 1: 使用 `wait()` 和 `notify()`
```java
class SharedResource {
    private boolean isAvailable = false;

    public synchronized void produce() {
        while (isAvailable) {
            try {
                wait(); // 等待消费者消费
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("生产者生产了一个资源");
        isAvailable = true;
        notify(); // 唤醒等待的消费者线程
    }

    public synchronized void consume() {
        while (!isAvailable) {
            try {
                wait(); // 等待生产者生产
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("消费者消费了一个资源");
        isAvailable = false;
        notify(); // 唤醒等待的生产者线程
    }
}

public class WaitNotifyExample {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();

        Thread producer = new Thread(resource::produce);
        Thread consumer = new Thread(resource::consume);

        producer.start();
        consumer.start();
    }
}
```

### 示例 2: 使用 `wait()` 和 `notifyAll()`
```java
class SharedQueue {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity = 5;

    public synchronized void produce(int value) {
        while (queue.size() == capacity) {
            try {
                wait(); // 等待消费者消费
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        queue.add(value);
        System.out.println("生产者生产: " + value);
        notifyAll(); // 唤醒所有等待的线程
    }

    public synchronized void consume() {
        while (queue.isEmpty()) {
            try {
                wait(); // 等待生产者生产
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        int value = queue.poll();
        System.out.println("消费者消费: " + value);
        notifyAll(); // 唤醒所有等待的线程
    }
}

public class NotifyAllExample {
    public static void main(String[] args) {
        SharedQueue sharedQueue = new SharedQueue();

        Thread producer1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                sharedQueue.produce(i);
            }
        });

        Thread producer2 = new Thread(() -> {
            for (int i = 10; i < 20; i++) {
                sharedQueue.produce(i);
            }
        });

        Thread consumer1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                sharedQueue.consume();
            }
        });

        Thread consumer2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                sharedQueue.consume();
            }
        });

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
    }
}
```

### 解释
1. **`wait()`**:
   - 当前线程进入等待状态，直到被其他线程通过 `notify()` 或 `notifyAll()` 唤醒。
   - 必须在同步块或同步方法中调用，否则会抛出 `IllegalMonitorStateException`。

2. **`notify()`**:
   - 唤醒一个正在等待该对象监视器的线程。
   - 被唤醒的线程需要重新获得锁，才能继续执行。

3. **`notifyAll()`**:
   - 唤醒所有正在等待该对象监视器的线程。
   - 被唤醒的线程需要竞争锁，只有一个线程能成功获得锁并继续执行。

## 6. java 中是值传递还是引用传递？

**Java只有值传递，没有引用传递。**

```java
public class Test {
    public static void main(String[] args) {
        int x = 10;
        changeValue(x);  // 传递的是x的值的副本
        System.out.println(x);  // 输出：10
        
        StringBuilder sb = new StringBuilder("Hello");
        changeReference(sb);  // 传递的是sb引用的副本
        System.out.println(sb.toString());  // 输出：Hello World
    }
    
    static void changeValue(int value) {
        value = 20;  // 修改的是副本，不影响原变量
    }
    
    // 形参是sb的复制值，指向同一个对象，可以通过形参修改对象内容
    // 但是，修改对象引用本身不会影响main中的对象引用
    static void changeReference(StringBuilder sb) {
        sb.append(" World");  // 通过引用副本修改原对象
        sb = new StringBuilder("New");  // 这个赋值不影响main中的sb
    }
}
```

## 7. 形参与实参区别

| 特征 | 形参(Formal Parameter) | 实参(Actual Parameter) |
|------|----------------------|----------------------|
| 定义位置 | 方法声明中 | 方法调用中 |
| 作用时间 | 方法执行期间 | 方法调用时 |
| 内存分配 | 栈内存 | 可能是栈也可能是堆 |
| 示例 | `void test(int a)` 中的a | `test(5)` 中的5 |

## 8. 构造方法能不能重写？能不能重载？

- **重写：不能** - 构造方法不能被继承，所以不能重写
- **重载：能** - 同一个类中可以有多个构造方法

```java
public class Student {
    private String name;
    private int age;
    
    // 构造方法重载
    public Student() {}  // 无参构造
    
    public Student(String name) {
        this.name = name;
    }
    
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

### 子类调用父类的构造方法

子类不能重写父类的构造方法，但可以通过 `super` 关键字调用父类的构造方法。

```java
class Parent {
    private String name;

    public Parent(String name) {
        this.name = name;
        System.out.println("父类构造方法被调用，name: " + name);
    }
}

class Child extends Parent {
    private int age;

    public Child(String name, int age) {
        super(name); // 调用父类的构造方法
        this.age = age;
        System.out.println("子类构造方法被调用，age: " + age);
    }
}

public class ConstructorExample {
    public static void main(String[] args) {
        Child child = new Child("Alice", 10);
    }
}
```

## 9. 内部类与静态内部类的区别

| 特征 | 成员内部类 | 静态内部类 |
|------|------------|------------|
| 声明 | `class Inner{}` | `static class Inner{}` |
| 访问外部类成员 | 可以访问所有成员 | 只能访问静态成员 |
| 创建实例 | 需要外部类实例 | 不需要外部类实例 |
| 内存关系 | 与外部类实例关联 | 独立存在 |

```java
public class Outer {
    private int x = 10;
    private static int y = 20;
    
    // 成员内部类
    class Inner1 {
        void method() {
            System.out.println(x);  // 可以访问
            System.out.println(y);  // 可以访问
        }
    }
    
    // 静态内部类
    static class Inner2 {
        void method() {
            // System.out.println(x);  // 错误：不能访问非静态成员
            System.out.println(y);     // 可以访问静态成员
        }
    }
    
    public static void main(String[] args) {
        // 创建成员内部类实例
        Outer outer = new Outer();
        Outer.Inner1 inner1 = outer.new Inner1();
        
        // 创建静态内部类实例
        Outer.Inner2 inner2 = new Outer.Inner2();
    }
}
```

## 10. Static 关键字有什么作用？

**主要作用：**

1. **修饰变量** - 类变量，所有实例共享
2. **修饰方法** - 类方法，可通过类名直接调用
3. **修饰代码块** - 静态代码块，类加载时执行
4. **修饰内部类** - 静态内部类
5. **导入静态成员** - `import static`

```java
public class Student {
    private static int count = 0;  // 静态变量
    
    static {  // 静态代码块
        System.out.println("类加载时执行");
    }
    
    public static void showCount() {  // 静态方法
        System.out.println("学生总数：" + count);
    }
}

// 使用
Student.showCount();  // 无需创建实例
```

## 11. final 在 java 中的作用，有哪些用法?

**三个用途：**

1. **修饰变量** - 常量，只能赋值一次
2. **修饰方法** - 不能被重写
3. **修饰类** - 不能被继承

```java
public final class Constants {
    public static final double PI = 3.14159;  // 常量
    
    public final void display() {  // 不能被重写
        System.out.println("显示信息");
    }
}

// public class SubClass extends Constants {}  // 错误：不能继承final类
```

## 12. String str="aaa",与 String str=new String("aaa")一样吗？

**不一样：**
所有的new对象都是堆内存；字符串常量池中存储着字符串对象，字符串对象在字符串常量池中只存在一个。

```java
String str1 = "aaa";           // 字符串常量池
String str2 = new String("aaa");  // 堆内存
String str3 = "aaa";           // 复用常量池对象

System.out.println(str1 == str3);  // true - 同一个对象
System.out.println(str1 == str2);  // false - 不同对象
System.out.println(str1.equals(str2));  // true - 内容相同
```

## 13. 讲下 java 中的 math 类有那些常用方法？

```java
public class MathDemo {
    public static void main(String[] args) {
        // 基本数学运算
        System.out.println(Math.abs(-5));      // 5 - 绝对值
        System.out.println(Math.max(3, 7));    // 7 - 最大值
        System.out.println(Math.min(3, 7));    // 3 - 最小值
        
        // 幂运算
        System.out.println(Math.pow(2, 3));    // 8.0 - 2的3次方
        System.out.println(Math.sqrt(16));     // 4.0 - 平方根
        
        // 三角函数
        System.out.println(Math.sin(Math.PI/2));  // 1.0
        System.out.println(Math.cos(0));       // 1.0
        
        // 取整
        System.out.println(Math.ceil(3.2));    // 4.0 - 向上取整
        System.out.println(Math.floor(3.8));   // 3.0 - 向下取整
        System.out.println(Math.round(3.5));   // 4 - 四舍五入
        
        // 随机数
        System.out.println(Math.random());     // 0.0-1.0随机数
    }
}
```

## 14. Char 类型能不能转成 int 类型？能不能转化成 string 类型，能不能转成 double 类型

**都可以转换：**

```java
char c = 'A';

// 转换为int - 获取ASCII码值
int i1 = c;           // 65
int i2 = (int) c;     // 65

// 转换为String
String s1 = String.valueOf(c);  // "A"
String s2 = Character.toString(c);  // "A"
String s3 = c + "";   // "A"

// 转换为double
double d1 = c;        // 65.0
double d2 = (double) c;  // 65.0
```

## 15. 什么是拆装箱？

**自动装箱和拆箱：**

```java
// 自动装箱 - 基本类型 → 包装类型
Integer obj1 = 100;        // int → Integer
Double obj2 = 3.14;        // double → Double
Boolean obj3 = true;       // boolean → Boolean

// 自动拆箱 - 包装类型 → 基本类型
int val1 = obj1;           // Integer → int
double val2 = obj2;        // Double → double
boolean val3 = obj3;       // Boolean → boolean

// 手动装箱拆箱
Integer obj4 = Integer.valueOf(200);  // 手动装箱
int val4 = obj4.intValue();           // 手动拆箱
```

## 16. Java 中的包装类都是那些？

| 基本类型 | 包装类 | 继承关系 |
|----------|--------|----------|
| byte | Byte | Number |
| short | Short | Number |
| int | Integer | Number |
| long | Long | Number |
| float | Float | Number |
| double | Double | Number |
| char | Character | Object |
| boolean | Boolean | Object |

## 17. 一个 java 类中包含那些内容？

```java
public class Student {  // 类声明
    
    // 1. 成员变量
    private String name;
    private int age;
    public static int count = 0;
    
    // 2. 构造方法
    public Student() {}
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    // 3. 成员方法
    public void study() {
        System.out.println(name + "正在学习");
    }
    
    // 4. getter/setter方法
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // 5. 静态方法
    public static void showCount() {
        System.out.println("总学生数：" + count);
    }
    
    // 6. 代码块
    {  // 实例代码块，在每次创建对象时执行，且在构造方法之前执行。
        count++;
    }
    
    static {  // 静态代码块，在类加载时执行一次，且只执行一次。
        System.out.println("类加载");
    }
    
    // 7. 内部类
    class Course {
        private String courseName;
    }
}
```

### java中为什么不需要析构函数
1. Java 虚拟机（JVM）会自动跟踪对象的引用关系。
当一个对象不再被任何活动引用所指向时，它就成为“不可达”的，JVM 的垃圾回收器会在适当的时候回收该对象占用的内存。
因此，开发者无需手动释放内存，也就不需要像 C++ 那样的析构函数来清理资源。
2. Java 曾提供 Object.finalize() 方法，作为“类析构函数”，但：
执行时间不确定；
性能差；
容易引发安全和稳定性问题；
从 Java 9 开始被标记为 deprecated，Java 18+ 已移除。

## 18. 那针对浮点型数据运算出现的误差的问题，你怎么解决？

**解决方案：**

```java
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class FloatPrecision {
    public static void main(String[] args) {
        // 问题演示
        System.out.println(0.1 + 0.2);  // 0.30000000000000004
        
        // 解决方案1：BigDecimal
        // BigDecimal 能够解决浮点数误差问题，根本原因在于它不使用二进制浮点表示，而是采用十进制的任意精度整数运算来模拟小数，从而避免了二进制浮点数（如 float 和 double）在表示十进制小数时产生的舍入误差。
        BigDecimal bd1 = new BigDecimal("0.1");
        BigDecimal bd2 = new BigDecimal("0.2");
        BigDecimal result = bd1.add(bd2);
        System.out.println(result);  // 0.3
        
        // 解决方案2：格式化输出
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println(df.format(0.1 + 0.2));  // 0.3
        
        // 解决方案3：整数运算
        int price1 = 10;  // 0.1元用分表示
        int price2 = 20;  // 0.2元用分表示
        System.out.println((price1 + price2) / 100.0);  // 0.3
    }
}
```

## 19. 访问修饰符 public,private,protected,以及不写（默认） 时的区别？

| 修饰符 | 同一类 | 同包 | 子类 | 不同包 |
|--------|--------|------|------|--------|
| public | ✓ | ✓ | ✓ | ✓ |
| protected | ✓ | ✓ | ✓ | ✗ |
| 默认(default) | ✓ | ✓ | ✗ | ✗ |
| private | ✓ | ✗ | ✗ | ✗ |

## 20. 接口有什么特点？

**接口特点：**

```java
public interface Animal {
    // 1. 常量（默认，并且必须为public static final）
    int LEGS = 4;
    
    // 2. 抽象方法（默认public abstract）
    void eat();
    void sleep();
    
    // 3. 默认方法（Java 8+）
    default void move() {
        System.out.println("动物会移动");
    }
    
    // 4. 静态方法（Java 8+）
    static void info() {
        System.out.println("这是动物接口");
    }
    
    // 5. 私有方法（Java 9+）
    private void helper() {
        System.out.println("辅助方法");
    }
}
```

## 21. 抽象类和接口的区别?

| 特征 | 抽象类 | 接口 |
|------|--------|------|
| 关键字 | abstract class | interface |
| 继承 | 单继承 | 多实现 |
| 构造方法 | 有 | 无 |
| 成员变量 | 任意访问修饰符 | 只能是public static final |
| 方法 | 抽象/具体方法共存 | 默认抽象（Java 8后可有默认方法） |
| 设计理念 | "is-a"关系 | "like-a"关系 |


## 22. Hashcode 的作用

**主要作用：**

1. **快速定位** - 在哈希表中快速找到存储位置
2. **提高效率** - 减少equals比较次数
3. **唯一标识** - 对象的数字标识

```java
public class Person {
    private String name;
    private int age;
    
    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return age == person.age && Objects.equals(name, person.name);
    }
}
```

## 23. 深拷贝和浅拷贝的区别是什么?

```java
class Student implements Cloneable {
    private String name;
    private Address address;  // 引用类型
    
    // 浅拷贝
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();  // 只复制引用，不复制对象
    }
    
    // 深拷贝
    public Student deepClone() throws CloneNotSupportedException {
        Student student = (Student) super.clone();
        student.address = (Address) address.clone();  // 递归克隆
        return student;
    }
}

class Address implements Cloneable {
    private String city;
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
```

**区别对比：**
- **浅拷贝**：基本类型复制值，引用类型复制引用地址
- **深拷贝**：基本类型复制值，引用类型也创建新对象

## 24. JDBC 操作的步骤

Java Data Base Connection

```java
import java.sql.*;

public class JDBCDemo {
    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // 1. 加载驱动
            // dynamically loads the specified class (com.mysql.cj.jdbc.Driver) into the JVM at runtime.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. 建立连接
            String url = "jdbc:mysql://localhost:3306/test";
            String username = "root";
            String password = "password";
            conn = DriverManager.getConnection(url, username, password);
            
            // 3. 创建Statement
            String sql = "SELECT * FROM users WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, 1);
            
            // 4. 执行SQL
            rs = pstmt.executeQuery();
            
            // 5. 处理结果
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 6. 关闭资源
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

## 25. 什么时候用 assert

**assert用于程序调试阶段：**

```java
public class AssertDemo {
    public static void main(String[] args) {
        int x = -5;
        
        // 断言x必须为正数
        assert x > 0 : "x必须大于0";
        
        System.out.println("程序继续执行");
    }
}

// 运行时需要启用断言：
// -ea or --enableassertions
// java -ea AssertDemo
```

## 26. 数组有没有 length()这个方法? String 有没有 length()这个方法

- **数组**：有 `length` 属性，没有 `length()` 方法
- **String**：有 `length()` 方法，没有 `length` 属性

```java
int[] arr = {1, 2, 3, 4, 5};
String str = "Hello";

System.out.println(arr.length);     // 5 - 数组属性
System.out.println(str.length());   // 5 - 字符串方法
```

## 27. 用最有效率的方法算出 2 乘以 8 等于几？

**使用位运算：**

```java
int result = 2 << 3;  // 左移3位相当于乘以2³=8
System.out.println(result);  // 16

// 原理：2 × 8 = 2 × 2³ = 2 << 3
```

## 28. String 和 StringBuilder、StringBuffer 的区别？

| 特征 | String | StringBuilder | StringBuffer |
|------|--------|---------------|--------------|
| 可变性 | 不可变 | 可变 | 可变 |
| 线程安全 | 安全 | 不安全 | 安全 |
| 性能 | 低(产生新对象) | 高 | 中等 |
| 适用场景 | 少量字符串操作 | 单线程大量操作 | 多线程环境 |

```java
// String - 每次操作都创建新对象
String str = "Hello";
str += " World";  // 创建新的String对象

// StringBuilder - 高效的字符串拼接
StringBuilder sb = new StringBuilder("Hello");
sb.append(" World");  // 在原对象上修改

// StringBuffer - 线程安全的字符串拼接
StringBuffer sbf = new StringBuffer("Hello");
sbf.append(" World");
```

## 29. 接口是否可继承（extends）接口？抽象类是否可实现（implements）接口？抽象类是否可继承具体类（concrete class）？

**都可以：**

```java
// 1. 接口继承接口
interface A {
    void methodA();
}

interface B extends A {  // 接口可以继承接口
    void methodB();
}

// 2. 抽象类实现接口
abstract class AbstractClass implements A {
    public void methodA() {  // 实现接口方法
        System.out.println("实现methodA");
    }
    
    abstract void abstractMethod();  // 抽象方法
}

// 3. 抽象类继承具体类
// 注意：抽象类可以没有任何抽象方法
class ConcreteClass {
    void concreteMethod() {
        System.out.println("具体方法");
    }
}

abstract class SubAbstractClass extends ConcreteClass {
    abstract void subMethod();
}
```

## 30. 一个".java"源文件中是否可以包含多个类（不是内部类）？有什么限制？

**可以包含多个类，但有限制：**

```java
// File: MyClass.java
public class MyClass {  // 只能有一个public类
    // 主类
}

class AnotherClass {  // 包访问权限类
    // 其他类
}

class ThirdClass {  // 可以有多个非public类
    // 第三个类
}
```

**限制条件：**
1. 最多只能有一个public类
2. public类名必须与文件名相同
3. 其他类只能是包访问权限(default)
    -> Private没有意义
    -> Protect允许了其他包的子类可以访问

## 31、怎么比较两个字符串的值一样，怎么比较两个字符串是否同一对象？

**答：**

**比较字符串值是否相同：**
- 使用 `equals()` 方法：比较字符串内容
- 使用 `equalsIgnoreCase()` 方法：忽略大小写比较

**比较是否为同一对象：**
- 使用 `==` 操作符：比较引用地址

```java
String str1 = "Hello";
String str2 = "Hello";
String str3 = new String("Hello");

// 比较值
System.out.println(str1.equals(str2)); // true
System.out.println(str1.equals(str3)); // true

// 比较对象引用
System.out.println(str1 == str2); // true (字符串常量池)
System.out.println(str1 == str3); // false (new 创建的对象)
```

## 32、String str = new String("abc"); 创建了几个对象，为什么？

**答：** 可能创建 1 个或 2 个对象，具体取决于情况：

**创建 2 个对象的情况：**
1. 字符串字面量 "abc" 在字符串常量池中创建一个对象
2. `new String("abc")` 在堆内存中创建一个新的 String 对象

**创建 1 个对象的情况：**
如果字符串常量池中已经存在 "abc"，则只在堆中创建一个新对象。

```java
// 第一次执行 - 创建 2 个对象
String str1 = new String("abc"); // 常量池 + 堆对象

// 第二次执行 - 创建 1 个对象  
String str2 = new String("abc"); // 只在堆中创建，常量池已存在
```

### 注意！即使类是动态加载的（例如通过 ClassLoader 加载），字符串字面量 "abc" 仍然会进入字符串常量池。









