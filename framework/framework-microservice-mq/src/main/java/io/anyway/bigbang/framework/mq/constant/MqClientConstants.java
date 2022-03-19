package io.anyway.bigbang.framework.mq.constant;

public interface MqClientConstants {

    Integer PURGE_JOB_TIME_AHEAD = 3;

    Integer MAX_RETRY_TIMES = 5;

    Long NEXT_RETRY_GAP = 60 * 1000L;

    String MESSAGE_KEY_SPLITTER = ":";

    int MESSAGE_MAX_SIZE = 8192;

}
