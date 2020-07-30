package io.anyway.bigbang.example.dao;

import io.anyway.bigbang.example.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {

    List<User> getUsers();

    User getUser(Long id);

    int addUser(User user);

    int deleteUser(Long id);
}
