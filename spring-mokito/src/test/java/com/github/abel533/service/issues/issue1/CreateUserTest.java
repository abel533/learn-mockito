package com.github.abel533.service.issues.issue1;

import com.github.abel533.api.UserService;
import com.github.abel533.dubbo.api.EmployeeService;
import com.github.abel533.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles("baseServiceTest") //这里可以使用 BaseTest 内部静态类的 mock
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:com/github/abel533/service/issues/issue1/spring-database.xml",
        "classpath:spring/spring.xml"
})
public class CreateUserTest {

    @Autowired
    @InjectMocks
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Test
    public void test() {
        long newUserId = 999L;
        String newUserName = "super";
        Mockito.when(employeeService.getEmployeeName(newUserId)).thenReturn(newUserName);
        User user = userService.createUserBy(newUserId);
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(newUserName, user.getName());
    }

}
