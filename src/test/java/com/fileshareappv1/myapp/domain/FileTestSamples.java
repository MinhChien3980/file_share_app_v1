package com.fileshareappv1.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static File getFileSample1() {
        return new File().id(1L).fileName("fileName1").fileUrl("fileUrl1").mimeType("mimeType1").fileSize(1L);
    }

    public static File getFileSample2() {
        return new File().id(2L).fileName("fileName2").fileUrl("fileUrl2").mimeType("mimeType2").fileSize(2L);
    }

    public static File getFileRandomSampleGenerator() {
        return new File()
            .id(longCount.incrementAndGet())
            .fileName(UUID.randomUUID().toString())
            .fileUrl(UUID.randomUUID().toString())
            .mimeType(UUID.randomUUID().toString())
            .fileSize(longCount.incrementAndGet());
    }
}
