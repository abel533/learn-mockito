package com.github.abel533.api;

import com.github.abel533.model.User;

public interface UserService {

    /**
     * 根据 id 获取用户信息
     *
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 根据 id 创建用户
     *
     * @param id
     * @return
     */
    User createUserBy(Long id);


    /**
     * 根据 id 删除用户
     *
     * @param id
     */
    int deleteById(Long id);

}
