package io.anyway.bigbang.framework.model.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class AbstractEntity {
    private Long id;
    private int deleted;
    private Date createTime;
    private Date updateTime;
}
