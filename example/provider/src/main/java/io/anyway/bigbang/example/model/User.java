package io.anyway.bigbang.example.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @ApiModelProperty(value = "用户姓名")
    private String name;
    @ApiModelProperty(value = "用户性别")
    private String gender;
    @ApiModelProperty(value = "用户年龄")
    private int age;

}
