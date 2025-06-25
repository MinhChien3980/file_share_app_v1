package com.fileshareappv1.myapp.domain;

import static com.fileshareappv1.myapp.domain.FileTestSamples.*;
import static com.fileshareappv1.myapp.domain.PostTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fileshareappv1.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(File.class);
        File file1 = getFileSample1();
        File file2 = new File();
        assertThat(file1).isNotEqualTo(file2);

        file2.setId(file1.getId());
        assertThat(file1).isEqualTo(file2);

        file2 = getFileSample2();
        assertThat(file1).isNotEqualTo(file2);
    }

    @Test
    void postTest() {
        File file = getFileRandomSampleGenerator();
        Post postBack = getPostRandomSampleGenerator();

        file.setPost(postBack);
        assertThat(file.getPost()).isEqualTo(postBack);

        file.post(null);
        assertThat(file.getPost()).isNull();
    }
}
