package com.fileshareappv1.myapp.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FavoriteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Favorite getFavoriteSample1() {
        return new Favorite().id(1L);
    }

    public static Favorite getFavoriteSample2() {
        return new Favorite().id(2L);
    }

    public static Favorite getFavoriteRandomSampleGenerator() {
        return new Favorite().id(longCount.incrementAndGet());
    }
}
