package com.fileshareappv1.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShareDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShareDTO.class);
        ShareDTO shareDTO1 = new ShareDTO();
        shareDTO1.setId(1L);
        ShareDTO shareDTO2 = new ShareDTO();
        assertThat(shareDTO1).isNotEqualTo(shareDTO2);
        shareDTO2.setId(shareDTO1.getId());
        assertThat(shareDTO1).isEqualTo(shareDTO2);
        shareDTO2.setId(2L);
        assertThat(shareDTO1).isNotEqualTo(shareDTO2);
        shareDTO1.setId(null);
        assertThat(shareDTO1).isNotEqualTo(shareDTO2);
    }
}
