package com.trendyol.mpc.kafkathena.kafka;

import com.trendyol.mpc.kafkathena.commons.model.exception.KSProduceException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KSSenderTest {
    String payload = "payload";
    String producerName = "default";
    String topic = "topic";
    String key = "key";
    @InjectMocks
    private KSSender ksSender;

    @Test
    void it_should_send() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        doReturn(CompletableFuture.completedFuture(new SendResult(null, null))).when(template).send(any(ProducerRecord.class));
        doReturn(template).when(spy).getProducer(producerName);

        spy.send(producerName, topic, key, payload);

        verify(template).send(any(ProducerRecord.class));
    }

    @Test
    void it_should_not_send_due_to_exception_() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        CompletableFuture<SendResult<?, ?>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException());
        doReturn(future).when(template).send(any(ProducerRecord.class));
        doReturn(template).when(spy).getProducer(producerName);

        Throwable throwable = catchThrowable(() -> spy.send(producerName, topic, key, payload));
        verify(template).send(any(ProducerRecord.class));
        assertThat(throwable).isInstanceOf(KSProduceException.class);
    }

    @Test
    void it_should_not_send_due_to_exception() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        doThrow(RuntimeException.class).when(template).send(any(ProducerRecord.class));
        doReturn(template).when(spy).getProducer(producerName);

        Throwable throwable = catchThrowable(() -> spy.send(producerName, topic, key, payload));
        verify(template).send(any(ProducerRecord.class));
        assertThat(throwable).isInstanceOf(KSProduceException.class);
    }

    @Test
    void it_should_send_v2() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        ProducerRecord<String, String> producerRecord = Mockito.mock(ProducerRecord.class);
        doReturn(CompletableFuture.completedFuture(new SendResult(null, null))).when(template).send(any(ProducerRecord.class));
        doReturn(template).when(spy).getProducer(producerName);

        spy.send(producerName, producerRecord);

        verify(template).send(any(ProducerRecord.class));
    }

    @Test
    void it_should_not_send_due_to_exception_v2() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        CompletableFuture<SendResult<?, ?>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException());
        ProducerRecord<String, String> producerRecord = Mockito.mock(ProducerRecord.class);
        doReturn(future).when(template).send(any(ProducerRecord.class));
        doReturn(template).when(spy).getProducer(producerName);

        Throwable throwable = catchThrowable(() -> spy.send(producerName, producerRecord));
        verify(template).send(any(ProducerRecord.class));
        assertThat(throwable).isInstanceOf(KSProduceException.class);
    }

    @Test
    void it_should_not_send_due_to_exception__v2() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        doThrow(RuntimeException.class).when(template).send(any(ProducerRecord.class));
        ProducerRecord<String, String> producerRecord = Mockito.mock(ProducerRecord.class);
        doReturn(template).when(spy).getProducer(producerName);

        Throwable throwable = catchThrowable(() -> spy.send(producerName, producerRecord));
        verify(template).send(any(ProducerRecord.class));
        assertThat(throwable).isInstanceOf(KSProduceException.class);
    }

    @Test
    void it_should_send_with_filter_header() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        doReturn(CompletableFuture.completedFuture(new SendResult(null, null))).when(template).send(any(ProducerRecord.class));
        doReturn(template).when(spy).getProducer(producerName);
        String filterHeaderKey = "filter-header-key";
        String filterHeaderValue = "filter-header-value";
        spy.send(producerName, topic, key, payload, filterHeaderKey, filterHeaderValue);

        verify(template).send(any(ProducerRecord.class));
    }

    @Test
    void it_should_throw_with_filter_header() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        doReturn(template).when(spy).getProducer(producerName);
        doThrow(RuntimeException.class).when(template).send(any(ProducerRecord.class));
        String filterHeaderKey = "filter-header-key";
        String filterHeaderValue = "filter-header-value";

        Throwable throwable = catchThrowable(() -> spy.send(producerName, topic, key, payload, filterHeaderKey, filterHeaderValue));
        verify(template).send(any(ProducerRecord.class));
        assertThat(throwable).isInstanceOf(KSProduceException.class);
    }

    @Test
    void it_should_throw_with_filter_header_with_futured_exception() {
        KSSender spy = Mockito.spy(ksSender);
        KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
        doReturn(template).when(spy).getProducer(producerName);
        CompletableFuture<SendResult<?, ?>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException());
        doReturn(future).when(template).send(any(ProducerRecord.class));
        String filterHeaderKey = "filter-header-key";
        String filterHeaderValue = "filter-header-value";

        Throwable throwable = catchThrowable(() -> spy.send(producerName, topic, key, payload, filterHeaderKey, filterHeaderValue));
        verify(template).send(any(ProducerRecord.class));
        assertThat(throwable).isInstanceOf(KSProduceException.class);
    }


}