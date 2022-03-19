package io.anyway.bigbang.framework.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractEntity {
    /**
     * automatically increment
     */
    Long id;
    /**
     * logic deleted sign
     */
    byte deleted;
    /**
     * automatically created once
     */
    Date createdAt;
    /**
     * automatically updated every time
     */
    Date updatedAt;
}
