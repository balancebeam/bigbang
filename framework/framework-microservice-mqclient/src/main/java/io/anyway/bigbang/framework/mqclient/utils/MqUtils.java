package io.anyway.bigbang.framework.mqclient.utils;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class MqUtils {
    public static final String MQ_MESSAGE_HEADER = "MQMessageHeader";

    public static Long evalPartitionKey(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        return Long.valueOf(format.format(date));
    }

    public static Long[] evalValidQueryPartitionKeys(Date date) {
        Date oneMonthBefore = new DateTime(date).minusMonths(1).toDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");

        Long curPartitionKey = Long.valueOf(format.format(date));
        Long prePartitionKey = Long.valueOf(format.format(oneMonthBefore));

        return new Long[]{prePartitionKey, curPartitionKey};
    }
}
