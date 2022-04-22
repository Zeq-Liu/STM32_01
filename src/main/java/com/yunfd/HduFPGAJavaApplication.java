package com.yunfd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动
 *
 * @Author yunfd
 */
@Slf4j
@SpringBootApplication
@EnableAsync
public class HduFPGAJavaApplication extends SpringBootServletInitializer {
  public static ConfigurableApplicationContext ac;

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(HduFPGAJavaApplication.class);
  }

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(HduFPGAJavaApplication.class, args);
    HduFPGAJavaApplication.ac = context;
    ConfigurableEnvironment env = context.getEnvironment();
    log.info("\n----------------------------------------------------------\n\t" +
                    "Application '{}' is running! Access URLs:\n\t" +
                    "Local: \t\thttp://localhost:{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            env.getProperty("server.port"));
  }
}