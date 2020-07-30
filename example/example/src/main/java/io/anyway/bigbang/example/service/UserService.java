package io.anyway.bigbang.example.service;

import io.anyway.bigbang.example.entity.User;

import java.util.List;

public interface UserService {

    List<User> getUsers();

    User getUser(Long id);

    void addUser(User user);

    void deleteUser(Long id);

    List<User> getShardingUsers();

    User getShardingUser(Long id);

    void addShardingUser(User user);

    void deleteShardingUser(Long id);

}
