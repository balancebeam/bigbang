package io.anyway.bigbang.framework.mq.schedule;

import io.anyway.bigbang.framework.mutex.annotation.Mutex;
import io.anyway.bigbang.framework.mq.service.MqConsumerClientManager;
import io.anyway.bigbang.framework.mq.service.MqProducerClientManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
public class MqMutexSchedule {

    @Resource
    private MqProducerClientManager mqProducerClientManager;

    @Resource
    private MqConsumerClientManager mqConsumerClientManager;

    @PostConstruct
    public void init(){
        log.info("Load mq mutex schedule");
    }

    @Mutex(value = "framework-mq-mutex",heartbeat = 15)
    @Scheduled(cron = "*/30 * * * * ?")
    public void mqProducerFailOverSchedule(){
        mqProducerClientManager.failover();
    }

    @Mutex(value = "framework-mq-mutex",heartbeat = 15)
    @Scheduled(cron = "0 0 0 */1 * ?")
    public void mqProducerPurgeSchedule(){
        mqProducerClientManager.purge();
    }

    @Mutex(value = "framework-mq-mutex",heartbeat = 15)
    @Scheduled(cron = "0 0 0 */1 * ?")
    public void mqConsumerPurgeSchedule(){
        mqConsumerClientManager.purge();
    }
}
