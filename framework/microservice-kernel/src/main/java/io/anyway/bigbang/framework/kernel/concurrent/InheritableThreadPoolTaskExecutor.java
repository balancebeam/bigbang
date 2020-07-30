package io.anyway.bigbang.framework.kernel.concurrent;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class InheritableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    public void execute(final Runnable task){
        super.execute(TtlRunnable.get(task,true));
    }

    @Override
    public void execute(final Runnable task, final long startTimeout) {
        super.execute(TtlRunnable.get(task,true),startTimeout);
    }

    @Override
    public Future<?> submit(final Runnable task){
        return super.submit(TtlRunnable.get(task,true));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task){
        return super.submit(TtlCallable.get(task,true));
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        return super.submitListenable(TtlRunnable.get(task,true));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return super.submitListenable(TtlCallable.get(task,true));
    }

}
