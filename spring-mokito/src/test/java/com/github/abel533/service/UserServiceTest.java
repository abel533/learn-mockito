package com.github.abel533.service;

import com.github.abel533.BaseTest;
import com.github.abel533.api.UserService;
import com.github.abel533.dubbo.api.EmployeeService;
import com.github.abel533.model.User;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

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
