package com.novel2video.dto;

import lombok.Data;

/**
 * 人物提取 DTO
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class NovelCharacterExtractDTO {
    
    /** 项目 ID */
    private Long projectId;
    
    /** 人物名称 */
    private String name;
    
    /** 人物描述 */
    private String description;
    
    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;
    
    /** 角色类型 */
    private String role;
}
