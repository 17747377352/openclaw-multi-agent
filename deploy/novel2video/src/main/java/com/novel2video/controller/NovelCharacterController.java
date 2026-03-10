package com.novel2video.controller;

import com.novel2video.entity.NovelCharacter;
import com.novel2video.service.CharacterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人物管理 Controller
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping("/api/character")
public class NovelCharacterController {
    
    @Autowired
    private CharacterService characterService;
    
    /**
     * 获取人物列表
     */
    @GetMapping("/project/{projectId}")
    public Map<String, Object> getCharacters(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<NovelCharacter> characters = characterService.getCharactersByProjectId(projectId);
            result.put("success", true);
            result.put("data", characters);
            result.put("count", characters.size());
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 提取人物（AI）
     */
    @PostMapping("/extract")
    public Map<String, Object> extractCharacters(@RequestParam Long projectId,
                                                  @RequestParam String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<NovelCharacter> characters = characterService.extractCharacters(projectId, content);
            result.put("success", true);
            result.put("data", characters);
            result.put("message", "人物提取成功");
        } catch (Exception e) {
            log.error("提取失败", e);
            result.put("success", false);
            result.put("message", "提取失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 生成人物图
     */
    @PostMapping("/{characterId}/image")
    public Map<String, Object> generateImage(@PathVariable Long characterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String imageUrl = characterService.generateCharacterImage(characterId);
            result.put("success", true);
            result.put("imageUrl", imageUrl);
            result.put("message", "人物图生成成功");
        } catch (Exception e) {
            log.error("生成失败", e);
            result.put("success", false);
            result.put("message", "生成失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 更新人物信息
     */
    @PostMapping("/{characterId}")
    public Map<String, Object> updateCharacter(@PathVariable Long characterId,
                                                @RequestBody NovelCharacter character) {
        Map<String, Object> result = new HashMap<>();
        try {
            character.setId(characterId);
            characterService.updateCharacter(character);
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
     * 批量确认人物
     */
    @PostMapping("/confirm")
    public Map<String, Object> confirmCharacters(@RequestBody List<Long> characterIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            characterService.confirmCharacters(characterIds);
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
