package com.novel2video.controller;

import com.novel2video.common.Result;
import com.novel2video.dto.view.VideoBatchResult;
import com.novel2video.dto.view.VideoTaskView;
import com.novel2video.entity.ChapterGroup;
import com.novel2video.entity.Storyboard;
import com.novel2video.entity.VideoTask;
import com.novel2video.service.NovelService;
import com.novel2video.service.StoryboardService;
import com.novel2video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private NovelService novelService;

    @Autowired
    private StoryboardService storyboardService;

    @GetMapping("/group/{groupId}")
    public Result<List<VideoTaskView>> getVideoTasks(@PathVariable Long groupId,
                                                     @RequestParam(required = false) Long projectId) {
        try {
            List<VideoTask> tasks = new ArrayList<>();
            if (projectId != null) {
                List<ChapterGroup> groups = novelService.getGroupsByProjectId(projectId);
                for (ChapterGroup group : groups) {
                    tasks.addAll(videoService.getVideoTasksByGroupId(group.getId()));
                }
            } else {
                tasks = videoService.getVideoTasksByGroupId(groupId);
            }

            tasks.sort(Comparator.comparing(VideoTask::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            List<VideoTaskView> views = tasks.stream().map(this::toVideoTaskView).collect(Collectors.toList());
            return Result.success(views);
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<Long> createVideoTask(@RequestParam Long storyboardId) {
        try {
            Long taskId = videoService.createVideoTask(storyboardId);
            return Result.success("视频任务创建成功，正在生成中...", taskId);
        } catch (Exception e) {
            log.error("创建失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public Result<VideoTaskView> getVideoTask(@PathVariable Long taskId) {
        try {
            VideoTask task = videoService.getVideoTask(taskId);
            if (task == null) {
                return Result.error("任务不存在");
            }
            return Result.success(toVideoTaskView(task));
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/{taskId}/retry")
    public Result<Void> retryVideoTask(@PathVariable Long taskId) {
        try {
            videoService.retryVideoTask(taskId);
            return Result.success("重试成功，正在重新生成...", null);
        } catch (Exception e) {
            log.error("重试失败", e);
            return Result.error("重试失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch/{groupId}")
    public Result<VideoBatchResult> batchGenerate(@PathVariable Long groupId) {
        try {
            List<Storyboard> storyboards = storyboardService.getStoryboardsByGroupId(groupId);
            if (storyboards == null || storyboards.isEmpty()) {
                return Result.error("当前分组没有可生成的视频分镜");
            }

            Set<Long> existingStoryboardIds = videoService.getVideoTasksByGroupId(groupId).stream()
                    .map(VideoTask::getStoryboardId)
                    .collect(Collectors.toCollection(HashSet::new));

            int submitted = 0;
            int skipped = 0;
            for (Storyboard storyboard : storyboards) {
                if (storyboard.getId() == null) {
                    skipped++;
                    continue;
                }
                if (existingStoryboardIds.contains(storyboard.getId())) {
                    skipped++;
                    continue;
                }
                if (storyboard.getFrameImageUrl() == null || storyboard.getFrameImageUrl().trim().isEmpty()) {
                    skipped++;
                    continue;
                }

                videoService.createVideoTask(storyboard.getId());
                submitted++;
            }

            VideoBatchResult batchResult = new VideoBatchResult();
            batchResult.setSubmitted(submitted);
            batchResult.setSkipped(skipped);
            return Result.success("批量生成任务已提交", batchResult);
        } catch (Exception e) {
            log.error("批量生成失败", e);
            return Result.error("批量生成失败：" + e.getMessage());
        }
    }

    private VideoTaskView toVideoTaskView(VideoTask task) {
        String uiStatus = toUiStatus(task.getStatus());
        int progress = task.getProgress() != null ? task.getProgress() : 0;
        if ("completed".equals(uiStatus) && progress < 100) {
            progress = 100;
        }

        VideoTaskView view = new VideoTaskView();
        view.setId(task.getId());
        view.setTaskId(task.getTaskId() != null ? task.getTaskId() : "TASK-" + task.getId());
        view.setGroupId(task.getGroupId());
        view.setStoryboardId(task.getStoryboardId());
        view.setTitle("场景 " + (task.getStoryboardId() != null ? task.getStoryboardId() : ""));
        view.setVideoUrl(task.getVideoUrl());
        view.setDuration(task.getVideoDuration() != null ? task.getVideoDuration() + " 秒" : "预计 5 秒");
        view.setVideoDuration(task.getVideoDuration());
        view.setResolution("1080x1920");
        view.setModel("doubao-seedance");
        view.setProgress(progress);
        view.setStatus(uiStatus);
        view.setRawStatus(task.getStatus());
        view.setFailReason(task.getFailReason());
        view.setRetryCount(task.getRetryCount());
        view.setCreatedAt(task.getCreatedAt());
        view.setUpdatedAt(task.getUpdatedAt());
        return view;
    }

    private String toUiStatus(Integer status) {
        if (status == null) return "pending";
        if (status == 2) return "completed";
        if (status == 1) return "processing";
        if (status >= 3) return "failed";
        return "pending";
    }
}
