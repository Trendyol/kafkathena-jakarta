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

import java.util.concurrent.CountDownLatch;

@Component
@DependsOnKafkathena
@Slf4j
public class StraightTestableConsumer extends TestableConsumer {

    @KafkaListener(
            topics = "${kafkathena.consumers[json-consumer].topic}",
            groupId = "${kafkathena.consumers[json-consumer].props[group.id]}",
            containerFactory = "${kafkathena.consumers[json-consumer].factory-bean-name}"
    )
    public void consume(@Payload ConsumerRecord<String, Person> payload,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
                        @Header(KafkaHeaders.OFFSET) Long offset,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(value = "throw_exception", required = false) String throwException,
                        @Header(value = "ignored_exception", required = false) String throwIgnoredException) {

        this.runCount.incrementAndGet();
        log.info("{} consumed with topic: {}, and partition: {}, and offset: {}, {}",
                getClass().getSimpleName(),
                topic,
                partition,
                offset,
                payload);
        this.payload = payload;
        latch.countDown();
        log.info("Payload consumed successfully.");
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }
}
