package com.novel2video.entity;

import lombok.Data;
import java.util.Date;

/**
 * 章节实体
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class Chapter {
    
    /** 章节 ID */
    private Long id;
    
    /** 项目 ID */
    private Long projectId;
    
    /** 章节序号 */
    private Integer chapterNumber;
    
    /** 章节标题 */
    private String title;
    
    /** 章节内容 */
    private String content;
    
    /** 字数 */
    private Integer wordCount;
    
    /** 所属分组 ID */
    private Long groupId;
    
    /** 创建时间 */
    private Date createdAt;
    
    /** 更新时间 */
    private Date updatedAt;
}
