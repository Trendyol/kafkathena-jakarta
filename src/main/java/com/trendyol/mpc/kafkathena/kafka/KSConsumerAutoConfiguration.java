package com.trendyol.mpc.kafkathena.kafka;

import com.trendyol.mpc.kafkathena.commons.config.KSCommonConfiguration;
import com.trendyol.mpc.kafkathena.commons.initializer.KafkathenaConsumersInitializer;
import com.trendyol.mpc.kafkathena.commons.model.constant.KSConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;


@RequiredArgsConstructor
@Log4j2
@AutoConfiguration(value = KSConstants.KSCONSUMER_CONFIGURATION_BEAN_NAME, before = KafkaAutoConfiguration.class)
@DependsOn(KSConstants.KSPRODUCER_CONFIGURATION_BEAN_NAME)
@Import(KSCommonConfiguration.class)
@EnableKafka
public class KSConsumerAutoConfiguration {
    private final KafkathenaConsumersInitializer kafkathenaConsumersInitializer;

    @PostConstruct
    public void init() {
        kafkathenaConsumersInitializer.initConsumers();
    }
}


