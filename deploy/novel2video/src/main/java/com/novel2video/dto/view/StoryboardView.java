package com.novel2video.dto.view;

import lombok.Data;

import java.util.Date;

@Data
public class StoryboardView {
    private Long id;
    private Long groupId;
    private Integer sceneNumber;
    private String title;
    private String location;
    private String description;
    private String prompt;
    private String characterIds;
    private String imageUrl;
    private String frameImageUrl;
    private Integer frameStatus;
    private Integer isConfirmed;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
