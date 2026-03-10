package com.novel2video.controller;

import com.novel2video.entity.VideoTask;
import com.novel2video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频任务管理 Controller
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoController {
    
    @Autowired
    private VideoService videoService;
    
    /**
     * 获取视频任务列表
     */
    @GetMapping("/group/{groupId}")
    public Map<String, Object> getVideoTasks(@PathVariable Long groupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<VideoTask> tasks = videoService.getVideoTasksByGroupId(groupId);
            result.put("success", true);
            result.put("data", tasks);
            result.put("count", tasks.size());
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 创建视频任务
     */
    @PostMapping("/create")
    public Map<String, Object> createVideoTask(@RequestParam Long storyboardId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long taskId = videoService.createVideoTask(storyboardId);
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("message", "视频任务创建成功，正在生成中...");
        } catch (Exception e) {
            log.error("创建失败", e);
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public Map<String, Object> getVideoTask(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            VideoTask task = videoService.getVideoTask(taskId);
            result.put("success", true);
            result.put("data", task);
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 重试视频任务
     */
    @PostMapping("/{taskId}/retry")
    public Map<String, Object> retryVideoTask(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            videoService.retryVideoTask(taskId);
            result.put("success", true);
            result.put("message", "重试成功，正在重新生成...");
        } catch (Exception e) {
            log.error("重试失败", e);
            result.put("success", false);
            result.put("message", "重试失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 批量生成视频（按分组）
     */
    @PostMapping("/batch/{groupId}")
    public Map<String, Object> batchGenerate(@PathVariable Long groupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // TODO: 实现批量生成逻辑
            result.put("success", true);
            result.put("message", "批量生成任务已提交");
        } catch (Exception e) {
            log.error("批量生成失败", e);
            result.put("success", false);
            result.put("message", "批量生成失败：" + e.getMessage());
        }
        return result;
    }
}
