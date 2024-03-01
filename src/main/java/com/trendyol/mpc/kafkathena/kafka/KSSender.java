package com.trendyol.mpc.kafkathena.kafka;

import com.trendyol.mpc.kafkathena.commons.handler.KSSenderDelegate;
import com.trendyol.mpc.kafkathena.commons.initializer.KafkathenaProducersInitializer;
import com.trendyol.mpc.kafkathena.commons.model.constant.KSHeader;
import com.trendyol.mpc.kafkathena.commons.model.exception.KSProduceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Kafkathena Sender Utility
 *
 * @author sercan.celenk
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KSSender implements KSSenderDelegate {
    private static final String SEND_ERROR_LOG_MESSAGE = "Kafkathena: Sending Kafka Message is failed with the following exception: {}, topic: {}, key: {}, message: {} ";
    private final KafkathenaProducersInitializer kafkathenaProducersInitializer;

    @Override
    public <T> void send(String producerName, String topic, String key, T message) {
        try {
            KafkaTemplate<String, Object> producer = getProducer(producerName);
            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topic, key, message);
            producer.send(producerRecord)
                    .handle((result, throwable) -> {
                        if (Optional.ofNullable(throwable).isPresent()) {
                            log.error("Sending Kafka Message is failed with the following exception: {}, topic: {}, key: {}, message: {} ", throwable.getMessage(), topic, key, message);
                            throw KSProduceException.builder().cause(throwable).build();
                        }

                        log.debug("Sending Kafka Message is successful with the following topic: {}, key: {}, message: {} ", topic, key, message);
                        return result;
                    }).get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {//NOSONAR
            log.error(SEND_ERROR_LOG_MESSAGE, ex.getMessage(), topic, key, message, ex);
            throw KSProduceException.builder().cause(ex).build();
        }
    }

    @SuppressWarnings("all")
    @Override
    public <T> void send(String producerName, ProducerRecord<String, T> record) {
        try {
            KafkaTemplate<String, Object> producer = getProducer(producerName);

            producer.send((ProducerRecord<String, Object>) record)
                    .handle((result, throwable) -> {
                        if (Optional.ofNullable(throwable).isPresent()) {
                            log.error("Kafkathena: Sending Kafka Message is failed with the following exception: {}, Message: {} ", throwable.getMessage(), record);
                            throw KSProduceException.builder().cause(throwable).build();
                        }

                        log.debug("Kafkathena: Sending Kafka Message is successful with the following message: {} ", record);
                        return result;
                    }).get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {//NOSONAR
            log.error(SEND_ERROR_LOG_MESSAGE, ex.getMessage(), record.topic(), record.key(), record.value(), ex);
            throw KSProduceException.builder().cause(ex).build();
        }
    }

    /**
     * Send data with default header key
     * Default header key : "filter_key"
     *
     * @param topic
     * @param key
     * @param value
     * @param filterHeaderValue
     */
    @Override
    public void send(String producerName, String topic, String key, Object value, String filterHeaderValue) {
        send(producerName, topic, key, value, KSHeader.FILTER_KEY.getHeaderKeyName(), filterHeaderValue);
    }

    @SuppressWarnings("all")
    @Override
    public void send(String producerName, String topic, String key, Object value, String filterHeaderKey, String filterHeaderValue) {
        try {
            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topic, key, value);
            Optional.ofNullable(filterHeaderValue)
                    .filter(StringUtils::hasText)
                    .ifPresent(fk -> producerRecord.headers().add(filterHeaderKey, fk.getBytes(StandardCharsets.UTF_8)));

            KafkaTemplate<String, Object> producer = getProducer(producerName);

            producer.send(producerRecord)
                    .handle((result, throwable) -> {
                        if (Optional.ofNullable(throwable).isPresent()) {
                            log.error(SEND_ERROR_LOG_MESSAGE, ExceptionUtils.getRootCauseMessage(throwable), topic, key, value, throwable);
                            throw KSProduceException.builder().cause(throwable).build();
                        }

                        log.debug("Sending Kafka Message is successful with the following topic: {}, key: {}, message: {} ", topic, key, value);
                        return result;
                    }).get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {//NOSONAR
            log.error(SEND_ERROR_LOG_MESSAGE, ExceptionUtils.getRootCauseMessage(ex), topic, key, value, ex);
            throw KSProduceException.builder().cause(ex).build();
        }
    }

    @SuppressWarnings("all")
    @Override
    public KafkaTemplate<String, Object> getProducer(String producerName) {
        return kafkathenaProducersInitializer.getProducer(producerName.concat("_kt_producer"));
    }

    @SuppressWarnings("all")
    @Override
    public boolean checkProducer(String producerName) {
        return kafkathenaProducersInitializer.getProducerMap().containsKey(producerName.concat("_kt_producer"));
    }
}
