package com.github.abel533;

import com.github.abel533.dubbo.api.EmployeeService;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("baseServiceTest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-database.xml",
        "classpath:spring/spring.xml"
})
public abstract class BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 在当前配置类中，对所有 dubbo 接口提供 Mock 实现
     */
    @Profile("baseServiceTest")
    @Configuration
    public static class MockConfig {

        @Bean
        @Primary
        public EmployeeService employeeService() {
            return Mockito.mock(EmployeeService.class);
        }

    }

}
