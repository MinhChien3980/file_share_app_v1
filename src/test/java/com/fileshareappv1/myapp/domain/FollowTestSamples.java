package com.fileshareappv1.myapp.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FollowTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Follow getFollowSample1() {
        return new Follow().id(1L);
    }

    public static Follow getFollowSample2() {
        return new Follow().id(2L);
    }

    public static Follow getFollowRandomSampleGenerator() {
        return new Follow().id(longCount.incrementAndGet());
    }
}
