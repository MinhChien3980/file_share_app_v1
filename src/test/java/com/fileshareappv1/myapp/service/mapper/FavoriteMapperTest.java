package com.fileshareappv1.myapp.service.mapper;

import static com.fileshareappv1.myapp.domain.FavoriteAsserts.*;
import static com.fileshareappv1.myapp.domain.FavoriteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FavoriteMapperTest {

    private FavoriteMapper favoriteMapper;

    @BeforeEach
    void setUp() {
        favoriteMapper = new FavoriteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFavoriteSample1();
        var actual = favoriteMapper.toEntity(favoriteMapper.toDto(expected));
        assertFavoriteAllPropertiesEquals(expected, actual);
    }
}
