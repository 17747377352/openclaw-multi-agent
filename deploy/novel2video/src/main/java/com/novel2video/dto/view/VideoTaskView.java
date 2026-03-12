package com.novel2video.dto.view;

import lombok.Data;

import java.util.Date;

@Data
public class VideoTaskView {
    private Long id;
    private String taskId;
    private Long groupId;
    private Long storyboardId;
    private String title;
    private String videoUrl;
    private String duration;
    private Integer videoDuration;
    private String resolution;
    private String model;
    private Integer progress;
    private String status;
    private Integer rawStatus;
    private String failReason;
    private Integer retryCount;
    private Date createdAt;
    private Date updatedAt;
}
