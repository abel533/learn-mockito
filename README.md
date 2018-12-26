# 测试规范和要求

微服务模式中，由于需要调用其它服务提供的接口服务，测试会变得非常复杂。

比如 B 服务依赖 A 服务，在 B 服务中编写测试的时候，测试会受 A 服务的影响，如果 A 服务没有按照预期返回结果，
此时对 B 服务来说，自己的服务没法进行正常测试，反而发现了一个鞭长莫及的 A 服务 BUG。
当互相调用的服务更多时（比如 A 又调用了一个 Z，依次类推），测试就无法进行下去（测试隔离）。

因此为了避免服务调用的影响，在我们的测试中会使用 Mock，和字面翻译一样，就是假的，模仿的。

>[**为什么要使用模拟对象？**](https://zh.wikipedia.org/wiki/%E6%A8%A1%E6%8B%9F%E5%AF%B9%E8%B1%A1)
>
>在单元测试中，模拟对象可以模拟复杂的、真实的（非模拟）对象的行为， 
>如果真实的对象无法放入单元测试中，使用模拟对象就很有帮助。
>
>在下面的情形，可能需要使用模拟对象来代替真实对象：
>
>- 真实对象的行为是不确定的（例如，当前的时间或当前的温度）；
>- 真实对象很难搭建起来；
>- 真实对象的行为很难触发（例如，网络错误）；
>- 真实对象速度很慢（例如，一个完整的数据库，在测试之前可能需要初始化）；
>- 真实的对象是用户界面，或包括用户界面在内；
>- 真实的对象使用了回调机制；
>- 真实对象可能还不存在；
>- 真实对象可能包含不能用作测试（而不是为实际工作）的信息和方法。
>
>例如，一个可能会在特定的时间响铃的闹钟程序可能需要外部世界的当前时间。
>要测试这一点，测试一直要等到闹铃时间才知道闹钟程序是否正确地响铃。
>如果使用一个模拟对象替代真实的对象，可以变成提供一个闹铃时间（不管是否实际时间）,
>这样就可以隔离地测试闹钟程序。

例如在上面例子中，对 所有用到的A 服务中接口进行 Mock，提供一个假的接口实现，
这个实现会完全按照 A 服务接口的预期结果运行，不需要启动 A 服务，不会受 A 服务的影响，
因此我们可以针对 B 服务的接口进行测试，对于具体的一个服务，
**测试分为两大部分，分别为 DAO 层和 Service 层。**

不管是 DAO 层还是 Service 层测试，因为代码在同一个项目中，各自不能独立启动，
并且 Service 层可能注入了其他服务的接口，因此如果不提前提供实现，
整个 Spring 在启动时会因为找不到实现而无法正常启动。所以在进行测试前，先对需要的接口进行 Mock。

## 如何 Mock 其他服务接口

>这里使用 mockito，官网地址: https://site.mockito.org

首先查找项目 Service 模块依赖的外部服务接口，最简的情况下，只需要找到所有被注入到当前服务中的接口（假设 A
服务提供了 10 个接口，但是只用（**注入**）了 2 个，只需要 mock 用到的 2 个接口）。

比如在当前示例项目中，spring-mockito 服务引用了 dubbo-api 中的 `EmployeeService` 接口。
在示例的 `BaseTest` 中的内部静态类提供了如下配置：

```java
/**
 * 在当前配置类中，对所有外部 dubbo 接口提供 Mock 实现
 */
@Profile("baseServiceTest")
@Configuration
public static class MockConfig {

    @Bean
    @Primary//增加该注解后，同时存在多个实现时，优先使用当前的
    public EmployeeService employeeService() {
        return Mockito.mock(EmployeeService.class);
    }

}
```

这里通过 `Mockito.mock(EmployeeService.class)` 返回了一个 mock 后的接口。

>这里是注解配置用法，不理解的情况下直接按照这里写即可。 
>
>mockito 文档：http://static.javadoc.io/org.mockito/mockito-core/2.23.4/org/mockito/Mockito.html 
>
>javadoc 也提供了详细的示例: http://static.javadoc.io/org.mockito/mockito-core/2.23.4/overview-summary.html

在后续启动过程中，Spring 可以找到这个接口实现，因此下面代码中的注入不会出错：

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private EmployeeService employeeService;

    //...
}
```

有了预先配置好的 mock 接口后，Spring 能正常启动，下面简单说说 Dao 测试。

## Dao 测试

对于 Dao 中自带的（通用 Mapper)方法不需要进行额外的测试（主要几个增删改查也建议测试）。但是自己手动添加的接口方法需要增加测试。

示例中的 DAO 测试（UserDaoTest.java）如下：

```java
public class UserDaoTest extends BaseTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void testSelectById() {
        User user = userDao.selectById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getName());
    }

}
```

通过简单的测试保证自己写完一个 DAO 方法时，确保方法正确执行。

你应该能发现上面测试中的一个问题，这里查询了 ID 为 1 的用户，在断言中期望用户名是 admin。

**数据库中一定存在该用户吗？**如果清库了怎么办？改名字了怎么办？

下面看看如何准备测试数据。

## 测试数据

在示例中（spring-mokito\src\test\resources\database），提供了 data.sql 和 schema.sql 两个 SQL 文件。

其中 schema.sql 中需要提供建表 SQL（必须和开发数据库表结构一致，但是用的 HSQL 语法），示例如下：

```sql
drop table if exists user;

