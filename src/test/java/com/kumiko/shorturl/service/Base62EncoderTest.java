package com.kumiko.shorturl.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Base62EncoderTest {
    private static final long[] TEST_IDS = {1L, 62L, 100000000L, 35875969759711232L,
            35875969759711233L,
            35875969759711234L,
            35875969759711235L,
            35875969759711236L,
    };

    @Test
    void testMinimumEncodedStringLength() {
        for (long testId : TEST_IDS) {
            Assertions.assertTrue(Base62Encoder.encode(testId).length() >= 7);
        }
    }

    @Test
    void testContainOnlyCharsetChars() {
        for (long testId : TEST_IDS) {
            String encoded = Base62Encoder.encode(testId);
            Assertions.assertTrue(encoded.matches("[0-9a-zA-Z]+"),
                    "Invalid encoded string: " + encoded);
        }
    }

    @Test
    void testEncodeDeterminism() {
        for (long testId : TEST_IDS) {
            String encoded = Base62Encoder.encode(testId);
            String encoded2 = Base62Encoder.encode(testId);
            Assertions.assertEquals(encoded, encoded2);
        }
    }

    @Test
    void testInputToOutput() {
        long in = 62L;
        String out = "0000010";
        Assertions.assertEquals(out, Base62Encoder.encode(in));
    }

}
