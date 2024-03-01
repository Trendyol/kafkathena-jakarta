package com.trendyol.mpc.kafkathena.kafka.application.consumer.json;

import com.trendyol.mpc.kafkathena.commons.annotation.DependsOnKafkathena;
import com.trendyol.mpc.kafkathena.kafka.application.model.Person;
import com.trendyol.mpc.kafkathena.kafka.application.util.TestableConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@DependsOnKafkathena
@Slf4j
public class FailoverErrorTopicErrorTestableConsumer extends TestableConsumer {

    @KafkaListener(
            topics = "${kafkathena.consumers[json-consumer-failover-error-topic-error].topic}",
            groupId = "${kafkathena.consumers[json-consumer-failover-error-topic-error].props[group.id]}",
            containerFactory = "${kafkathena.consumers[json-consumer-failover-error-topic-error].factory-bean-name}"
    )
    public void consume(@Payload ConsumerRecord<String, Person> payload,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
                        @Header(KafkaHeaders.OFFSET) Long offset,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(value = "throw_exception", required = false) String throwException,
                        @Header(value = "ignored_exception", required = false) String throwIgnoredException) {

        this.runCount.incrementAndGet();
        this.payload = payload;
        this.latch.countDown();

        log.info("{} consumed with topic: {}, and partition: {}, and offset: {}, {}",
                getClass().getSimpleName(),
                topic,
                partition,
                offset,
                payload);
    }
}
