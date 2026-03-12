package com.novel2video.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel2video.entity.Storyboard;
import com.novel2video.entity.VideoTask;
import com.novel2video.mapper.StoryboardMapper;
import com.novel2video.mapper.VideoTaskMapper;
import com.novel2video.service.VideoService;
import com.novel2video.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * 视频服务实现 - 火山引擎视频生成
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    private static final String REQUEST_MODEL_HEADER = "X-Request-Model";
    private static final String CONTENT_TASKS_PATH = "/contents/generations/tasks";
    
    @Autowired
    private VideoTaskMapper videoTaskMapper;
    
    @Autowired
    private StoryboardMapper storyboardMapper;
    
    @Value("${huoshan.api-key}")
    private String apiKey;
    
    @Value("${huoshan.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;
    
    @Value("${huoshan.video-model:doubao-seedance-1-5-pro-251215}")
    private String videoModel;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    // 轮询间隔（秒）
    private static final int POLL_INTERVAL_SECONDS = 5;
    // 最大轮询次数
    private static final int MAX_POLL_ATTEMPTS = 120; // 10 分钟
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVideoTask(Long storyboardId) {
        log.info("创建视频任务：storyboardId={}", storyboardId);
        
        Storyboard storyboard = storyboardMapper.selectById(storyboardId);
        if (storyboard == null) {
            throw new RuntimeException("分镜不存在：storyboardId=" + storyboardId);
        }
        
        if (storyboard.getFrameImageUrl() == null || storyboard.getFrameImageUrl().isEmpty()) {
            throw new RuntimeException("分镜首帧图未生成，无法创建视频任务");
        }
        
        // 创建任务记录
        VideoTask task = new VideoTask();
        task.setStoryboardId(storyboardId);
        task.setGroupId(storyboard.getGroupId());
        task.setStatus(0); // 待处理
        task.setProgress(0);
        task.setRetryCount(0);
        videoTaskMapper.insert(task);
        
        Long taskId = task.getId();
        log.info("视频任务创建成功：taskId={}", taskId);
        
        // 异步执行视频生成
        generateVideoAsync(taskId, storyboard);
        
        return taskId;
    }
    
    /**
     * 异步生成视频
     */
    private void generateVideoAsync(Long taskId, Storyboard storyboard) {
        executorService.submit(() -> {
            try {
                log.info("开始生成视频：taskId={}", taskId);
                
                // 更新状态为生成中
                VideoTask task = videoTaskMapper.selectById(taskId);
                if (task != null) {
                    task.setStatus(1); // 生成中
                    videoTaskMapper.update(task);
                }
                
                // 创建火山引擎视频生成任务
                String volcanoTaskId = createVideoGenerationTask(storyboard);
                log.info("火山任务创建成功：taskId={}, volcanoTaskId={}", taskId, volcanoTaskId);
                
                // 更新任务 ID
                task = videoTaskMapper.selectById(taskId);
                if (task != null) {
                    task.setTaskId(volcanoTaskId);
                    videoTaskMapper.update(task);
                }
                
                // 轮询任务状态
                pollTaskStatus(taskId, volcanoTaskId);
                
            } catch (Exception e) {
                log.error("视频生成失败：taskId={}", taskId, e);
                handleTaskFailure(taskId, e.getMessage());
            }
        });
    }
    
    /**
     * 创建火山引擎视频生成任务
     */
    private String createVideoGenerationTask(Storyboard storyboard) {
        log.info("创建火山视频生成任务：storyboardId={}", storyboard.getId());
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", videoModel);
        
        // 构建内容数组
        List<Map<String, Object>> content = new ArrayList<>();
        
        // 文本提示词
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        String prompt = storyboard.getPrompt() != null && !storyboard.getPrompt().isEmpty()
            ? storyboard.getPrompt()
            : storyboard.getDescription();
        textContent.put("text", prompt + " --duration 10 --camerafixed false --watermark true");
        content.add(textContent);
        
        // 首帧图
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", storyboard.getFrameImageUrl());
        imageContent.put("image_url", imageUrl);
        content.add(imageContent);
        
        requestBody.put("content", content);
        
        try {
            Map<String, String> customHeaders = new HashMap<>();
            customHeaders.put(REQUEST_MODEL_HEADER, videoModel);

            Map<String, Object> response = HttpUtil.postWithRetry(
                baseUrl + CONTENT_TASKS_PATH,
                apiKey,
                requestBody,
                customHeaders,
                Map.class,
                3
            );
            
            // 解析响应，获取任务 ID
            String volcanoTaskId = extractTaskId(response);
            if (volcanoTaskId == null || volcanoTaskId.isEmpty()) {
                throw new RuntimeException("火山 API 未返回任务 ID");
            }
            
            return volcanoTaskId;
            
        } catch (Exception e) {
            log.error("创建火山视频任务失败", e);
            throw new RuntimeException("创建视频任务失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 从响应中提取任务 ID
     */
    private String extractTaskId(Map<String, Object> response) {
        try {
            JsonNode rootNode = objectMapper.valueToTree(response);
            
            // 尝试不同的字段路径
            String taskId = rootNode.path("id").asText(null);
            if (taskId != null && !taskId.isEmpty()) {
                return taskId;
            }
            
            taskId = rootNode.path("task_id").asText(null);
            if (taskId != null && !taskId.isEmpty()) {
                return taskId;
            }
            
            taskId = rootNode.path("data").path("task_id").asText(null);
            if (taskId != null && !taskId.isEmpty()) {
                return taskId;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("解析任务 ID 失败", e);
            return null;
        }
    }
    
    /**
     * 轮询任务状态
     */
    private void pollTaskStatus(Long taskId, String volcanoTaskId) {
        log.info("开始轮询视频任务状态：taskId={}, volcanoTaskId={}", taskId, volcanoTaskId);
        
        int attempts = 0;
        
        while (attempts < MAX_POLL_ATTEMPTS) {
            try {
                // 等待间隔
                Thread.sleep(POLL_INTERVAL_SECONDS * 1000);
                
                // 查询任务状态
                Map<String, Object> statusResponse = getTaskStatus(volcanoTaskId);
                String status = extractTaskStatus(statusResponse);
                
                log.info("视频任务状态：taskId={}, status={}, attempt={}/{}", 
                    taskId, status, attempts + 1, MAX_POLL_ATTEMPTS);
                
                // 更新进度
                VideoTask task = videoTaskMapper.selectById(taskId);
                if (task != null) {
                    int progress = Math.min(90, (attempts + 1) * 100 / MAX_POLL_ATTEMPTS);
                    task.setProgress(progress);
                    videoTaskMapper.update(task);
                }
                
                // 检查状态
                if ("succeeded".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                    // 获取视频 URL
                    String videoUrl = extractVideoUrl(statusResponse);
                    handleTaskSuccess(taskId, videoUrl);
                    return;
                }
                
                if ("failed".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status)) {
                    String failReason = extractFailReason(statusResponse);
                    handleTaskFailure(taskId, failReason != null ? failReason : "火山 API 返回失败");
                    return;
                }
                
                attempts++;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("轮询被中断：taskId={}", taskId, e);
                handleTaskFailure(taskId, "任务被中断");
                return;
            } catch (Exception e) {
                log.error("轮询失败：taskId={}, attempt={}", taskId, attempts, e);
                attempts++;
                
                // 如果连续失败多次，终止任务
                if (attempts >= 10) {
                    handleTaskFailure(taskId, "轮询失败次数过多：" + e.getMessage());
                    return;
                }
            }
        }
        
        // 超时
        handleTaskFailure(taskId, "视频生成超时，超过最大轮询次数");
    }
    
    /**
     * 获取任务状态
     */
    private Map<String, Object> getTaskStatus(String volcanoTaskId) {
        log.debug("查询火山任务状态：volcanoTaskId={}", volcanoTaskId);

        try {
            String encodedTaskId = URLEncoder.encode(volcanoTaskId, StandardCharsets.UTF_8);
            return HttpUtil.getWithRetry(
                baseUrl + CONTENT_TASKS_PATH + "/" + encodedTaskId,
                apiKey,
                Map.class,
                3
            );
        } catch (Exception e) {
            log.error("查询任务状态失败：volcanoTaskId={}", volcanoTaskId, e);
            throw new RuntimeException("查询状态失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 从响应中提取任务状态
     */
    private String extractTaskStatus(Map<String, Object> response) {
        try {
            JsonNode rootNode = objectMapper.valueToTree(response);
            
            String status = rootNode.path("status").asText(null);
            if (status != null && !status.isEmpty()) {
                return status;
            }
            
            status = rootNode.path("data").path("status").asText(null);
            if (status != null && !status.isEmpty()) {
                return status;
            }
            
            return "unknown";
            
        } catch (Exception e) {
            log.error("解析任务状态失败", e);
            return "unknown";
        }
    }
    
    /**
     * 从响应中提取视频 URL
     */
    private String extractVideoUrl(Map<String, Object> response) {
        try {
            JsonNode rootNode = objectMapper.valueToTree(response);
            
            // 尝试不同的字段路径
            String url = rootNode.path("video_url").asText(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }
            
            url = rootNode.path("data").path("video_url").asText(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }

            url = rootNode.path("content").path("video_url").asText(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }
            
            url = rootNode.path("data").path("output").path("video_url").asText(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }

            url = rootNode.path("content").path("file_url").asText(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("解析视频 URL 失败", e);
            return null;
        }
    }
    
    /**
     * 从响应中提取失败原因
     */
    private String extractFailReason(Map<String, Object> response) {
        try {
            JsonNode rootNode = objectMapper.valueToTree(response);
            
            String reason = rootNode.path("error").path("message").asText(null);
            if (reason != null && !reason.isEmpty()) {
                return reason;
            }
            
            reason = rootNode.path("data").path("error_message").asText(null);
            if (reason != null && !reason.isEmpty()) {
                return reason;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("解析失败原因失败", e);
            return null;
        }
    }
    
    /**
     * 处理任务成功
     */
    @Transactional(rollbackFor = Exception.class)
    private void handleTaskSuccess(Long taskId, String videoUrl) {
        log.info("视频生成成功：taskId={}, videoUrl={}", taskId, videoUrl);
        
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus(2); // 已完成
            task.setProgress(100);
            task.setVideoUrl(videoUrl);
            videoTaskMapper.update(task);
        }
    }
    
    /**
     * 处理任务失败
     */
    @Transactional(rollbackFor = Exception.class)
    private void handleTaskFailure(Long taskId, String failReason) {
        log.error("视频生成失败：taskId={}, reason={}", taskId, failReason);
        
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task != null) {
            // 检查是否可以重试
            if (task.getRetryCount() < MAX_RETRY_COUNT) {
                task.setStatus(3); // 失败（可重试）
            } else {
                task.setStatus(4); // 失败（已达最大重试次数）
            }
            task.setFailReason(failReason);
            videoTaskMapper.update(task);
        }
    }
    
    @Override
    public VideoTask getVideoTask(Long taskId) {
        return videoTaskMapper.selectById(taskId);
    }
    
    @Override
    public List<VideoTask> getVideoTasksByGroupId(Long groupId) {
        return videoTaskMapper.selectByGroupId(groupId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryVideoTask(Long taskId) {
        log.info("重试视频任务：taskId={}", taskId);
        
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在：taskId=" + taskId);
        }
        
        if (task.getRetryCount() >= MAX_RETRY_COUNT) {
            throw new RuntimeException("任务已达到最大重试次数：" + MAX_RETRY_COUNT);
        }
        
        // 重置任务状态
        task.setStatus(0); // 待处理
        task.setRetryCount(task.getRetryCount() + 1);
        task.setFailReason(null);
        task.setProgress(0);
        videoTaskMapper.update(task);
        
        // 获取分镜信息
        Storyboard storyboard = storyboardMapper.selectById(task.getStoryboardId());
        if (storyboard == null) {
            throw new RuntimeException("分镜不存在：storyboardId=" + task.getStoryboardId());
        }
        
        // 重新执行视频生成
        generateVideoAsync(taskId, storyboard);
        
        log.info("视频任务重试已启动：taskId={}, retryCount={}", taskId, task.getRetryCount());
    }
    
    /**
     * 定时任务：检查并恢复卡住的任务
     * 每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void checkStuckTasks() {
        log.debug("检查卡住的视频任务...");
        
        try {
            List<VideoTask> pendingTasks = videoTaskMapper.selectPendingTasks();
            
            for (VideoTask task : pendingTasks) {
                // 检查任务是否超时（超过 30 分钟）
                long elapsed = System.currentTimeMillis() - task.getCreatedAt().getTime();
                if (elapsed > 30 * 60 * 1000) { // 30 分钟
                    log.warn("检测到卡住的任务：taskId={}, elapsed={}ms", task.getId(), elapsed);
                    
                    if (task.getRetryCount() < MAX_RETRY_COUNT) {
                        log.info("自动重试卡住的任务：taskId={}", task.getId());
                        retryVideoTask(task.getId());
                    } else {
                        log.error("任务卡住且已达最大重试次数：taskId={}", task.getId());
                        handleTaskFailure(task.getId(), "任务超时且已达最大重试次数");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("检查卡住任务失败", e);
        }
    }
}
