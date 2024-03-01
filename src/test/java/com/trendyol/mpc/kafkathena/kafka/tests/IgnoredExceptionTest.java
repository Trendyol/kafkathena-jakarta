package com.trendyol.mpc.kafkathena.kafka.tests;

import com.trendyol.mpc.kafkathena.commons.model.KSConfigurationProperties;
import com.trendyol.mpc.kafkathena.kafka.KSSender;
import com.trendyol.mpc.kafkathena.kafka.application.KafkathenaJakartaApplication;
import com.trendyol.mpc.kafkathena.kafka.application.consumer.json.IgnoredExceptionTestableConsumer;
import com.trendyol.mpc.kafkathena.kafka.application.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static com.trendyol.mpc.kafkathena.kafka.tests.IgnoredExceptionTest.ERROR_TOPIC;
import static com.trendyol.mpc.kafkathena.kafka.tests.IgnoredExceptionTest.TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = KafkathenaJakartaApplication.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        bootstrapServersProperty = "default.cluster",
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092",
                "auto.create.topics.enable=${kafka.broker.topics-enable:true}"},
        topics = {TOPIC, ERROR_TOPIC})
@Slf4j
@ExtendWith(OutputCaptureExtension.class)
public class IgnoredExceptionTest {
    public static final String TOPIC = "json.consumer.ignored.exception.topic";
    public static final String ERROR_TOPIC = "kafkathena.common.error.topic";
    public static final String CONSUMER_NAME = "json-consumer-ignored-exception";
    @Autowired
    KSConfigurationProperties ksConfigurationProperties;
    @Autowired
    private IgnoredExceptionTestableConsumer consumer;
    @Autowired
    private KSSender ksSender;

    @Test
    void feature_ignore_exception_when_failover(CapturedOutput output)
            throws Exception {
        Integer retryCount = ksConfigurationProperties.getConsumers().get(CONSUMER_NAME).getFixedRetry().getRetryCount();
        Long backoffIntervalMillis = ksConfigurationProperties.getConsumers().get(CONSUMER_NAME).getFixedRetry().getBackoffIntervalMillis();
        consumer.resetLatch(1);
        assertThat(consumer.getLatch().getCount()).isEqualTo(1);

        Person p = new Person(1L, "Demo", 3);
        KafkaTemplate<String, Object> producer = ksSender.getProducer("default");

        log.info("CountdownLatch start count: {}", consumer.getLatch().getCount());

        producer.send(TOPIC, p);

        boolean messageConsumed = consumer.getLatch().await(60, TimeUnit.SECONDS);

        assertAll(
                () -> assertThat(messageConsumed).isTrue(),
                () -> assertThat(consumer.getLatch().getCount()).isZero(),
                () -> assertThat(output.getOut()).doesNotContain("IgnoredExceptionTestableConsumer consumed with topic")
        );
    }
}

