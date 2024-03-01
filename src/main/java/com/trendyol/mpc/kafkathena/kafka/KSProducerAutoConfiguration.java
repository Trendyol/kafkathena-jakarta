package com.trendyol.mpc.kafkathena.kafka;

import com.trendyol.mpc.kafkathena.commons.config.KSCommonConfiguration;
import com.trendyol.mpc.kafkathena.commons.initializer.KafkathenaProducersInitializer;
import com.trendyol.mpc.kafkathena.commons.model.constant.KSConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;


@RequiredArgsConstructor
@Log4j2
@Import(KSCommonConfiguration.class)
@AutoConfiguration(value = KSConstants.KSPRODUCER_CONFIGURATION_BEAN_NAME, before = KafkaAutoConfiguration.class)
@EnableKafka
public class KSProducerAutoConfiguration {
    private final KafkathenaProducersInitializer kafkathenaProducersInitializer;

    @PostConstruct
    public void init() {
        kafkathenaProducersInitializer.initProducers();
    }
}