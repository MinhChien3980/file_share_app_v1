package com.fileshareappv1.myapp.domain;

import static com.fileshareappv1.myapp.domain.FavoriteTestSamples.*;
import static com.fileshareappv1.myapp.domain.PostTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FavoriteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Favorite.class);
        Favorite favorite1 = getFavoriteSample1();
        Favorite favorite2 = new Favorite();
        assertThat(favorite1).isNotEqualTo(favorite2);

        favorite2.setId(favorite1.getId());
        assertThat(favorite1).isEqualTo(favorite2);

        favorite2 = getFavoriteSample2();
        assertThat(favorite1).isNotEqualTo(favorite2);
    }

    @Test
    void postTest() {
        Favorite favorite = getFavoriteRandomSampleGenerator();
        Post postBack = getPostRandomSampleGenerator();

        favorite.setPost(postBack);
        assertThat(favorite.getPost()).isEqualTo(postBack);

        favorite.post(null);
        assertThat(favorite.getPost()).isNull();
    }
}
