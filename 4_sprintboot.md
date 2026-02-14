## 1、什么是 Spring Framework？

**答：** Spring Framework 是一个开源的 Java 平台应用程序框架，提供了全面的基础设施支持。

**核心特性：**
- **IoC（控制反转）容器**：由Spring管理对象的生命周期和依赖关系
- **AOP（面向切面编程）**：提供横切关注点的解决方案
- **数据访问**：简化 JDBC、ORM 框架的使用
- **事务管理**：声明式和编程式事务支持
- **MVC 框架**：Web 应用开发支持
- **测试支持**：集成测试和单元测试工具

```java
// 传统方式 - 紧耦合
public class UserService {
    private UserRepository repository = new DatabaseUserRepository();
}

// Spring 方式 - 松耦合
@Service
public class UserService {
    @Autowired
    private UserRepository repository; // 依赖注入
}
```

### 为什么Spring的依赖注入算松耦合，何以见得？
#### 传统方式
1. 直接依赖类具体实现，如果需要修改UserRepository的实例，必须直接修改源码。
2. 单元测试困难，没法轻易Mock.
3. 注意：传统框架也可以定义某一个成员变量为抽象类成员，构造函数传入具体实现，这也算松耦合。
#### Spring的方案
Spring 并没有“创造”松耦合，而是：
1. 自动管理对象的创建和生命周期（你不用手动 new）
2. 自动完成依赖的查找与注入（你不用手动组装）
3. 提供配置化方式切换实现（如通过 @Primary、@Profile、配置文件等）
    - @Primary 告诉Spring当存在多个满足条件的@Bean时，选这个。
    - @Qualifier("{NAME_OF_BEAN}") 在需要注入的位置，主动指定需要注入的Bean。
    - @Profile 可以按照环境激活不同的配置，例如@Profile("dev")指定为开发环境的配置，test指定为测试环境配置，prod指定生产环境。
4. 支持 AOP、事务、作用域等高级特性
##### @Profile示例
```java
@Configuration
@Profile("dev")
public class DevDatabaseConfig {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(H2).build();
    }
}

@Configuration
@Profile("prod")
public class ProdDatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // 配置 MySQL 连接
    }
}
```
**激活 Profile**
启动参数：--spring.profiles.active=prod
配置文件：application.properties 中写 spring.profiles.active=dev
环境变量：SPRING_PROFILES_ACTIVE=test

##### 注：从 Spring 4.3 开始（包括 Spring Boot 2.x 及更高版本），如果你的类中只有一个构造函数，那么 @Autowired 注解是可以省略的，Spring 会自动使用该构造函数进行依赖注入。

##### AOP(面向切面编程)
在不修改业务代码的前提下，统一处理横切关注点（cross-cutting concerns）。即，统一关注并处理流程中的某一个层次、阶段。

##### Spring 方式事务管理
Spring 通过 AOP 代理（JDK 动态代理或 CGLIB）在方法执行前后自动开启/提交/回滚事务。
支持传播行为（如 REQUIRES_NEW）、隔离级别、超时等配置。
```java
@Service
public class OrderService {

    @Transactional
    public void createOrder(Order order) {
        orderRepo.save(order);
        inventoryService.reduceStock(order.getItemId());
        // 如果中间抛异常，整个操作回滚
    }
}
```

## 2、如何配置Spring

**配置方式：**
1. **XML 配置**（传统方式）
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="userService" class="com.example.UserService">
        <property name="userRepository" ref="userRepository"/>
    </bean>
    
    <bean id="userRepository" class="com.example.UserRepositoryImpl"/>
</beans>
```

2. **Java 配置**（现代方式）
```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
    
    @Bean
    public UserService userService(UserRepository userRepository) {
        UserService service = new UserService();
        service.setUserRepository(userRepository);
        return service;
    }
    
    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }
}
```

## 3、什么是依赖注入？

**答：** 依赖注入（Dependency Injection, DI）是一种设计模式，对象的依赖关系由外部容器在运行时注入，而不是由对象自己创建。

**三种注入方式：**

1. **构造器注入**
```java
@Service
public class UserService {
    private final UserRepository repository;
    
