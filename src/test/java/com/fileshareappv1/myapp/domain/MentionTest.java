package com.fileshareappv1.myapp.domain;

import static com.fileshareappv1.myapp.domain.MentionTestSamples.*;
import static com.fileshareappv1.myapp.domain.PostTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MentionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Mention.class);
        Mention mention1 = getMentionSample1();
        Mention mention2 = new Mention();
        assertThat(mention1).isNotEqualTo(mention2);

        mention2.setId(mention1.getId());
        assertThat(mention1).isEqualTo(mention2);

        mention2 = getMentionSample2();
        assertThat(mention1).isNotEqualTo(mention2);
    }

    @Test
    void postTest() {
        Mention mention = getMentionRandomSampleGenerator();
        Post postBack = getPostRandomSampleGenerator();

        mention.setPost(postBack);
        assertThat(mention.getPost()).isEqualTo(postBack);

        mention.post(null);
        assertThat(mention.getPost()).isNull();
    }
}
