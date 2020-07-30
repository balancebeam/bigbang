package io.anyway.bigbang.framework.core.concurrent;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Configuration
public class ConcurrentWrapper {

    private static AtomicReference<ConcurrentWrapper> instance= new AtomicReference<>();

    @Autowired(required = false)
    private List<InheritableThreadProcessor> processors= new LinkedList<>();

    @PostConstruct
    public void init(){
        instance.compareAndSet(null,this);
    }

    public static Runnable of(Runnable runnable){
        final Runnable runnableWrapper= RunnableWrapper.of(runnable);
        final List<Pair<InheritableThreadProcessor,Object>> mainThreadValueHolders= new LinkedList<>();
        for(InheritableThreadProcessor each: instance.get().processors){
            mainThreadValueHolders.add(new Pair<>(each,each.getInheritableThreadValue()));
        }
        Runnable delegate= () -> {
            try {
                for(Pair<InheritableThreadProcessor,Object> each: mainThreadValueHolders){
                    if(each.getValue()!= null) {
                        each.getKey().setInheritableThreadValue(each.getValue());
                    }
                }
                runnableWrapper.run();
            }catch (Exception e){
                log.error("Execute asynchronized method error",e);
            }
            finally {
                for(Pair<InheritableThreadProcessor,Object> each: mainThreadValueHolders){
                    each.getKey().removeInheritableThreadValue();
                }
            }
        };
        return delegate;
    }

    public static <T>Callable<T> of(Callable<T> task){
        final Callable<T> callableWrapper= CallableWrapper.of(task);
        final List<Pair<InheritableThreadProcessor,Object>> mainThreadValueHolders= new LinkedList<>();
        for(InheritableThreadProcessor each: instance.get().processors){
            mainThreadValueHolders.add(new Pair<>(each,each.getInheritableThreadValue()));
        }
        Callable<T> delegate= () -> {
            try {
                for(Pair<InheritableThreadProcessor,Object> each: mainThreadValueHolders){
                    if(each.getValue()!= null) {
                        each.getKey().setInheritableThreadValue(each.getValue());
                    }
                }
                return callableWrapper.call();
            }catch (Exception e){
                log.error("Execute asynchronized method error",e);
                throw e;
            }
            finally {
                for(Pair<InheritableThreadProcessor,Object> each: mainThreadValueHolders){
                    each.getKey().removeInheritableThreadValue();
                }
            }
        };
        return delegate;
    }
}