create table user
(
  id    BIGINT        not null,
  name  VARCHAR(200)  default NULL,
  primary key (id)
);
```

有多个表的情况下依次写上即可。

在 data.sql 中，提供可选的默认数据，示例如下：

```sql
insert into user(id, name) values(1, 'admin');
insert into user(id, name) values(2, 'dev');
insert into user(id, name) values(3, 'test');
```

>注意 HSQL 语法，如果插入日期，需要形如 `DATE '2018-12-25'` 时间戳如 `TIMESTAMP '2018-12-25 15:00:20'`。  
>遇到类似问题的时候可以网上搜 HSQL 语法解决。  
>HSQL: http://hsqldb.org/doc/guide/  

有了上述表结构和数据后，需要配置一个内存数据库，本文使用的 Spring XML 方式：

```xml
<jdbc:embedded-database id="dataSource" generate-name="true">
    <jdbc:script location="classpath:database/schema.sql"/>
    <jdbc:script location="classpath:database/data.sql"/>
</jdbc:embedded-database>
```

注解方式配置可以参考：
```java
@Bean
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScript("schema.sql")
            .addScripts("user_data.sql", "country_data.sql")
            .build();
}
```

>配置内存数据库后，还需要添加对应的数据库驱动。
>
>内存数据库参考：[jdbc-embedded-database-support](https://docs.spring.io/spring/docs/4.3.14.RELEASE/spring-framework-reference/html/jdbc.html#jdbc-embedded-database-support)

通过预设的测试数据，就能保证测试数据的正确。解决了测试数据，也看到 DAO 测试后，继续 Service 接口测试。

>复杂测试需要大量关联数据时，可以从真实数据库导出测试数据写入特定的 data.sql 文件中。

## Service 测试

**要求：所有接口必须提供测试！！！**

**要求：所有接口必须提供测试！！！**

**要求：所有接口必须提供测试！！！**

> 为了尽快发现问题，在开发完成一个接口（类）或接口方法后，先进行测试，尽早避免低级错误耽误时间。

参考示例 UserServiceTest.java，代码如下：

```java
public class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Test
    public void testGetById() {
        User user = userService.getById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getName());
    }

    @Test
    public void testCreateUserBy() {
        long newUserId = 999L;
        String newUserName = "super";
        //针对会被调用的进行设置模拟数据
        Mockito.when(employeeService.getEmployeeName(newUserId)).thenReturn(newUserName);
        //调用接口测试
        User user = userService.createUserBy(newUserId);
        //验证
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(newUserName, user.getName());
        //检查是否调用下面的方法
        Mockito.verify(employeeService).getEmployeeName(newUserId);
    }

    @Test
    public void testDeleteById() {
        assertEquals(1, userService.deleteById(1L));
        assertEquals(1, userService.deleteById(2L));
        assertEquals(0, userService.deleteById(888L));
    }

}
```

上面的测试中，有一个特殊的方法，就是 `UserService#createUserBy` 接口方法。在这个接口实现中，
调用了外部的 `EmployeeService` 接口，通过 id 获取了某种途径的用户名。接口实现代码如下：

