package com.fileshareappv1.myapp.service.mapper;

import static com.fileshareappv1.myapp.domain.TagAsserts.*;
import static com.fileshareappv1.myapp.domain.TagTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TagMapperTest {

    private TagMapper tagMapper;

    @BeforeEach
    void setUp() {
        tagMapper = new TagMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTagSample1();
        var actual = tagMapper.toEntity(tagMapper.toDto(expected));
        assertTagAllPropertiesEquals(expected, actual);
    }
}
