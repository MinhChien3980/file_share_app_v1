package com.fileshareappv1.myapp.domain;

import static com.fileshareappv1.myapp.domain.FollowTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FollowTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Follow.class);
        Follow follow1 = getFollowSample1();
        Follow follow2 = new Follow();
        assertThat(follow1).isNotEqualTo(follow2);

        follow2.setId(follow1.getId());
        assertThat(follow1).isEqualTo(follow2);

        follow2 = getFollowSample2();
        assertThat(follow1).isNotEqualTo(follow2);
    }
}
