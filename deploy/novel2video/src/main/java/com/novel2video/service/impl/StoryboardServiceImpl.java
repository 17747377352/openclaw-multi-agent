package com.novel2video.service.impl;

import com.novel2video.entity.Storyboard;
import com.novel2video.entity.NovelCharacter;
import com.novel2video.mapper.StoryboardMapper;
import com.novel2video.mapper.CharacterMapper;
import com.novel2video.service.StoryboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class StoryboardServiceImpl implements StoryboardService {
    
    @Autowired
    private StoryboardMapper storyboardMapper;
    
    @Autowired
    private CharacterMapper characterMapper;
    
    @Value("${ai.kimi.api-key}")
    private String kimiApiKey;
    
    @Value("${huoshan.api-key}")
    private String huoshanApiKey;
    
    @Value("${huoshan.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String huoshanBaseUrl;
    
    @Value("${huoshan.image-model:doubao-seedream-4-5-251128}")
    private String imageModel;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public List<Storyboard> generateStoryboards(Long groupId, String chapterContent) {
        log.info("生成分镜：groupId={}", groupId);
        List<Storyboard> storyboards = new ArrayList<>();
        Storyboard storyboard = new Storyboard();
        storyboard.setGroupId(groupId);
        storyboard.setSceneNumber(1);
        storyboard.setDescription("从 AI 提取的分镜描述");
        storyboard.setFrameStatus(0);
        storyboard.setIsConfirmed(0);
        storyboards.add(storyboard);
        return storyboards;
    }
    
    @Override
    public String generateStoryboardFrame(Long storyboardId, List<Long> characterIds) {
        log.info("生成分镜首帧图：storyboardId={}", storyboardId);
        Storyboard storyboard = storyboardMapper.selectById(storyboardId);
        if (storyboard == null) throw new RuntimeException("分镜不存在");
        List<NovelCharacter> characters = new ArrayList<>();
        if (characterIds != null && !characterIds.isEmpty()) {
            for (Long charId : characterIds) {
                NovelCharacter character = characterMapper.selectById(charId);
                if (character != null && character.getSeedImageUrl() != null) {
                    characters.add(character);
                }
            }
        }
        StringBuilder prompt = new StringBuilder("电影分镜，高质量，细节丰富，").append(storyboard.getDescription());
        if (!characters.isEmpty()) {
            prompt.append("。参考人物形象：");
            for (int i = 0; i < characters.size(); i++) {
                NovelCharacter c = characters.get(i);
                prompt.append("[").append(c.getName()).append(": ").append(c.getSeedImageUrl()).append("]");
                if (i < characters.size() - 1) prompt.append(", ");
            }
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + huoshanApiKey);
            Map<String, Object> body = new HashMap<>();
            body.put("model", imageModel);
            body.put("prompt", prompt.toString());
            body.put("n", 1);
            body.put("size", "1024x1024");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(huoshanBaseUrl + "/images/generations", entity, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            String imageUrl = (String) data.get(0).get("url");
            storyboard.setFrameImageUrl(imageUrl);
            storyboard.setFrameStatus(2);
            storyboardMapper.update(storyboard);
            return imageUrl;
        } catch (Exception e) {
            log.error("生图失败", e);
            throw new RuntimeException("生图失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<Storyboard> getStoryboardsByGroupId(Long groupId) {
        return storyboardMapper.selectByGroupId(groupId);
    }
    
    @Override
    public void updateStoryboard(Storyboard storyboard) {
        storyboardMapper.update(storyboard);
    }
    
    @Override
    public void confirmStoryboards(List<Long> storyboardIds) {
        for (Long id : storyboardIds) {
            Storyboard storyboard = storyboardMapper.selectById(id);
            if (storyboard != null) {
                storyboard.setIsConfirmed(1);
                storyboardMapper.update(storyboard);
            }
        }
    }
}
