package io.anyway.bigbang.framework.mqclient.metrics;
//
//import com.google.common.base.Joiner;
//import RMQMessageConsumerBuilder;
//import MqClient;
//import io.micrometer.core.instrument.Gauge;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.binder.MeterBinder;
//import org.apache.rocketmq.common.message.MessageQueue;
//import org.apache.rocketmq.common.protocol.body.ProcessQueueInfo;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//
//@Component
//public class MqClientMetricCollector implements MeterBinder {
//    @Resource
//    private MqClient mqClient;
//
//    @Resource
//    private RMQMessageConsumerBuilder consumerBuilder;
//
//    @Override
//    public void collect(Map<String, Number> map) {
//        MqClientMetric producerMetric = mqClient.getMqClientMetric();
//        if (producerMetric != null) {
//            map.put("mq.producer.threads.active_count", producerMetric.getProducerSendingPoolActiveSize());
//            map.put("mq.producer.all.sent_msg", producerMetric.getAllSentMsgSize());
//            map.put("mq.producer.threads.core_size", producerMetric.getConsumerThreadCoreSize());
//            map.put("mq.producer.threads.max_size", producerMetric.getProducerSendingPoolMaxSize());
//            map.put("mq.producer.threads.largest_size", producerMetric.getProducerSendingPoolLargestSize());
//            map.put("mq.producer.threads.queued_tasks", producerMetric.getProducerQueuedTask());
//        }
//
//        List<MqClientMetric> consumerMetrics = consumerBuilder.getMqClientMetrics();
//        for (MqClientMetric metric : consumerMetrics) {
//            if (metric.getAllReceivedMsgSize() != null) {
//                map.put("mq.consumer.all.received_msg", metric.getAllReceivedMsgSize());
//                continue;
//            }
//            map.put("mq.consumer." + metric.getConsumerGroupName() + ".threads.active_count",
//                    metric.getConsumerThreadCoreSize());
//            TreeMap<MessageQueue, ProcessQueueInfo> consumerStatusInfo = metric.getConsumerStatusInfo();
//            for (Map.Entry<MessageQueue, ProcessQueueInfo> entry : consumerStatusInfo.entrySet()) {
//                MessageQueue messageQueue = entry.getKey();
//                ProcessQueueInfo processQueueInfo = entry.getValue();
//
//                String prefix = "mq.consumer." +
//                        metric.getConsumerGroupName() + "." +
//                        Joiner.on("-").join(
//                                messageQueue.getBrokerName(),
//                                messageQueue.getTopic(),
//                                messageQueue.getQueueId()
//                        ) + ".";
//                map.put(prefix + "commit_offset", processQueueInfo.getCommitOffset());
//                map.put(prefix + "cached_count", processQueueInfo.getCachedMsgCount());
//                map.put(prefix + "last_consume_ts", processQueueInfo.getLastConsumeTimestamp());
//                map.put(prefix + "last_pull_ts", processQueueInfo.getLastPullTimestamp());
//            }
//
//        }
//    }
//
//    @Override
//    public void bindTo(MeterRegistry registry) {
//    }
//}
