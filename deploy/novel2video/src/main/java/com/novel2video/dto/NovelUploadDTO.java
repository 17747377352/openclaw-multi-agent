package com.novel2video.dto;

import lombok.Data;

/**
 * 小说上传 DTO
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class NovelUploadDTO {
    
    /** 小说标题 */
    private String title;
    
    /** 作者 */
    private String author;
    
    /** 文件 OSS 路径 */
    private String filePath;

    /** 直接上传的小说正文内容（文本模式） */
    private String content;

    /** 原始文件名（文本模式下可选） */
    private String fileName;

    /** 项目描述（前端扩展字段，可选） */
    private String description;
    
    /** 创建用户 ID */
    private Long userId;
}
