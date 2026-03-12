package com.novel2video.dto.view;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectView {
    private Long id;
    private String title;
    private String author;
    private Integer status;
    private Integer rawStatus;
    private Integer chapterCount;
    private Integer totalChapters;
    private Long totalWords;
    private Integer progress;
    private Integer characterCount;
    private Integer storyboardCount;
    private Date createdAt;
    private Date updatedAt;
    private String originalFilePath;
}
