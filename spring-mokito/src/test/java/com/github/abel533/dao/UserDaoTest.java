package com.github.abel533.dao;

import com.github.abel533.BaseTest;
import com.github.abel533.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
