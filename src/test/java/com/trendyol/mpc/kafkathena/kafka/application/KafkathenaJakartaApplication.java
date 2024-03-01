package com.trendyol.mpc.kafkathena.kafka.application;

import com.trendyol.mpc.kafkathena.commons.annotation.EnableKafkathena;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
@ComponentScan(basePackages = "com.trendyol.mpc")
@EnableKafkathena
@EnableConfigurationProperties
@EnableRetry
public class KafkathenaJakartaApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkathenaJakartaApplication.class, args);
    }
}
