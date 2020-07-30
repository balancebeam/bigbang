package io.anyway.bigbang.framework.tenant.proxy;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.List;

public class PluginDispatcherServlet extends DispatcherServlet  implements ApplicationListener<ContextRefreshedEvent> {

    private static ThreadLocal<HandlerExecutionChain> handlerThreadLocal= new ThreadLocal<>();

    @Override
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        return handlerThreadLocal.get();
    }

    public HandlerExecutionChain getAvailableHandler(HttpServletRequest request) throws Exception {
        return super.getHandler(request);
    }

    public static void setHandler(HandlerExecutionChain handler){
        handlerThreadLocal.set(handler);
    }
    public static void removeHandler(){
        handlerThreadLocal.remove();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        super.onApplicationEvent(event);
        Field f= ReflectionUtils.findField(DispatcherServlet.class,"handlerMappings");
        ReflectionUtils.makeAccessible(f);
        try {
            List<HandlerMapping> handlerMappings= (List<HandlerMapping>)f.get(this);
            for(int i=handlerMappings.size()-1;i>=0;i--){
                HandlerMapping handlerMapping= handlerMappings.get(i);
                if(!(handlerMapping instanceof RequestMappingHandlerMapping)){
                    handlerMappings.remove(i);
                }
            }
            logger.info(handlerMappings);
        } catch (IllegalAccessException e) {
            logger.error(e);
        }

    }
}
