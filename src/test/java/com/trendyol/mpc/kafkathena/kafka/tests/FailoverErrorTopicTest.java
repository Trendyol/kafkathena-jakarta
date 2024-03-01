package com.trendyol.mpc.kafkathena.kafka.tests;

import com.trendyol.mpc.kafkathena.commons.model.KSConfigurationProperties;
import com.trendyol.mpc.kafkathena.kafka.KSSender;
import com.trendyol.mpc.kafkathena.kafka.application.KafkathenaJakartaApplication;
import com.trendyol.mpc.kafkathena.kafka.application.consumer.json.FailoverErrorTopicErrorTestableConsumer;
import com.trendyol.mpc.kafkathena.kafka.application.consumer.json.FailoverErrorTopicTestableConsumer;
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

import static com.trendyol.mpc.kafkathena.kafka.tests.FailoverErrorTopicTest.ERROR_TOPIC;
import static com.trendyol.mpc.kafkathena.kafka.tests.FailoverErrorTopicTest.TOPIC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = KafkathenaJakartaApplication.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        bootstrapServersProperty = "default.cluster",
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092",
                "auto.create.topics.enable=${kafka.broker.topics-enable:true}"},
        topics = {TOPIC, ERROR_TOPIC})
@Slf4j
@ExtendWith(OutputCaptureExtension.class)
class FailoverErrorTopicTest {
    public static final String TOPIC = "json.consumer.failover.error.topic";
    public static final String ERROR_TOPIC = "json.consumer.failover.error.topic.error";
    public static final String CONSUMER_NAME = "json-consumer-failover-error-topic";

    public static final String ERROR_CONSUMER_NAME = "json-consumer-failover-error-topic-error";
    @Autowired
    KSConfigurationProperties ksConfigurationProperties;
    @Autowired
    private FailoverErrorTopicTestableConsumer consumer;
    @Autowired
    private FailoverErrorTopicErrorTestableConsumer errorConsumer;
    @Autowired
    private KSSender ksSender;

    @Test
    void feature_failover_send_error_topic(CapturedOutput output)
            throws Exception {
        Integer retryCount = ksConfigurationProperties.getConsumers().get(CONSUMER_NAME).getFixedRetry().getRetryCount();
        Long backoffIntervalMillis = ksConfigurationProperties.getConsumers().get(CONSUMER_NAME).getFixedRetry().getBackoffIntervalMillis();
        consumer.resetLatch(retryCount + 1);

        assertThat(consumer.getLatch().getCount()).isEqualTo(retryCount.longValue() + 1);

        Person p = new Person(1L, "Demo", 3);
        KafkaTemplate<String, Object> producer = ksSender.getProducer("default");

        producer.send(TOPIC, p);

        boolean messageConsumed = consumer.getLatch().await(60, TimeUnit.SECONDS);

        log.info("CountdownLatch end count: {}", consumer.getLatch().getCount());
        assertThat(consumer.getLatch().getCount()).isZero();
        assertThat(output.getOut()).doesNotContain("FailoverErrorTopicTestableConsumer consumed with topic");
        // Error Topic Consume
        errorConsumer.resetLatch(1);
        assertThat(errorConsumer.getLatch().getCount()).isEqualTo(1);
        boolean errorConsumed = errorConsumer.getLatch().await(60, TimeUnit.SECONDS);
        assertThat(errorConsumed).isTrue();
        assertThat(output.getOut()).contains("FailoverErrorTopicErrorTestableConsumer consumed with topic");
    }
}

