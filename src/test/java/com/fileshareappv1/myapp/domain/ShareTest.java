package com.fileshareappv1.myapp.domain;

import static com.fileshareappv1.myapp.domain.PostTestSamples.*;
import static com.fileshareappv1.myapp.domain.ShareTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShareTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Share.class);
        Share share1 = getShareSample1();
        Share share2 = new Share();
        assertThat(share1).isNotEqualTo(share2);

        share2.setId(share1.getId());
        assertThat(share1).isEqualTo(share2);

        share2 = getShareSample2();
        assertThat(share1).isNotEqualTo(share2);
    }

    @Test
    void postTest() {
        Share share = getShareRandomSampleGenerator();
        Post postBack = getPostRandomSampleGenerator();

        share.setPost(postBack);
        assertThat(share.getPost()).isEqualTo(postBack);

        share.post(null);
        assertThat(share.getPost()).isNull();
    }
}
