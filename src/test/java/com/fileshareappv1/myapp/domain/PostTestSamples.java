package com.fileshareappv1.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PostTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Post getPostSample1() {
        return new Post().id(1L).locationName("locationName1").viewCount(1L).commentCount(1L).shareCount(1L).reactionCount(1L);
    }

    public static Post getPostSample2() {
        return new Post().id(2L).locationName("locationName2").viewCount(2L).commentCount(2L).shareCount(2L).reactionCount(2L);
    }

    public static Post getPostRandomSampleGenerator() {
        return new Post()
            .id(longCount.incrementAndGet())
            .locationName(UUID.randomUUID().toString())
            .viewCount(longCount.incrementAndGet())
            .commentCount(longCount.incrementAndGet())
            .shareCount(longCount.incrementAndGet())
            .reactionCount(longCount.incrementAndGet());
    }
}
