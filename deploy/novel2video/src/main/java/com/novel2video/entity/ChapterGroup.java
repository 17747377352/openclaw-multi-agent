package com.novel2video.entity;

import lombok.Data;
import java.util.Date;

/**
 * 章节分组实体
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class ChapterGroup {
    
    /** 分组 ID */
    private Long id;
    
    /** 项目 ID */
    private Long projectId;
    
    /** 分组序号 */
    private Integer groupNumber;
    
    /** 分组名称 */
    private String name;
    
    /** 章节 ID 列表，逗号分隔 */
    private String chapterIds;
    
    /** 起始章节号 */
    private Integer startChapter;
    
    /** 结束章节号 */
    private Integer endChapter;
    
    /** 状态：0-待处理 1-人物审核中 2-分镜审核中 3-视频生成中 4-已完成 */
    private Integer status;
    
    /** 创建时间 */
    private Date createdAt;
    
    /** 更新时间 */
    private Date updatedAt;
}
