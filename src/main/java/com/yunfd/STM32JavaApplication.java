package com.yunfd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动类
 * @Description
 * @Author LiuZequan
 * @Date 2022/4/13 10:14
 * @Version 2.0
 */

@Slf4j
@SpringBootApplication
@EnableAsync
public class STM32JavaApplication extends SpringBootServletInitializer {
    public static ConfigurableApplicationContext ac;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(STM32JavaApplication.class);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(STM32JavaApplication.class, args);
        STM32JavaApplication.ac = context;
        ConfigurableEnvironment env = context.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: http://localhost:{}\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"));
    }
}