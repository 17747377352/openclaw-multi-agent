package com.novel2video.service;

import com.novel2video.entity.VideoTask;

import java.util.List;

/**
 * 视频服务 - 处理视频生成、状态轮询
 * 
 * @author developer
 * @since 2026-03-10
 */
public interface VideoService {

    /**
     * 创建视频生成任务
     * 
     * @param storyboardId 分镜 ID
     * @return 任务 ID
     */
    Long createVideoTask(Long storyboardId);

    /**
     * 获取视频任务详情
     */
    VideoTask getVideoTask(Long taskId);

    /**
     * 获取分组的视频任务列表
     */
    List<VideoTask> getVideoTasksByGroupId(Long groupId);

    /**
     * 重试视频任务
     */
    void retryVideoTask(Long taskId);
}
