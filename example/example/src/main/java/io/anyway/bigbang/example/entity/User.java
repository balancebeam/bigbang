package io.anyway.bigbang.example.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {
    private Long id;
    private String name;
    private String password;
    private String description;
}
