package com.fileshareappv1.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MentionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MentionDTO.class);
        MentionDTO mentionDTO1 = new MentionDTO();
        mentionDTO1.setId(1L);
        MentionDTO mentionDTO2 = new MentionDTO();
        assertThat(mentionDTO1).isNotEqualTo(mentionDTO2);
        mentionDTO2.setId(mentionDTO1.getId());
        assertThat(mentionDTO1).isEqualTo(mentionDTO2);
        mentionDTO2.setId(2L);
        assertThat(mentionDTO1).isNotEqualTo(mentionDTO2);
        mentionDTO1.setId(null);
        assertThat(mentionDTO1).isNotEqualTo(mentionDTO2);
    }
}
