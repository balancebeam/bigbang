package io.anyway.bigbang.example.service;

import io.anyway.bigbang.example.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> getUser(String name);

    void addUser(User user);

}
