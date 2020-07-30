package io.anyway.bigbang.framework.core.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Slf4j
public class AsyncTaskExecutorWrapper implements AsyncTaskExecutor {

    private final AsyncTaskExecutor target;

    public AsyncTaskExecutorWrapper(AsyncTaskExecutor target){
        this.target= target;
    }

    @Override
    public void execute(final Runnable task){
        target.execute(ConcurrentWrapper.of(task));
    }

    @Override
    public void execute(final Runnable task, final long startTimeout) {
        execute(task);
    }

    @Override
    public Future<?> submit(final Runnable task){
        return target.submit(ConcurrentWrapper.of(task));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task){
        return target.submit(ConcurrentWrapper.of(task));
    }

    public AsyncTaskExecutor getTarget(){
        return target;
    }
}
