package com.novel2video.controller;

import com.novel2video.common.Result;
import com.novel2video.dto.view.CharacterView;
import com.novel2video.entity.NovelCharacter;
import com.novel2video.service.CharacterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/character")
public class NovelCharacterController {

    @Autowired
    private CharacterService characterService;

    @GetMapping("/project/{projectId}")
    public Result<List<CharacterView>> getCharacters(@PathVariable Long projectId) {
        try {
            List<CharacterView> characters = characterService.getCharactersByProjectId(projectId).stream()
                    .map(this::toCharacterView)
                    .collect(Collectors.toList());
            return Result.success(characters);
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/extract")
    public Result<List<CharacterView>> extractCharacters(@RequestParam Long projectId,
                                                         @RequestParam(required = false) String content) {
        try {
            List<CharacterView> characters = characterService.extractCharacters(projectId, content).stream()
                    .map(this::toCharacterView)
                    .collect(Collectors.toList());
            return Result.success("人物提取成功", characters);
        } catch (Exception e) {
            log.error("提取失败", e);
            return Result.error("提取失败：" + e.getMessage());
        }
    }

    @PostMapping("/{characterId}/image")
    public Result<String> generateImage(@PathVariable Long characterId) {
        try {
            return Result.success("人物图生成成功", characterService.generateCharacterImage(characterId));
        } catch (Exception e) {
            log.error("生成失败", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    @PostMapping("/{characterId}")
    public Result<Void> updateCharacter(@PathVariable Long characterId, @RequestBody NovelCharacter character) {
        try {
            character.setId(characterId);
            characterService.updateCharacter(character);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public Result<Void> confirmCharacters(@RequestBody List<Long> characterIds) {
        try {
            characterService.confirmCharacters(characterIds);
            return Result.success("确认成功", null);
        } catch (Exception e) {
            log.error("确认失败", e);
            return Result.error("确认失败：" + e.getMessage());
        }
    }

    private CharacterView toCharacterView(NovelCharacter character) {
        CharacterView view = new CharacterView();
        view.setId(character.getId());
        view.setProjectId(character.getProjectId());
        view.setName(character.getName());
        view.setRole(character.getRole());
        view.setDescription(character.getDescription());
        view.setImageUrl(character.getSeedImageUrl());
        view.setSeedImageUrl(character.getSeedImageUrl());
        view.setSeedStatus(character.getSeedStatus());
        view.setIsConfirmed(character.getIsConfirmed());
        view.setStatus(toUiStatus(character));
        view.setCreatedAt(character.getCreatedAt());
        view.setUpdatedAt(character.getUpdatedAt());
        return view;
    }

    private String toUiStatus(NovelCharacter character) {
        Integer seedStatus = character.getSeedStatus();
        Integer isConfirmed = character.getIsConfirmed();
        if (seedStatus != null && seedStatus == 1) return "generating";
        if (seedStatus != null && seedStatus == 3) return "rejected";
        if ((seedStatus != null && seedStatus == 2) || (isConfirmed != null && isConfirmed == 1)) return "approved";
        return "pending";
    }
}
