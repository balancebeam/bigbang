package io.anyway.bigbang.framework.mutex.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.concurrent.ExecutorService;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MutexState {

    volatile boolean occupancy;

    String host;

    String version;

    String serviceId;

    String mutex;

    Date dueAt;

    int heartbeat;

    volatile boolean running;

    ExecutorService executor;


}
