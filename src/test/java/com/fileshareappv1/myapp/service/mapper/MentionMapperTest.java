package com.fileshareappv1.myapp.service.mapper;

import static com.fileshareappv1.myapp.domain.MentionAsserts.*;
import static com.fileshareappv1.myapp.domain.MentionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MentionMapperTest {

    private MentionMapper mentionMapper;

    @BeforeEach
    void setUp() {
        mentionMapper = new MentionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMentionSample1();
        var actual = mentionMapper.toEntity(mentionMapper.toDto(expected));
        assertMentionAllPropertiesEquals(expected, actual);
    }
}
