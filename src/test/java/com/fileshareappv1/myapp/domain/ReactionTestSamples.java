package com.fileshareappv1.myapp.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ReactionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Reaction getReactionSample1() {
        return new Reaction().id(1L);
    }

    public static Reaction getReactionSample2() {
        return new Reaction().id(2L);
    }

    public static Reaction getReactionRandomSampleGenerator() {
        return new Reaction().id(longCount.incrementAndGet());
    }
}
