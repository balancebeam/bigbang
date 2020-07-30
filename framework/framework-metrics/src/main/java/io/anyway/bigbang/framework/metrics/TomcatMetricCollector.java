package io.anyway.bigbang.framework.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;

@Slf4j
public class TomcatMetricCollector implements TomcatConnectorCustomizer , MeterBinder {

    private Connector connector;

    @Override
    public void customize(Connector connector) {
        this.connector= connector;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        ProtocolHandler handler = connector.getProtocolHandler();
        String prefix= "bigbang.tomcat.";
        Gauge.builder(prefix+"active_count", handler, x ->  handler.getExecutor()!=null?((ThreadPoolExecutor) handler.getExecutor()).getActiveCount(): 0).register(registry);
        Gauge.builder(prefix+"max_active_count", handler, x -> handler.getExecutor()!=null?((ThreadPoolExecutor) handler.getExecutor()).getMaximumPoolSize(): 0).register(registry);
        Gauge.builder(prefix+"task_count", handler, x -> handler.getExecutor()!=null?((ThreadPoolExecutor) handler.getExecutor()).getTaskCount(): 0).register(registry);
        Gauge.builder(prefix+"completed_task_count", handler, x -> handler.getExecutor()!=null?((ThreadPoolExecutor) handler.getExecutor()).getCompletedTaskCount(): 0).register(registry);
        Gauge.builder(prefix+"submitted_count", handler, x -> handler.getExecutor()!=null?((ThreadPoolExecutor) handler.getExecutor()).getSubmittedCount(): 0).register(registry);
    }

}
