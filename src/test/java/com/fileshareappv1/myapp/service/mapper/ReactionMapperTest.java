package com.fileshareappv1.myapp.service.mapper;

import static com.fileshareappv1.myapp.domain.ReactionAsserts.*;
import static com.fileshareappv1.myapp.domain.ReactionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReactionMapperTest {

    private ReactionMapper reactionMapper;

    @BeforeEach
    void setUp() {
        reactionMapper = new ReactionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getReactionSample1();
        var actual = reactionMapper.toEntity(reactionMapper.toDto(expected));
        assertReactionAllPropertiesEquals(expected, actual);
    }
}
