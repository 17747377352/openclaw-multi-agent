package com.novel2video.dto.view;

import lombok.Data;

import java.util.Date;

@Data
public class CharacterView {
    private Long id;
    private Long projectId;
    private String name;
    private String role;
    private String description;
    private String imageUrl;
    private String seedImageUrl;
    private Integer seedStatus;
    private Integer isConfirmed;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
