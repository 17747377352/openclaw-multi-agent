package com.novel2video.entity;

import lombok.Data;
import java.util.Date;

/**
 * 小说项目实体
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class NovelProject {
    
    /** 项目 ID */
    private Long id;
    
    /** 小说标题 */
    private String title;
    
    /** 作者 */
    private String author;
    
    /** 原始文件 OSS 路径 */
    private String originalFilePath;
    
    /** 总章节数 */
    private Integer totalChapters;
    
    /** 总字数 */
    private Long totalWords;
    
    /** 状态：0-草稿 1-处理中 2-人物审核中 3-分镜审核中 4-视频生成中 5-已完成 9-已废弃 */
    private Integer status;
    
    /** 创建用户 ID */
    private Long userId;
    
    /** 创建时间 */
    private Date createdAt;
    
    /** 更新时间 */
    private Date updatedAt;
}
