package io.anyway.bigbang.example.service.impl;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.example.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Service
@RefreshScope
public class UserServiceImpl implements UserService {

    public static ThreadLocal<String> threadLocal = new TransmittableThreadLocal<>();

    @Resource
    private Executor executor;

    @Value("${dynamic.location:}")
    public void setLocation(String location){
        log.info("dynamic.location: {}",location);
    }

    @Override
    public Optional<User> getUser(String name) {
        User user= new User();
        user.setName(name);
        user.setAge(20);
        user.setGender("M");
        log.info("User: {}",user);
        threadLocal.set("main thread variable parameter");
        executor.execute(()->{
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
            log.info("this child thread value: {} ----", UserServiceImpl.threadLocal.get());
        });

        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
        threadLocal.remove();
        log.info("this main thread value: {} +++++", UserServiceImpl.threadLocal.get());
        return Optional.of(user);
    }

    @Override
    public void addUser(User user) {
        log.info("User: {}",user);
    }
}
