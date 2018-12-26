package com.github.abel533.serivce;

import com.github.abel533.api.UserService;
import com.github.abel533.dao.UserDao;
import com.github.abel533.dubbo.api.EmployeeService;
import com.github.abel533.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserDao userDao;

    @Override
    public User getById(Long id) {
        return userDao.selectByPrimaryKey(id);
    }

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

    @Override
    public int deleteById(Long id) {
        return userDao.deleteByPrimaryKey(id);
    }

}
