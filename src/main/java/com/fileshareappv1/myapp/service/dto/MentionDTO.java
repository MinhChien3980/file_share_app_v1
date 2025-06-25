package com.fileshareappv1.myapp.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.fileshareappv1.myapp.domain.Mention} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MentionDTO implements Serializable {

    private Long id;

    private PostDTO post;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PostDTO getPost() {
        return post;
    }

    public void setPost(PostDTO post) {
        this.post = post;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MentionDTO)) {
            return false;
        }

        MentionDTO mentionDTO = (MentionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, mentionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MentionDTO{" +
            "id=" + getId() +
            ", post=" + getPost() +
            ", user=" + getUser() +
            "}";
    }
}
