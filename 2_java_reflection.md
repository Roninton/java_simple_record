## 1、什么是反射？

**答案：**
Java反射（Reflection）是指在程序运行时动态获取类的信息（如类名、方法、字段、构造器等）并操作这些信息的能力。通过反射，我们可以在运行时检查类、接口、字段和方法的信息，而不需要在编译时就知道这些信息。

**关键特性：**
- 运行时获取类的完整结构信息
- 动态创建对象实例
- 动态调用方法和访问字段
- 在运行时分析类的能力

**核心类：**
- `Class` 类：代表类的实体，在运行的Java应用程序中表示类和接口
- `Field` 类：提供有关类或接口的单个字段的信息
- `Method` 类：提供关于类或接口上单个方法的信息
- `Constructor` 类：提供关于类的单个构造函数的信息

## 2、Java 反射创建对象效率高还是通过 new 创建对象的效率高？

**答案：**
通过 `new` 关键字创建对象的效率更高。

**原因分析：**

1. **性能差异：**
   - `new` 操作是编译时确定的，JVM可以直接分配内存并调用构造函数
   - 反射需要在运行时通过ClassLoader查找类信息，涉及更多步骤

2. **具体开销：**
   ```
   new 操作：
   - 直接内存分配
   - 直接调用构造函数
   - JVM优化（内联inline等）
   
   反射操作：
   - 类加载检查
   - 安全权限验证
   - 方法查找和解析
   - 参数类型匹配
   ```

3. **性能对比示例：**
   ```java
   // new 方式 - 更快
   MyClass obj1 = new MyClass();
   
   // 反射方式 - 较慢
   Class<?> clazz = Class.forName("MyClass");
   Object obj2 = clazz.newInstance(); // 已废弃
   // 或者
   Constructor<?> constructor = clazz.getConstructor();
   Object obj3 = constructor.newInstance();
   ```

4. **实际建议：**
   - 频繁创建的对象使用 `new`
   - 只在需要动态性时才使用反射
   - 可以缓存 `Class` 对象来减少部分开销

## 3、什么叫对象序列化，什么是反序列化，实现对象序列化需要做哪些工作？

**答案：**

### 对象序列化（Serialization）
将对象的状态转换为字节流的过程，以便存储到文件、数据库或通过网络传输。

### 对象反序列化（Deserialization）
将字节流恢复为对象的过程，重建对象的状态。

### 实现序列化的必要工作：

1. **实现 Serializable 接口：**
   ```java
   public class Person implements Serializable {
       private static final long serialVersionUID = 1L;
       private String name;
       private int age;
       // ... 其他字段和方法
   }
   ```

2. **注意事项：**
   - `serialVersionUID`：用于版本控制，建议显式声明
   - 静态变量不会被序列化
   - `transient` 修饰的字段不会被序列化
   - 所有引用的对象也必须可序列化

3. **序列化示例：**
   ```java
   // 以下使用了装饰器模式实现了object -> file
   // 序列化
   ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("person.ser"));
   oos.writeObject(person);
   oos.close();
   
   // 反序列化
   ObjectInputStream ois = new ObjectInputStream(new FileInputStream("person.ser"));
   Person person = (Person) ois.readObject();
   ois.close();
   ```

## 4、哪里会用到反射机制？

**答案：**
反射机制在以下场景中广泛使用：

### 框架开发
- **Spring框架**：依赖注入、AOP代理生成
    Spring 通过反射实现依赖注入（DI）。例如：

    ```java
    @Component
    public class UserService {
        @Autowired
        private UserRepository userRepository;

        public void saveUser(User user) {
            userRepository.save(user);
        }
    }
    ```
    Spring 在启动时扫描所有带有 @Component 注解的类。
    通过反射获取类的字段（如 userRepository），并为其注入实例。

    注： AOP 是 Aspect-Oriented Programming 的缩写，中文翻译为“面向切面编程”。

    AOP 的核心思想：
    将分散在多个方法中的横切关注点（如日志记录、事务管理、权限校验等）抽取出来，形成独立的模块（称为“切面”），然后通过动态代理技术将其织入到目标方法中。
- **Hibernate**：ORM映射、动态SQL生成
    ```java
    @Entity
    @Table(name = "users")
    public class User {
        @Id
        private Long id;
        private String name;
        private int age;
    }
    ```
    Hibernate 通过反射读取 @Entity 和 @Table 注解，动态生成 SQL 语句。
    注：ORM 是一种编程技术，用于在面向对象编程语言（如 Java、C#、Python 等）和关系型数据库（如 MySQL、PostgreSQL、Oracle 等）之间建立映射关系。它解决了以下问题：

    阻抗失配（Impedance Mismatch）：

    面向对象语言中的对象模型与关系型数据库中的表结构存在本质差异。
    ORM 通过映射将两者统一起来，让开发者可以用面向对象的方式操作数据库。
    简化数据库操作：

    开发者无需手写复杂的 SQL 语句，ORM 框架会自动生成并执行 SQL。
    提高开发效率，降低出错概率。