    // 构造器注入
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

2. **Setter 注入**
```java
@Service
public class UserService {
    private UserRepository repository;
    
    // Setter 注入
    @Autowired
    public void setUserRepository(UserRepository repository) {
        this.repository = repository;
    }
}
```

3. **字段注入**
```java
@Service
public class UserService {
    @Autowired
    private UserRepository repository; // 字段注入
}
```

**优势：**
- 降低耦合度
- 提高可测试性
- 增强可维护性
- 支持运行时动态配置

#### 注意，构造器注入在三者中最早，在Bean实例化时注入。另外两种的执行顺序不确定，都在实例化之后。成员的最终值取决于最后注入的值。
这样虽然不至于导致Bug，但是是冗余且混乱的设计，不要这么做。

## 4、什么是 Spring IOC 容器？

**答：** IOC（Inversion of Control）容器是 Spring 框架的核心，负责管理对象的生命周期和依赖关系。

**两种主要的 IOC 容器：**

1. **BeanFactory**
   - 基础的 IOC 容器
   - 延迟初始化 Bean
   - 轻量级，适合资源受限环境

```java
Resource resource = new ClassPathResource("applicationContext.xml");
BeanFactory factory = new XmlBeanFactory(resource);
UserService userService = (UserService) factory.getBean("userService");
```

2. **ApplicationContext**
   - 扩展了 BeanFactory 的功能
   - 立即初始化 Bean
   - 提供企业级特性（AOP、消息资源处理等）

```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
UserService userService = context.getBean(UserService.class);
```

## 5、区分 BeanFactory 和 ApplicationContext

| 特性 | BeanFactory | ApplicationContext |
|------|-------------|-------------------|
| 初始化时机 | 延迟加载 | 立即加载 |
| 国际化支持 | 不支持 | 支持 |
| 事件发布 | 不支持 | 支持 |
| AOP 支持 | 有限支持 | 完全支持 |
| 资源访问 | 基本支持 | 增强支持 |
| 应用场景 | 简单应用 | 企业级应用 |

#### 关于初始化时机
BeanFactory默认为懒加载（Lazy）；Application默认为预加载（Eager），但是可以在使用@Lazy注解来标记一个类需要使用懒加载。【对于BeanFactory，没有@Eager注解，但是可以手动调用一次getBean触发初始化。】

## 6、列举 IoC 的一些好处

**答：** IoC（控制反转）的主要优势：

1. **松耦合**
   - 组件间依赖关系由容器管理
   - 降低类之间的直接依赖

2. **易于测试**
   - 可以轻松注入 Mock 对象
   - 支持单元测试和集成测试

3. **配置灵活**
   - 运行时动态配置
   - 支持多种配置方式

4. **生命周期管理**
   - 容器管理对象的创建和销毁
   - 自动处理资源释放

5. **减少样板代码**
   - 无需手动创建和管理对象
   - 减少重复的工厂代码

## 7、什么是 spring bean？

**答：** Spring Bean 是由 Spring IoC 容器管理的对象。

**Bean 的特征：**
- 由 Spring 容器实例化
- 由 Spring 容器管理生命周期
- 依赖关系由 Spring 容器注入
- 可以通过配置定义其行为

**Bean 的创建方式：**
1. 通过构造器创建
2. 通过工厂方法创建
3. 通过 FactoryBean 创建

```java
// 通过 @Component 注解标记 Bean
@Component
public class UserService {
    // Bean 定义
}

// 通过 @Bean 注解在配置类中定义
@Configuration
public class AppConfig {
    @Bean
    public UserService userService() {
        return new UserService();
    }
}
```

## 8、Spring 作用域 Bean Scope
Spring 默认所有 Bean 是 单例（Singleton），但支持多种作用域：
1. singleton(default): 整个应用上下文只有一个实例
2. prototype: 每次注入或 getBean() 都创建新实例
3. request: Web 应用中，每个 HTTP 请求一个实例
4. session: 每个用户会话一个实例
5. application: ServletContext 生命周期内一个实例
 - NOTE: ServletContext 是 Java Servlet 规范中的一个核心接口，代表整个 Web 应用的上下文环境。它的生命周期与 Web 应用（Web Application）的部署和卸载紧密绑定。
 - 例：Tomcat中的WebApp中的每一个具体应用，对应一个ServletConext.
6. websocket: 每个 WebSocket 创建一个实例，仅在 WebSocket环境中有效。

示例：
```java
@Component
@Scope("prototype")
public class ShoppingCart {
    // 每个用户应有自己的购物车
}
```
### 注：session和websocket的区别
1. HTTP Session（@Scope("session")）和 WebSocket 连接是两个完全不同的机制，不能混为一谈。
2. WebSocket 本身不等于 HTTP Session，也不直接绑定到 Spring 的 session 作用域 Bean。
3. 对于HTTP Session: 从用户首次访问开始，到超时或调用 invalidate()；由于记录用户登录状态，以服务端Session存活状态为准。
4. 对于WebSocket: 从 WebSocket 握手成功开始，到连接关闭; 表示一个长连接通道，用于双向实时通信。

## 9、Spring 的内部 bean？

**答：** 内部 Bean 是在其他 Bean 的**属性**中直接定义的匿名 Bean。

**特点：**
- 只能被包含它的 Bean 使用
- 不能被其他 Bean 引用
- 通常用于一次性使用的辅助对象

```xml
<!-- XML 配置中的内部 Bean -->
<bean id="outerBean" class="com.example.OuterBean">
    <property name="innerBean">
        <bean class="com.example.InnerBean"/>
    </property>
</bean>
```

```java
// Java 配置中的内部 Bean
@Configuration
public class AppConfig {
    @Bean
    public OuterBean outerBean() {
        OuterBean outer = new OuterBean();
        // 内部 Bean
        outer.setInnerBean(new InnerBean());
        return outer;
    }
}
```

## 10、自动装配有哪些方式
1. @Autowired（Spring 原生）
 - 注入方式：默认按 类型（byType） 自动装配
 - 可选位置：字段、方法、构造器
 - 是否必须：默认 required = true，即必须找到匹配的 Bean；若设为 false，则允许为 null
 - 配合使用：可与 @Qualifier 联合使用，解决多个同类型 Bean 的歧义（按名称限定）
```java
@Autowired
@Qualifier("userService")
private UserService userService;
```
2. @Inject（JSR-330 标准）
 - 注入方式：按 类型（byType） 自动装配（和 @Autowired 类似）
 - 可选位置：字段、方法、构造器
 - 是否必须：总是 required，不支持设置为可选（无 required=false 选项）
 - 配合使用：使用 @Named（而非 @Qualifier）来指定 Bean 名称
 - 优点：与 Spring 解耦，适用于其他兼容 JSR-330 的容器
```java
@Inject
@Named("userService")
private UserService userService;
```
3. @Resource（JSR-250 标准）
 - 注入方式：默认按 名称（byName） 自动装配
 - 可选位置：字段、方法（不能用于构造器）
 - 如果没有指定 name，会使用字段名或 setter 方法名作为 Bean 名称
 - 若找不到同名 Bean，则回退到按类型（不同容器实现可能有差异，但在 Spring 中优先 byName）
 - 是否必须：默认 required，但可通过 lookup 或其他方式间接控制
```java
@Resource(name = "userService")
private UserService userService;

// 等价于：
@Resource
private UserService userService; // 会查找名为 "userService" 的 Bean
```
4. XML 自动装配（传统方式）
在 Spring 的 XML 配置文件中，可以通过 <bean> 元素的 autowire 属性启用自动装配.
```xml
<bean id="myBean" class="com.example.MyClass" autowire="byType"/>
```

#### Bean的名字是什么
在 Spring 中，每个 Bean 都有一个 唯一标识符（ID 或 name），这个就是它的“名字”。例如，对于Component及其衍生注解，它可以通过以下方式指定：
```java
@Service("userService") // 显式指定名字为 "userService"
public class UserServiceImpl implements UserService { ... }
```

## 11、如何在 spring 中启动注解装配？

**答：** 启用注解装配的方法：

1. **XML 配置启用**
```xml
<context:annotation-config/>
<!-- 或者 -->
<context:component-scan base-package="com.example"/>
```

2. **Java 配置启用**
```java
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
    // 配置类自动启用注解
}
```

3. **Spring Boot 自动启用**
```java
@SpringBootApplication // 包含 @EnableAutoConfiguration 和 @ComponentScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
## 12、@Component, @Controller, @Repository, @Service 注解的区别

**答：** 这些注解的关系和用途：

1. **@Component**：通用的组件注解，是其他注解的元注解

2. **@Controller**：用于表示 MVC 控制器层
```java
@Controller
public class UserController {
    @RequestMapping("/users")
    public String getUsers() {
        return "users";
    }
}
```

3. **@Service**：用于表示业务逻辑层
```java
@Service
public class UserService {
    public List<User> getAllUsers() {
        // 业务逻辑
        return userRepository.findAll();
    }
}
```

4. **@Repository**：用于表示数据访问层，还提供持久化异常转换
```java
@Repository
public class UserRepositoryImpl implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }
}
```

## 13、@Required 注解有什么用？

**答：** @Required 注解用于标记 setter 方法，表示该属性必须在配置时设置。

**注意：** 从 Spring 5.1 开始已被废弃，推荐使用构造器注入。

## 14、@Qualifier 注解有什么用？

**答：** @Qualifier 注解用于解决自动装配时的歧义性问题。

**使用场景：**
- 存在多个相同类型的 Bean
- 需要指定使用特定的 Bean 实例

## 15、@Autowired 注解有什么用？

**答：** @Autowired 注解用于自动装配 Bean 的依赖关系

## 16、Spring DAO 有什么用？

**答：** Spring DAO（Data Access Object）提供数据访问层的标准和支持。

**主要功能：**
1. **统一异常处理**
   - 将特定技术的异常转换为 Spring 的 DataAccessException
   - 提供一致的异常层次结构

2. **模板类支持**
   - JdbcTemplate：简化 JDBC 操作
   - HibernateTemplate：简化 Hibernate 操作
   - JpaTemplate：简化 JPA 操作

3. **事务管理**
   - 声明式事务支持
   - 编程式事务支持

```java
@Repository
public class UserDaoImpl implements UserDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public User findById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new Object[]{id},
            new UserRowMapper()
        );
    }
}
```

## 17@RequestMapping 注解有什么用？

**答：** @RequestMapping 注解用于映射 HTTP 请求到处理器方法。

**可以配置的属性：**
- value/path：请求路径
- method：HTTP 方法
- params：请求参数
- headers：请求头
- consumes：消费的内容类型
- produces：生产的内容类型

```java
@Controller
public class UserController {
    
