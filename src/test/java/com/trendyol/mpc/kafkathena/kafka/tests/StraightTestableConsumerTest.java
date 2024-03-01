package com.trendyol.mpc.kafkathena.kafka.tests;

import com.trendyol.mpc.kafkathena.commons.model.KSConfigurationProperties;
import com.trendyol.mpc.kafkathena.kafka.KSSender;
import com.trendyol.mpc.kafkathena.kafka.application.KafkathenaJakartaApplication;
import com.trendyol.mpc.kafkathena.kafka.application.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.concurrent.TimeUnit;

import static com.trendyol.mpc.kafkathena.kafka.tests.StraightTestableConsumerTest.ERROR_TOPIC;
import static com.trendyol.mpc.kafkathena.kafka.tests.StraightTestableConsumerTest.TOPIC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = KafkathenaJakartaApplication.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        bootstrapServersProperty = "default.cluster",
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092",
                "auto.create.topics.enable=${kafka.broker.topics-enable:true}"},
        topics = {TOPIC, ERROR_TOPIC})
@ExtendWith(OutputCaptureExtension.class)
@EnabledIf(value = "#{{'integration-tests'}.contains(environment.getActiveProfiles()[0])}")
public class StraightTestableConsumerTest {
    public static final String TOPIC = "json.consumer.topic";
    public static final String ERROR_TOPIC = "kafkathena.common.error.topic";
    public static final String CONSUMER_NAME = "json-consumer";
    @Autowired
    private com.trendyol.mpc.kafkathena.kafka.application.consumer.json.StraightTestableConsumer consumer;

    @Autowired
    private KSSender ksSender;

    @Autowired
    private KSConfigurationProperties ksConfigurationProperties;

    @Test
    void should_consume_data_when_send_data_with_no_failover(CapturedOutput output)
            throws Exception {
        Integer retryCount = ksConfigurationProperties.getConsumers().get(CONSUMER_NAME).getFixedRetry().getRetryCount();
        Long backoffIntervalMillis = ksConfigurationProperties.getConsumers().get(CONSUMER_NAME).getFixedRetry().getBackoffIntervalMillis();
        consumer.resetLatch(retryCount + 1);

        assertThat(consumer.getLatch().getCount()).isEqualTo(retryCount.longValue() + 1);
        Person p = new Person(1L, "Demo", 3);
        KafkaTemplate<String, Object> producer = ksSender.getProducer("default");
        producer.send(TOPIC, p);

        boolean messageConsumed = consumer.getLatch().await(2, TimeUnit.SECONDS);
        assertThat(consumer.getRunCount().get()).isEqualTo(1);
        assertThat(output.getOut()).contains("Payload consumed successfully.");
    }
}

