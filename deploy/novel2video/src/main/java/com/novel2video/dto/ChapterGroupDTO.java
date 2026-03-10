package com.novel2video.dto;

import lombok.Data;
import java.util.List;

/**
 * 章节分组 DTO
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class ChapterGroupDTO {
    
    /** 分组 ID */
    private Long groupId;
    
    /** 项目 ID */
    private Long projectId;
    
    /** 分组序号 */
    private Integer groupNumber;
    
    /** 分组名称 */
    private String name;
    
    /** 章节 ID 列表 */
    private List<Long> chapterIds;
    
    /** 起始章节号 */
    private Integer startChapter;
    
    /** 结束章节号 */
    private Integer endChapter;
}
