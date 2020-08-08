package io.anyway.bigbang.framework.mqclient.metrics;

import lombok.Data;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ProcessQueueInfo;

import java.util.TreeMap;

@Data
public class MqClientMetric {
    private int producerQueuedTask;
    private int producerSendingPoolActiveSize;
    private int producerSendingPoolCoreSize;
    private int producerSendingPoolMaxSize;
    private int producerSendingPoolLargestSize;
    private long allSentMsgSize;
    private Long allReceivedMsgSize;
    private Long allFailureReceivedMsgSize;

    private String consumerGroupName;
    private int consumerThreadCoreSize;
    private TreeMap<MessageQueue, ProcessQueueInfo> consumerStatusInfo;
}
