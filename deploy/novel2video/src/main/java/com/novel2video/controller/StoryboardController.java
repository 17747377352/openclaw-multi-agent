package com.novel2video.controller;

import com.novel2video.entity.Storyboard;
import com.novel2video.service.StoryboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分镜管理 Controller
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping("/api/storyboard")
public class StoryboardController {
    
    @Autowired
    private StoryboardService storyboardService;
    
    /**
     * 获取分镜列表
     */
    @GetMapping("/group/{groupId}")
    public Map<String, Object> getStoryboards(@PathVariable Long groupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Storyboard> storyboards = storyboardService.getStoryboardsByGroupId(groupId);
            result.put("success", true);
            result.put("data", storyboards);
            result.put("count", storyboards.size());
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 生成分镜（AI）
     */
    @PostMapping("/generate")
    public Map<String, Object> generateStoryboards(@RequestParam Long groupId,
                                                    @RequestParam String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Storyboard> storyboards = storyboardService.generateStoryboards(groupId, content);
            result.put("success", true);
            result.put("data", storyboards);
            result.put("message", "分镜生成成功");
        } catch (Exception e) {
            log.error("生成失败", e);
            result.put("success", false);
            result.put("message", "生成失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 生成首帧图
     */
    @PostMapping("/{storyboardId}/frame")
    public Map<String, Object> generateFrame(@PathVariable Long storyboardId,
                                              @RequestBody List<Long> characterIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            String imageUrl = storyboardService.generateStoryboardFrame(storyboardId, characterIds);
            result.put("success", true);
            result.put("imageUrl", imageUrl);
            result.put("message", "首帧图生成成功");
        } catch (Exception e) {
            log.error("生成失败", e);
            result.put("success", false);
            result.put("message", "生成失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 更新分镜
     */
    @PostMapping("/{storyboardId}")
    public Map<String, Object> updateStoryboard(@PathVariable Long storyboardId,
                                                  @RequestBody Storyboard storyboard) {
        Map<String, Object> result = new HashMap<>();
        try {
            storyboard.setId(storyboardId);
            storyboardService.updateStoryboard(storyboard);
            result.put("success", true);
            result.put("message", "更新成功");
        } catch (Exception e) {
            log.error("更新失败", e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 批量确认分镜
     */
    @PostMapping("/confirm")
    public Map<String, Object> confirmStoryboards(@RequestBody List<Long> storyboardIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            storyboardService.confirmStoryboards(storyboardIds);
            result.put("success", true);
            result.put("message", "确认成功");
        } catch (Exception e) {
            log.error("确认失败", e);
            result.put("success", false);
            result.put("message", "确认失败：" + e.getMessage());
        }
        return result;
    }
}
