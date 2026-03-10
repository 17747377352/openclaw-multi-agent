package com.novel2video.mapper;

import com.novel2video.entity.VideoTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频任务 Mapper
 * 
 * @author developer
 * @since 2026-03-10
 */
@Mapper
public interface VideoTaskMapper {
    
    /**
     * 插入任务
     */
    int insert(VideoTask task);
    
    /**
     * 根据分组 ID 查询
     */
    List<VideoTask> selectByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 根据任务 ID 查询
     */
    VideoTask selectByTaskId(@Param("taskId") String taskId);
    
    /**
     * 根据 ID 查询
     */
    VideoTask selectById(@Param("id") Long id);
    
    /**
     * 更新任务
     */
    int update(VideoTask task);
    
    /**
     * 查询待轮询的任务
     */
    List<VideoTask> selectPendingTasks();
}
