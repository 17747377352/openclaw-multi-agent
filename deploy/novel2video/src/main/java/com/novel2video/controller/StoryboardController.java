package com.novel2video.controller;

import com.novel2video.common.Result;
import com.novel2video.dto.view.StoryboardView;
import com.novel2video.entity.Storyboard;
import com.novel2video.service.StoryboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/storyboard")
public class StoryboardController {

    @Autowired
    private StoryboardService storyboardService;

    @GetMapping("/group/{groupId}")
    public Result<List<StoryboardView>> getStoryboards(@PathVariable Long groupId) {
        try {
            List<StoryboardView> storyboards = storyboardService.getStoryboardsByGroupId(groupId).stream()
                    .map(this::toStoryboardView)
                    .collect(Collectors.toList());
            return Result.success(storyboards);
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public Result<List<StoryboardView>> generateStoryboards(@RequestParam Long groupId,
                                                            @RequestParam(required = false) String content) {
        try {
            storyboardService.generateStoryboards(groupId, content);
            List<StoryboardView> storyboards = storyboardService.getStoryboardsByGroupId(groupId).stream()
                    .map(this::toStoryboardView)
                    .collect(Collectors.toList());
            return Result.success("分镜生成成功", storyboards);
        } catch (Exception e) {
            log.error("生成失败", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    @PostMapping("/{storyboardId}/frame")
    public Result<String> generateFrame(@PathVariable Long storyboardId,
                                        @RequestBody List<Long> characterIds) {
        try {
            return Result.success("首帧图生成成功", storyboardService.generateStoryboardFrame(storyboardId, characterIds));
        } catch (Exception e) {
            log.error("生成失败", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    @PostMapping("/{storyboardId}")
    public Result<Void> updateStoryboard(@PathVariable Long storyboardId, @RequestBody Storyboard storyboard) {
        try {
            storyboard.setId(storyboardId);
            storyboardService.updateStoryboard(storyboard);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public Result<Void> confirmStoryboards(@RequestBody List<Long> storyboardIds) {
        try {
            storyboardService.confirmStoryboards(storyboardIds);
            return Result.success("确认成功", null);
        } catch (Exception e) {
            log.error("确认失败", e);
            return Result.error("确认失败：" + e.getMessage());
        }
    }

    private StoryboardView toStoryboardView(Storyboard storyboard) {
        StoryboardView view = new StoryboardView();
        view.setId(storyboard.getId());
        view.setGroupId(storyboard.getGroupId());
        view.setSceneNumber(storyboard.getSceneNumber());
        view.setTitle("场景 " + (storyboard.getSceneNumber() != null ? storyboard.getSceneNumber() : ""));
        view.setLocation("未设置");
        view.setDescription(storyboard.getDescription());
        view.setPrompt(storyboard.getPrompt());
        view.setCharacterIds(storyboard.getCharacterIds());
        view.setImageUrl(storyboard.getFrameImageUrl());
        view.setFrameImageUrl(storyboard.getFrameImageUrl());
        view.setFrameStatus(storyboard.getFrameStatus());
        view.setIsConfirmed(storyboard.getIsConfirmed());
        view.setStatus(toUiStatus(storyboard));
        view.setCreatedAt(storyboard.getCreatedAt());
        view.setUpdatedAt(storyboard.getUpdatedAt());
        return view;
    }

    private String toUiStatus(Storyboard storyboard) {
        Integer frameStatus = storyboard.getFrameStatus();
        Integer isConfirmed = storyboard.getIsConfirmed();
        if (frameStatus != null && frameStatus == 1) return "generating";
        if (frameStatus != null && frameStatus == 3) return "rejected";
        if ((frameStatus != null && frameStatus == 2) || (isConfirmed != null && isConfirmed == 1)) return "approved";
        return "pending";
    }
}
