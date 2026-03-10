package com.novel2video.dto;

import lombok.Data;
import java.util.List;

/**
 * 分镜生成 DTO
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class StoryboardGenerateDTO {
    
    /** 分组 ID */
    private Long groupId;
    
    /** 分镜序号 */
    private Integer sceneNumber;
    
    /** 分镜描述 */
    private String description;
    
    /** 涉及人物 ID 列表 */
    private List<Long> characterIds;
    
    /** 提示词 */
    private String prompt;
}
