package com.novel2video.service.impl;

import com.novel2video.entity.NovelCharacter;
import com.novel2video.mapper.CharacterMapper;
import com.novel2video.service.CharacterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class CharacterServiceImpl implements CharacterService {
    
    @Autowired
    private CharacterMapper characterMapper;
    
    @Value("${ai.kimi.api-key}")
    private String kimiApiKey;
    
    @Value("${ai.kimi.base-url:https://api.moonshot.cn/v1}")
    private String kimiBaseUrl;
    
    @Value("${huoshan.api-key}")
    private String huoshanApiKey;
    
    @Value("${huoshan.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String huoshanBaseUrl;
    
    @Value("${huoshan.image-model:doubao-seedream-4-5-251128}")
    private String imageModel;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public List<NovelCharacter> extractCharacters(Long projectId, String novelContent) {
        log.info("提取人物：projectId={}", projectId);
        List<NovelCharacter> characters = new ArrayList<>();
        NovelCharacter character = new NovelCharacter();
        character.setProjectId(projectId);
        character.setName("示例人物");
        character.setDescription("从 AI 提取的描述");
        character.setGender(1);
        character.setRole("主角");
        character.setSeedStatus(0);
        character.setIsConfirmed(0);
        characters.add(character);
        return characters;
    }
    
    @Override
    public String generateCharacterImage(Long characterId) {
        log.info("生成人物图：characterId={}", characterId);
        NovelCharacter character = characterMapper.selectById(characterId);
        if (character == null) throw new RuntimeException("人物不存在");
        String description = character.getUserEditedDescription() != null ? character.getUserEditedDescription() : character.getDescription();
        String prompt = "角色设计图，全身像，正面视角，高清细节，" + character.getName() + "：" + description;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + huoshanApiKey);
            Map<String, Object> body = new HashMap<>();
            body.put("model", imageModel);
            body.put("prompt", prompt);
            body.put("n", 1);
            body.put("size", "1024x1024");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(huoshanBaseUrl + "/images/generations", entity, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            String imageUrl = (String) data.get(0).get("url");
            character.setSeedImageUrl(imageUrl);
            character.setSeedPrompt(prompt);
            character.setSeedStatus(2);
            characterMapper.update(character);
            return imageUrl;
        } catch (Exception e) {
            log.error("豆包生图失败", e);
            throw new RuntimeException("生图失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<NovelCharacter> getCharactersByProjectId(Long projectId) {
        return characterMapper.selectByProjectId(projectId);
    }
    
    @Override
    public void updateCharacter(NovelCharacter character) {
        characterMapper.update(character);
    }
    
    @Override
    public void confirmCharacters(List<Long> characterIds) {
        characterMapper.batchUpdateConfirmStatus(characterIds, 1);
    }
}