- **MyBatis**：结果集映射、动态代理
    MyBatis 使用反射将查询结果映射到 Java 对象：

    ```xml
    <select id="selectUser" resultType="User">
        SELECT id, name, age FROM users WHERE id = #{id}
    </select>
    ```
    MyBatis 通过反射将数据库查询结果填充到 User 对象的字段中。

### 开发工具
- **IDE自动补全**：分析类结构提供智能提示
    当你在 IDE 中输入代码时，IDE 会通过反射分析类结构，提供方法和字段的智能提示：

    ```java
    String str = "Hello";
    str. // 此时 IDE 会通过反射列出 String 类的所有公共方法
    ```
- **调试器**：运行时检查对象状态
    调试器在运行时通`过反射查看对象的状态：

    ```java
    public class Person {
        private String name = "Alice";
        private int age = 25;
    }
    ```
    调试器通过反射读取 Person 对象的私有字段 name 和 age，并在调试窗口中显示其值。
- **单元测试框架**：JUnit、TestNG的方法发现
    JUnit 使用反射发现并运行测试方法：

    ```java
    public class CalculatorTest {
        @Test
        public void testAdd() {
            Calculator calc = new Calculator();
            assertEquals(5, calc.add(2, 3));
        }
    }
    ```
    JUnit 通过反射扫描类中的 @Test 注解，动态调用测试方法。

### 系统级应用
- **JDBC驱动加载**：`Class.forName()` 加载数据库驱动
    JDBC 驱动加载
    JDBC 通过反射加载数据库驱动：

    ```java
    Class.forName("com.mysql.cj.jdbc.Driver");
    Connection conn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/mydb", "root", "password");
    ```
    Class.forName() 动态加载 MySQL 驱动类，驱动类的静态代码块会自动注册到 DriverManager。
- **Servlet容器**：动态加载和实例化Servlet
    Tomcat 等 Servlet 容器通过反射动态加载和实例化 Servlet：

    ```java
    @WebServlet("/hello")
    public class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            resp.getWriter().write("Hello, World!");
        }
    }
    ```
    容器通过反射扫描 @WebServlet 注解，动态创建 HelloServlet 实例并处理请求。
- **插件系统**：动态加载第三方扩展
    许多插件系统通过反射动态加载第三方扩展：

    ```java
    public interface Plugin {
        void execute();
    }

    public class MyPlugin implements Plugin {
        @Override
        public void execute() {
            System.out.println("插件执行中...");
        }
    }
    ```
    主程序通过反射加载 MyPlugin 类并调用其 execute() 方法。


### 配置文件处理
- **XML/JSON解析**：根据配置动态创建对象
    XML/JSON 解析
    通过配置文件动态创建对象：

    ```xml
    <!-- config.xml -->
    <bean id="userService" class="com.example.UserService"/>
    ```
    ```java
    // 解析 XML 并通过反射创建对象
    String className = getConfiguredClassName("userService");
    Class<?> clazz = Class.forName(className);
    Object service = clazz.getDeclaredConstructor().newIns
    ```
    - **注解处理**：运行时读取注解信息

### 示例代码：
```java
// JDBC驱动加载
Class.forName("com.mysql.cj.jdbc.Driver");

// Spring IOC容器中的反射使用
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
MyService service = context.getBean("myService", MyService.class);

// 注解处理
Class<?> clazz = MyController.class;
if (clazz.isAnnotationPresent(RequestMapping.class)) {
    RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
    // 处理注解信息
}
```


### 补充知识
#### Java EE（Java Platform, Enterprise Edition）
    是一套用于构建大型、分布式、多层企业级应用的 Java 标准规范。它定义了一系列 API 和服务。
#### Servlet
    是 Java EE 规范中用于处理 HTTP 请求和响应的核心组件。它是一个运行在服务器端的 Java 类，用于动态生成 Web 内容。

#### Servlet 和 REST API 的联系
    1. Servlet 是底层机制 (Tomcat、Jetty)
    REST API 是一种架构风格，强调使用 HTTP 方法（GET/POST/PUT/DELETE）操作资源。在 Java 中，要实现 REST API，需要一个能接收 HTTP 请求并返回响应的机制——Servlet 正是这个基础。
    2. JAX-RS 构建在 Servlet 之上
    Java EE 提供了 JAX-RS（Java API for RESTful Web Services） 规范（如 Jersey、RESTEasy 实现），用于简化 REST API 开发。但 JAX-RS 框架内部仍然依赖 Servlet 来接收请求。
    3. 直接用 Servlet 也可以实现 REST API (Spring Boot、JAX-RS)
    虽然不推荐（因为代码繁琐），但你可以直接用原生 Servlet 实现 REST 风格的接口：
```java
@WebServlet("/api/users")
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        // 返回 JSON 数据
    }
}
```
    这种方式缺乏自动序列化、路径参数解析、内容协商等高级功能，所以通常使用 JAX-RS 或 Spring Boot（基于 Spring MVC）来构建 REST API。















