package io.anyway.bigbang.example;

import io.anyway.bigbang.example.base.RedisContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Profile("test")
public class Session7_RedisContainerTest {
    @ClassRule
    public static final RedisContainer redis = new RedisContainer();
    private static final String listName = "kg.users";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeClass
    public static void setUp() {
        System.setProperty("spring.redis.host", redis.getContainerIpAddress());
        System.setProperty("spring.redis.port", String.valueOf(redis.getMappedPort(6379)));
    }

    @AfterClass
    public static void tearDown() {
        System.clearProperty("spring.redis.host");
        System.clearProperty("spring.redis.port");
    }

    @Test
    public void shouldConnectToRedis() {
        Long size = redisTemplate.opsForList().size(listName);
        assertThat(size).isZero();

        redisTemplate.opsForList().leftPush(listName, "sean");

        size = redisTemplate.opsForList().size(listName);
        assertThat(size).isOne();

        Object element = redisTemplate.opsForList().leftPop(listName);
        assertThat(element).isEqualTo("sean");
    }


}
