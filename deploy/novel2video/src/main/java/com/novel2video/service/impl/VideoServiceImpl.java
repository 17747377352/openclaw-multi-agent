package com.novel2video.service.impl;

import com.volcengine.ark.runtime.model.content.generation.*;
import com.volcengine.ark.runtime.service.ArkService;
import com.novel2video.entity.Storyboard;
import com.novel2video.entity.VideoTask;
import com.novel2video.mapper.VideoTaskMapper;
import com.novel2video.mapper.StoryboardMapper;
import com.novel2video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 视频服务实现 - 火山引擎视频生成
 */
@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    
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
    
    private ArkService arkService;
    
    @Autowired
    public void initArkService() {
        okhttp3.ConnectionPool connectionPool = new okhttp3.ConnectionPool(5, 1, TimeUnit.SECONDS);
        okhttp3.Dispatcher dispatcher = new okhttp3.Dispatcher();
        this.arkService = ArkService.builder()
            .dispatcher(dispatcher)
            .connectionPool(connectionPool)
            .apiKey(apiKey)
            .build();
    }
    
    @Override
    public Long createVideoTask(Long storyboardId) {
        log.info("创建视频任务：storyboardId={}", storyboardId);
        Storyboard storyboard = storyboardMapper.selectById(storyboardId);
        if (storyboard == null || storyboard.getFrameImageUrl() == null) {
            throw new RuntimeException("分镜不存在或首帧图未生成");
        }
        VideoTask task = new VideoTask();
        task.setStoryboardId(storyboardId);
        task.setGroupId(storyboard.getGroupId());
        task.setStatus(0);
        task.setProgress(0);
        task.setRetryCount(0);
        videoTaskMapper.insert(task);
        generateVideoAsync(task.getId(), storyboard);
        return task.getId();
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
    public void retryVideoTask(Long taskId) {
        VideoTask task = videoTaskMapper.selectById(taskId);
        if (task == null || task.getRetryCount() >= 3) {
            throw new RuntimeException("任务不存在或已达到最大重试次数");
        }
        task.setStatus(0);
        task.setRetryCount(task.getRetryCount() + 1);
        task.setFailReason(null);
        videoTaskMapper.update(task);
        Storyboard storyboard = storyboardMapper.selectById(task.getStoryboardId());
        generateVideoAsync(taskId, storyboard);
    }
    
    private void generateVideoAsync(Long taskId, Storyboard storyboard) {
        new Thread(() -> {
            try {
                String volcanoTaskId = createContentGenerationTask(storyboard);
                VideoTask task = videoTaskMapper.selectById(taskId);
                task.setTaskId(volcanoTaskId);
                task.setStatus(1);
                videoTaskMapper.update(task);
                pollTaskStatus(taskId, volcanoTaskId);
            } catch (Exception e) {
                log.error("视频生成失败：taskId={}", taskId, e);
                VideoTask task = videoTaskMapper.selectById(taskId);
                if (task != null) {
                    task.setStatus(3);
                    task.setFailReason(e.getMessage());
                    videoTaskMapper.update(task);
                }
            }
        }).start();
    }
    
    private String createContentGenerationTask(Storyboard storyboard) {
        List<CreateContentGenerationTaskRequest.Content> contents = new ArrayList<>();
        contents.add(CreateContentGenerationTaskRequest.Content.builder()
            .type("text")
            .text(storyboard.getPrompt() + " --duration 10 --camerafixed false --watermark true")
            .build());
        contents.add(CreateContentGenerationTaskRequest.Content.builder()
            .type("image_url")
            .imageUrl(CreateContentGenerationTaskRequest.ImageUrl.builder()
                .url(storyboard.getFrameImageUrl())
                .build())
            .build());
        CreateContentGenerationTaskRequest request = CreateContentGenerationTaskRequest.builder()
            .model(videoModel)
            .content(contents)
            .build();
        return arkService.createContentGenerationTask(request).getId();
    }
    
    private void pollTaskStatus(Long taskId, String volcanoTaskId) {
        GetContentGenerationTaskRequest request = GetContentGenerationTaskRequest.builder()
            .taskId(volcanoTaskId)
            .build();
        int maxAttempts = 100;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                GetContentGenerationTaskResponse response = arkService.getContentGenerationTask(request);
                String status = response.getStatus();
                log.info("视频任务状态：taskId={}, status={}", taskId, status);
                if ("succeeded".equalsIgnoreCase(status)) {
                    VideoTask task = videoTaskMapper.selectById(taskId);
                    if (task != null) {
                        task.setStatus(2);
                        task.setProgress(100);
                        videoTaskMapper.update(task);
                    }
                    log.info("视频生成成功：taskId={}", taskId);
                    break;
                } else if ("failed".equalsIgnoreCase(status)) {
                    VideoTask task = videoTaskMapper.selectById(taskId);
                    if (task != null) {
                        task.setStatus(3);
                        task.setFailReason("火山 API 返回失败");
                        videoTaskMapper.update(task);
                    }
                    log.error("视频生成失败：taskId={}", taskId);
                    break;
                } else {
                    TimeUnit.SECONDS.sleep(3);
                    attempts++;
                    VideoTask task = videoTaskMapper.selectById(taskId);
                    if (task != null) {
                        task.setProgress(Math.min(90, attempts * 3));
                        videoTaskMapper.update(task);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("轮询被中断：taskId={}", taskId, e);
                break;
            } catch (Exception e) {
                log.error("轮询失败：taskId={}", taskId, e);
                attempts++;
            }
        }
    }
}
