package com.fileshareappv1.myapp.domain;

import static com.fileshareappv1.myapp.domain.PostTestSamples.*;
import static com.fileshareappv1.myapp.domain.ReactionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reaction.class);
        Reaction reaction1 = getReactionSample1();
        Reaction reaction2 = new Reaction();
        assertThat(reaction1).isNotEqualTo(reaction2);

        reaction2.setId(reaction1.getId());
        assertThat(reaction1).isEqualTo(reaction2);

        reaction2 = getReactionSample2();
        assertThat(reaction1).isNotEqualTo(reaction2);
    }

    @Test
    void postTest() {
        Reaction reaction = getReactionRandomSampleGenerator();
        Post postBack = getPostRandomSampleGenerator();

        reaction.setPost(postBack);
        assertThat(reaction.getPost()).isEqualTo(postBack);

        reaction.post(null);
        assertThat(reaction.getPost()).isNull();
    }
}
