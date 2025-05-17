package com.fileshareappv1.myapp.service.dto;

import com.fileshareappv1.myapp.domain.enumeration.Privacy;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.fileshareappv1.myapp.domain.Post} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PostDTO implements Serializable {

    private Long id;

    @Lob
    private String content;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    private String locationName;

    @DecimalMax(value = "90")
    private BigDecimal locationLat;

    @DecimalMax(value = "180")
    private BigDecimal locationLong;

    @NotNull
    private Privacy privacy;

    private Instant scheduledAt;

    @NotNull
    private Long viewCount;

    @NotNull
    private Long commentCount;

    @NotNull
    private Long shareCount;

    @NotNull
    private Long reactionCount;

    private UserDTO user;

    private Set<TagDTO> tags = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public BigDecimal getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(BigDecimal locationLat) {
        this.locationLat = locationLat;
    }

    public BigDecimal getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(BigDecimal locationLong) {
        this.locationLong = locationLong;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public Long getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(Long reactionCount) {
        this.reactionCount = reactionCount;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Set<TagDTO> getTags() {
        return tags;
    }

    public void setTags(Set<TagDTO> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostDTO)) {
            return false;
        }

        PostDTO postDTO = (PostDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, postDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PostDTO{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", locationName='" + getLocationName() + "'" +
            ", locationLat=" + getLocationLat() +
            ", locationLong=" + getLocationLong() +
            ", privacy='" + getPrivacy() + "'" +
            ", scheduledAt='" + getScheduledAt() + "'" +
            ", viewCount=" + getViewCount() +
            ", commentCount=" + getCommentCount() +
            ", shareCount=" + getShareCount() +
            ", reactionCount=" + getReactionCount() +
            ", user=" + getUser() +
            ", tags=" + getTags() +
            "}";
    }
}
