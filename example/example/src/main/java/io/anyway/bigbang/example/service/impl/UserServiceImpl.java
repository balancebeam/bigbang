package io.anyway.bigbang.example.service.impl;

import io.anyway.bigbang.example.dao.UserMapper;
import io.anyway.bigbang.example.entity.User;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.framework.datasource.sharding.annotation.DataSourceSharding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {



    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> getUsers() {
        return userMapper.getUsers();
    }

    @Override
    public User getUser(Long id) {
        return userMapper.getUser(id);
    }

    @Override
    public void addUser(User user) {
        userMapper.addUser(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteUser(id);
    }

    @Override
    @DataSourceSharding
    public List<User> getShardingUsers() {
        return getUsers();
    }

    @Override
    @DataSourceSharding
    public User getShardingUser(Long id) {
        return userMapper.getUser(id);
    }

    @Override
    @Transactional
    @DataSourceSharding
    public void addShardingUser(User user) {

        Map<Object, Object> map=  TransactionSynchronizationManager.getResourceMap();
        map.size();
        addUser(user);

        User u= getUser(user.getId());
        u.setId(user.getId()+99);

        addUser(u);

    }

    @Override
    @DataSourceSharding
    public void deleteShardingUser(Long id) {
        deleteUser(id);
    }
}
