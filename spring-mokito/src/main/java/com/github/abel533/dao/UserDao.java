package com.github.abel533.dao;

import com.github.abel533.model.User;
import tk.mybatis.mapper.common.Mapper;

/**
 * 用户 Dao
 *
 * @author liuzenghui
 * @since 2016-11-30 11:00:48
 */
public interface UserDao extends Mapper<User> {

    /**
     * 根据主键查询用户 - 仅仅是为了测试增加的方法，正常使用自带的 selectByPrimaryKey 方法即可
     *
     * @param id
     * @return
     */
    User selectById(Long id);
}