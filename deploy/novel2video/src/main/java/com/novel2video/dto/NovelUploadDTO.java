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
    
    /** 创建用户 ID */
    private Long userId;
}
