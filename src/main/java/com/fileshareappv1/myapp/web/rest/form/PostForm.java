package com.fileshareappv1.myapp.web.rest.form;

import com.fileshareappv1.myapp.domain.enumeration.Privacy;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class PostForm {

    @NotNull
    private String content;

    @NotNull
    private Privacy privacy;

    private String locationName;

    // Spring sẽ bind tất cả các phần form có key = files vào đây
    private List<MultipartFile> files;

    private List<Long> tagIds = new ArrayList<>();

    // getters & setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
