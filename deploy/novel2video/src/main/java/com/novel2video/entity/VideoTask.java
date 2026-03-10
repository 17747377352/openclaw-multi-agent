package com.novel2video.entity;

import lombok.Data;
import java.util.Date;

/**
 * 视频任务实体
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class VideoTask {
    
    /** 任务 ID */
    private Long id;
    
    /** 分组 ID */
    private Long groupId;
    
    /** 分镜 ID */
    private Long storyboardId;
    
    /** 火山引擎任务 ID */
    private String taskId;
    
    /** 生成完成的视频 OSS 路径 */
    private String videoUrl;
    
    /** 视频时长（秒） */
    private Integer videoDuration;
    
    /** 状态：0-待生成 1-生成中 2-已完成 3-失败 */
    private Integer status;
    
    /** 失败原因 */
    private String failReason;
    
    /** 重试次数 */
    private Integer retryCount;
    
    /** 生成进度百分比 */
    private Integer progress;
    
    /** 创建时间 */
    private Date createdAt;
    
    /** 更新时间 */
    private Date updatedAt;
}
