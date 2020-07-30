package io.anyway.bigbang.framework.core.trace;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;

public abstract class FrameworkTraceContext {

    public abstract String getTraceId();

    static FrameworkTraceContext INSTANCE= new FrameworkTraceContext(){

        @Override
        public String getTraceId() {
            return TraceContext.traceId();
        }
    };

    public static String getPlatformTraceId(){
        return INSTANCE.getTraceId();
    }
}
