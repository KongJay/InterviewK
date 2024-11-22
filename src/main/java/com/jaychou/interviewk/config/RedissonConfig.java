package com.jaychou.interviewk.config;

import io.swagger.models.auth.In;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: RedissonConfig
 * Package: com.jaychou.interviewk.config
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/22 - 19:08
 * @Version: v1.0
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
@Configuration
public class RedissonConfig {
    private String host;
    private Integer port;
    private Integer database;
    private String password;

    @Bean
    public RedissonClient redissionClient() {
        Config config = new Config();
        config.useSingleServer()
                .setPassword(password)
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}
