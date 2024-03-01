package com.trendyol.mpc.kafkathena.kafka.application.model;

public class IgnoredException extends RuntimeException {
    public IgnoredException(String message) {
        super(message);
    }
}