    // 基本用法
    @RequestMapping("/users")
    public String getUsers() {
        return "users";
    }
    
    // 指定 HTTP 方法
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public String createUser(@RequestBody User user) {
        // 创建用户
        return "redirect:/users";
    }
    
    // RESTful 风格（推荐使用专门的注解）
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Long id) {
        // 获取用户详情
        return "user-detail";
    }
}
```

## 18、Spring JDBC API 中存在哪些类？

**答：** Spring JDBC API 的主要类：

1. **JdbcTemplate**：核心模板类
```java
@Autowired
private JdbcTemplate jdbcTemplate;

public List<User> findAll() {
    return jdbcTemplate.query("SELECT * FROM users", new UserRowMapper());
}
```

2. **NamedParameterJdbcTemplate**：支持命名参数
```java
@Autowired
private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

public User findById(Long id) {
    String sql = "SELECT * FROM users WHERE id = :id";
    MapSqlParameterSource params = new MapSqlParameterSource("id", id);
    return namedParameterJdbcTemplate.queryForObject(sql, params, new UserRowMapper());
}
```

3. **SimpleJdbcInsert**：简化插入操作
```java
@Autowired
private SimpleJdbcInsert jdbcInsert;

public void insertUser(User user) {
    jdbcInsert.withTableName("users")
              .usingGeneratedKeyColumns("id")
              .execute(new BeanPropertySqlParameterSource(user));
}
```

4. **DataSource**：数据源接口
```java
@Bean
public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:mysql://localhost:3306/test");
    ds.setUsername("root");
    ds.setPassword("password");
    return ds;
}
```

