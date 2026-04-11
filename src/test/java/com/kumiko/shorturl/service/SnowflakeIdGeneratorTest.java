package com.kumiko.shorturl.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

public class SnowflakeIdGeneratorTest {
    @Test
    void shouldGeneratePositiveId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, System::currentTimeMillis);
        long id = generator.nextId();
        Assertions.assertTrue(id > 0);
    }

    @Test
    void shouldGenerateUniqueId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, System::currentTimeMillis);
        HashSet<Long> idSet = new HashSet<>();
        for (int i = 0; i<10000;++i){
            long id = generator.nextId();
            idSet.add(id);
        }
        Assertions.assertEquals(10000, idSet.size());
    }

}
