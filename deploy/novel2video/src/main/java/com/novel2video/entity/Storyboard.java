package com.novel2video.entity;

import lombok.Data;
import java.util.Date;

/**
 * 分镜实体
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class Storyboard {
    
    /** 分镜 ID */
    private Long id;
    
    /** 分组 ID */
    private Long groupId;
    
    /** 分镜序号 */
    private Integer sceneNumber;
    
    /** 分镜描述 */
    private String description;
    
    /** AI 生成的提示词（含人物链接） */
    private String prompt;
    
    /** 涉及人物 ID 列表，逗号分隔 */
    private String characterIds;
    
    /** 首帧图 OSS 路径 */
    private String frameImageUrl;
    
    /** 首帧图状态：0-待生成 1-生成中 2-已完成 3-失败 */
    private Integer frameStatus;
    
    /** 是否已确认：0-否 1-是 */
    private Integer isConfirmed;
    
    /** 创建时间 */
    private Date createdAt;
    
    /** 更新时间 */
    private Date updatedAt;
}
