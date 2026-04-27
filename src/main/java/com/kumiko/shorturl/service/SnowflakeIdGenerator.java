package com.kumiko.shorturl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.LongSupplier;

@Component
public class SnowflakeIdGenerator {
    // since 2026/1/1
    static final long EPOCH = 1767225600000L;
    static final int TIMESTAMP_BITS = 41;
    static final int MACHINE_ID_BITS = 10;
    static final int SEQUENCE_BITS = 12;
    static final int MACHINE_ID_SHIFT = 12;
    static final int TIME_STAMP_SHIFT = 22;
    static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);

    long machineId;
    long lastTimestamp = 0L;
    long sequenceNum = 0L;

    private final LongSupplier clock;

    @Autowired
    public SnowflakeIdGenerator(@Value("${snowflake.machine-id}") long machineId) {
        this(machineId, System::currentTimeMillis);
    }

    public SnowflakeIdGenerator(long machineId, LongSupplier clock) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Invalid machine ID");
        }
        this.machineId = machineId;
        this.clock = clock;
    }

    public synchronized long nextId() {
        long currentTimestamp = clock.getAsLong() - EPOCH;
        if (currentTimestamp == lastTimestamp) {
            sequenceNum++;
            if (sequenceNum > MAX_SEQUENCE) {
                while (clock.getAsLong() - EPOCH == currentTimestamp) {

                }
                currentTimestamp = clock.getAsLong() - EPOCH;
                sequenceNum = 0;
            }

        } else if (currentTimestamp > lastTimestamp) {
            sequenceNum = 0;
        } else {
            throw new IllegalStateException("Clock moved backwards");
        }
        lastTimestamp = currentTimestamp;
        return (currentTimestamp << TIME_STAMP_SHIFT) | (machineId << MACHINE_ID_SHIFT) | sequenceNum;
    }


}


