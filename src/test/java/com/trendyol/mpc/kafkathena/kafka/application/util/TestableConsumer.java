package com.trendyol.mpc.kafkathena.kafka.application.util;

import com.trendyol.mpc.kafkathena.kafka.application.model.Person;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class TestableConsumer {
    public AtomicInteger runCount = new AtomicInteger(0);
    public CountDownLatch latch = new CountDownLatch(0);
    public ConsumerRecord<String, Person> payload;

    public void resetLatch(int retry) {
        this.latch = new CountDownLatch(retry);
    }
}
