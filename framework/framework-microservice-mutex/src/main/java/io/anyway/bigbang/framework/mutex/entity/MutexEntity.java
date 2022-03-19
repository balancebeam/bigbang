package io.anyway.bigbang.framework.mutex.entity;

import io.anyway.bigbang.framework.model.entity.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MutexEntity extends AbstractEntity {
    /**
     * 服务唯一ID
     */
    String serviceId;
    /**
     * 互斥体名称
     */
    String mutex;
    /**
     *
     */
    String host;
    /**
     *
     */
    String version;
    /**
     *
     */
    Date dueAt;
    /**
     *
     */
    Long heartbeat;
}
