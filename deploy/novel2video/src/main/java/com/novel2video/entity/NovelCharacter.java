package com.novel2video.entity;

import lombok.Data;
import java.util.Date;

/**
 * 人物实体
 * 
 * @author developer
 * @since 2026-03-10
 */
@Data
public class NovelCharacter {
    
    /** 人物 ID */
    private Long id;
    
    /** 项目 ID */
    private Long projectId;
    
    /** 人物名称 */
    private String name;
    
    /** AI 提取的人物描述 */
    private String description;
    
    /** 用户编辑后的人物描述 */
    private String userEditedDescription;
    
    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;
    
    /** 角色类型：主角/配角/反派等 */
    private String role;
    
    /** 人物标准照 OSS 路径 */
    private String seedImageUrl;
    
    /** 生图提示词 */
    private String seedPrompt;
    
    /** 生图状态：0-待生成 1-生成中 2-已完成 3-失败 */
    private Integer seedStatus;
    
    /** 是否已确认：0-否 1-是 */
    private Integer isConfirmed;
    
    /** 创建时间 */
    private Date createdAt;
    
    /** 更新时间 */
    private Date updatedAt;
}
