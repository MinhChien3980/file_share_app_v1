package com.fileshareappv1.myapp.service.mapper;

import static com.fileshareappv1.myapp.domain.ShareAsserts.*;
import static com.fileshareappv1.myapp.domain.ShareTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShareMapperTest {

    private ShareMapper shareMapper;

    @BeforeEach
    void setUp() {
        shareMapper = new ShareMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getShareSample1();
        var actual = shareMapper.toEntity(shareMapper.toDto(expected));
        assertShareAllPropertiesEquals(expected, actual);
    }
}
