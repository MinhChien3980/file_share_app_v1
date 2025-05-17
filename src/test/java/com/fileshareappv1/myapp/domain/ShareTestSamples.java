package com.fileshareappv1.myapp.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ShareTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Share getShareSample1() {
        return new Share().id(1L);
    }

    public static Share getShareSample2() {
        return new Share().id(2L);
    }

    public static Share getShareRandomSampleGenerator() {
        return new Share().id(longCount.incrementAndGet());
    }
}
