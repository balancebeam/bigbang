package io.anyway.bigbang.framework.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public abstract class AbstractBaseEntity {
    private Long id;
    private Byte deleted;
    private Date createdAt;
    private Date updatedAt;
}
