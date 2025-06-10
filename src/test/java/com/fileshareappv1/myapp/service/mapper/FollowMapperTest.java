package com.fileshareappv1.myapp.service.mapper;

import static com.fileshareappv1.myapp.domain.FollowAsserts.*;
import static com.fileshareappv1.myapp.domain.FollowTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FollowMapperTest {

    private FollowMapper followMapper;

    @BeforeEach
    void setUp() {
        followMapper = new FollowMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFollowSample1();
        var actual = followMapper.toEntity(followMapper.toDto(expected));
        assertFollowAllPropertiesEquals(expected, actual);
    }
}
