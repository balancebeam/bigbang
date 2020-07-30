package io.anyway.bigbang.test;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterTest {

    public static void main(String[] args){

        RateLimiter limiter=  RateLimiter.create(5);
        boolean b= limiter.tryAcquire(30);
        System.out.println(b);
        b= limiter.tryAcquire(2);
        System.out.println(b);


    }
}