```java
@Override
public User createUserBy(Long id) {
    if (getById(id) != null) {
        throw new RuntimeException("用户已经存在");
    }
    //调用了其他服务的接口
    String userName = employeeService.getEmployeeName(id);
    User user = new User(id, userName);
    userDao.insert(user);
    return user;
}
```

虽然前面针对 `EmployeeService` 接口提供了 mock 实现，只是避免了 Spring 找不到实现无法启动而已，
该实现还需要我们按照 mockito 的用法进行具体的配置后，才能真正起到模仿的作用。

在上面的测试中，在调用 UserService 方法前，我们通过下面代码设置了期望的数据：

```java
//针对会被调用的进行设置模拟数据
Mockito.when(employeeService.getEmployeeName(newUserId)).thenReturn(newUserName);
```

这行代码的含义非常简单，当使用 `newUserId` 参数调用 `employeeService` 的 `getEmployeeName` 方法时，返回 `newUserName` 的值。

然后在 UserService 中真正调用该方法时，就按照这里的设置返回了 `newUserName` 的值。通过 mock 完全隔离了真正的实现，并且达到了调用方法返回预期值的目的。

>更多 mockito 的用法看官方文档：http://static.javadoc.io/org.mockito/mockito-core/2.23.4/org/mockito/Mockito.html

## 解决 BUG，处理 issues

当有人（包括自己）发现系统中存在的 BUG 时，除了直接修复 BUG 外，更好的解决步骤如下：

1. 分析 BUG，确定原因
2. 针对 BUG，编写测试复现问题，由于存在该 BUG，测试无法通过
3. 处理 BUG，解决问题后，跑测试确认

**为什么还要写麻烦的测试？**

由于代码在不停的维护，发现 BUG 解决后，如果没有测试，那么在后续更新维护过程中，
很可能会把 BUG 放出来，如果有测试，就会很快发现问题。

当一个服务存在大量测试的时候，每次改动后跑一遍测试都能让你更放心，
当你想要对代码进行重构的时候，每做一次改动就跑一遍测试，可以让你重构的更有底气。
否则当你写了无数代码后再发现各种各样的问题时，你都不敢提交这些代码。

尽早测试可以发现缺陷，完善的测试可以树立对产品质量的信息。测试不能避免 BUG，但是可以预防。

对于针对 BUG 或者 issue 的测试，可能需要准备针对性的测试环境，此时可以完全使用独立的测试数据和配置文件。
针对性的去测试，同时避免影响其他基础的测试，做到测试隔离。

>尽早地和不断地进行软件测试

## 完全独立的数据环境

对于某些需要特殊数据环境的测试，可以参考 issues.issue1 中的示例，针对测试创建独立的环境。

## 单元测试 or 集成测试？ 

作为一个基本的测试法则，如果

- 测试中使用了数据库
- 测试中使用了网络调用另外一个组件或者应用
- 测试中使用了一个外部系统（例如，一个队列或者邮件服务器）
- 测试中读写文件或者执行了其他 I/O 操作
- 测试不依赖于源代码，而是使用应用程序的部署二进制文件

那它就是一个集成测试而不是单元测试。

业务系统中，很少会有真正的单元测试，所以本文中不明确区分单元测试和集成测试。

>也可以简单认为业务系统中的测试都是集成测试（很少有不和数据库交互的独立方法）。
>独立的工具库，例如 Gson，Guava 这种库中更多的是单元测试。

## 参考资料

- [mockito](https://site.mockito.org)
- [Mock 不是 Stub](https://github.com/jizusun/my-blog-articles/issues/9)
- [软件测试的反模式](https://jizusun.github.io/software-testing-anti-patterns)
- [jdbc-embedded-database-support](https://docs.spring.io/spring/docs/4.3.14.RELEASE/spring-framework-reference/html/jdbc.html#jdbc-embedded-database-support)