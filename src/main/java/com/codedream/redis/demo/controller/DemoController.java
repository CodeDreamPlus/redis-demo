package com.codedream.redis.demo.controller;

import com.codedream.redis.CodeDreamRedis;
import com.codedream.redis.demo.service.DemoService;
import com.codedream.redis.lock.LockType;
import com.codedream.redis.lock.RedisLock;
import com.codedream.redis.lock.RedisLockClient;
import com.codedream.redis.ratelimiter.RateLimiter;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@AllArgsConstructor
public class DemoController {

    private final CodeDreamRedis codeDreamRedis;
    private final RedisLockClient redisLockClient;
    private final DemoService demoService;

    @GetMapping
    //限流，每分钟只能获取五次
    @RateLimiter(value = "test", max = 5, ttl = 1, timeUnit = TimeUnit.MINUTES)
    public String get() {
        return codeDreamRedis.get("test");
    }


    @PostMapping
    public Boolean save(@RequestParam String value) {
        codeDreamRedis.set("test", value);
        return Boolean.TRUE;
    }


    @GetMapping("/reentrantLock")
    @RedisLock(lockName = "testLock", waitTime = 10, leaseTime = 60, type = LockType.REENTRANT)
    public String getReentrantLock() {
        return getReentrantLockMethod();
    }


    @RedisLock(lockName = "testLock", waitTime = 10, leaseTime = 60, type = LockType.REENTRANT)
    public String getReentrantLockMethod() {
        return codeDreamRedis.get("test");
    }

    /**
     * 自定义锁
     *
     * @return {@link String}
     */
    @GetMapping("/customLock")
    public String customLock() {
        return redisLockClient.lock("customLock", LockType.REENTRANT, 5, 10, TimeUnit.SECONDS, demoService);
    }


    /**
     * 缓存
     *
     * @return {@link String}
     */
    @GetMapping("/cacheable")
    @Cacheable(cacheNames = "testCacheable#60")
    public String cacheable() {
        return codeDreamRedis.get("test");
    }
}
