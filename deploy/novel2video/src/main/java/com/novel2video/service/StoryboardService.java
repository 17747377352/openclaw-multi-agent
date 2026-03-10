package com.novel2video.service;

import com.novel2video.entity.Storyboard;
import com.novel2video.dto.StoryboardGenerateDTO;

import java.util.List;

/**
 * 分镜服务 - 处理分镜生成、提示词组装
 * 
 * @author developer
 * @since 2026-03-10
 */
public interface StoryboardService {

    /**
     * 使用 Kimi AI 为章节分组生成分镜
     * 
     * @param groupId 分组 ID
     * @param chapterContent 章节内容
     * @return 分镜列表
     */
    List<Storyboard> generateStoryboards(Long groupId, String chapterContent);

    /**
     * 生成首帧图（使用火山豆包生图）
     * 
     * @param storyboardId 分镜 ID
     * @param characterIds 涉及的人物 ID 列表
     * @return 首帧图 URL
     */
    String generateStoryboardFrame(Long storyboardId, List<Long> characterIds);

    /**
     * 获取分镜列表
     */
    List<Storyboard> getStoryboardsByGroupId(Long groupId);

    /**
     * 更新分镜
     */
    void updateStoryboard(Storyboard storyboard);

    /**
     * 批量确认分镜
     */
    void confirmStoryboards(List<Long> storyboardIds);
}
