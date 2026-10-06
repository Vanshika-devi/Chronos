package com.chronos.scheduler;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SequenceGenerator {

    private final AtomicLong sequence =
            new AtomicLong(0);

    public long next() {
        return sequence.incrementAndGet();
    }
}