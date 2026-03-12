package com.novel2video.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel2video.entity.Chapter;
import com.novel2video.entity.ChapterGroup;
import com.novel2video.entity.NovelCharacter;
import com.novel2video.entity.Storyboard;
import com.novel2video.mapper.ChapterGroupMapper;
import com.novel2video.mapper.ChapterMapper;
import com.novel2video.mapper.CharacterMapper;
import com.novel2video.mapper.StoryboardMapper;
import com.novel2video.service.StoryboardService;
import com.novel2video.util.AnimePromptUtil;
import com.novel2video.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 分镜服务实现 - 处理分镜生成、提示词组装
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@Service
public class StoryboardServiceImpl implements StoryboardService {
    
    @Autowired
    private StoryboardMapper storyboardMapper;
    
    @Autowired
    private CharacterMapper characterMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private ChapterGroupMapper chapterGroupMapper;
    
    @Value("${ai.kimi.api-key}")
    private String kimiApiKey;
    
    @Value("${ai.kimi.base-url:https://api.moonshot.cn/v1}")
    private String kimiBaseUrl;

    @Value("${ai.kimi.model:kimi-latest}")
    private String kimiModel;
    
    @Value("${huoshan.api-key}")
    private String huoshanApiKey;
    
    @Value("${huoshan.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String huoshanBaseUrl;
    
    @Value("${huoshan.image-model:doubao-seedream-4-5-251128}")
    private String imageModel;

    @Value("${huoshan.default-size:1920x1920}")
    private String imageSize;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 分镜生成 Prompt 模板
    private static final String STORYBOARD_GENERATE_PROMPT = 
        "请根据以下小说章节内容，生成适合 2D 动漫动态漫画制作的分镜脚本。\n" +
        "要求：\n" +
        "1. 将章节内容拆分成 5-10 个关键场景\n" +
        "2. 每个场景包含：scene_number（序号）、description（场景描述，100-200 字）、character_names（涉及人物名称列表）\n" +
        "3. 场景描述要包含：场景环境、单个关键动作、情绪氛围、镜头角度建议\n" +
        "4. 场景描述必须适合先生成静态首帧图，再生成 5 秒动漫视频，不要设计复杂连续动作\n" +
        "5. 同一角色在所有场景里保持同一发型、服装、年龄感和气质，不要改设定\n" +
        "6. 以 JSON 数组格式返回，不要有其他说明文字\n" +
        "\n" +
        "章节内容：\n%s";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Storyboard> generateStoryboards(Long groupId, String chapterContent) {
        log.info("生成分镜：groupId={}, contentLength={}", groupId, 
            chapterContent != null ? chapterContent.length() : 0);

        if (chapterContent == null || chapterContent.trim().isEmpty() || "...".equals(chapterContent.trim())) {
            chapterContent = loadGroupContent(groupId);
        }
        
        // 截取前 8000 字用于分析
        String contentToAnalyze = chapterContent.length() > 8000 
            ? chapterContent.substring(0, 8000) 
            : chapterContent;
        
        try {
            // 调用 Kimi AI 生成分镜
            String extractedJson = callKimiForStoryboardGeneration(contentToAnalyze);
            log.debug("Kimi 返回的分镜 JSON: {}", extractedJson);
            
            // 解析 JSON
            List<Map<String, Object>> storyboardList = parseStoryboardJson(extractedJson);
            
            // 获取本分组涉及的人物
            List<NovelCharacter> characters = getGroupCharacters(groupId);
            Map<String, NovelCharacter> characterMap = new HashMap<>();
            for (NovelCharacter character : characters) {
                characterMap.put(character.getName(), character);
            }
            
            // 保存到数据库
            List<Storyboard> storyboards = new ArrayList<>();
            for (Map<String, Object> sbData : storyboardList) {
                Storyboard storyboard = createStoryboardFromData(groupId, sbData, characterMap);
                storyboards.add(storyboard);
            }
            
            if (!storyboards.isEmpty()) {
                storyboardMapper.batchInsert(storyboards);
            }
            
            log.info("分镜生成完成：groupId={}, count={}", groupId, storyboards.size());
            return storyboards;
            
        } catch (Exception e) {
            log.error("分镜生成失败：groupId={}", groupId, e);
            throw new RuntimeException("分镜生成失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 调用 Kimi AI 生成分镜
     */
    private String callKimiForStoryboardGeneration(String content) {
        log.info("调用 Kimi AI 生成分镜...");
        
        String prompt = String.format(STORYBOARD_GENERATE_PROMPT, content);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", kimiModel);
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "system", "content", "你是一个专业的 2D 动漫动态漫画分镜脚本师，擅长将小说内容转化为适合静态首帧图和短视频镜头的分镜脚本。返回纯 JSON 数组格式，不要设计复杂长动作。"),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.4);
        requestBody.put("max_tokens", 4000);
        
        try {
            Map<String, Object> response = HttpUtil.postWithRetry(
                kimiBaseUrl + "/chat/completions",
                kimiApiKey,
                requestBody,
                Map.class,
                3
            );
            
            // 解析响应
            JsonNode rootNode = objectMapper.valueToTree(response);
            JsonNode choicesNode = rootNode.path("choices");
            
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                String content_text = choicesNode.get(0)
                    .path("message")
                    .path("content")
                    .asText();
                
                // 清理响应，提取 JSON
                return extractJsonFromResponse(content_text);
            }
            
            throw new RuntimeException("Kimi 响应格式异常");
            
        } catch (Exception e) {
            log.error("Kimi API 调用失败", e);
            throw new RuntimeException("Kimi API 调用失败：" + e.getMessage(), e);
        }
    }

    private String loadGroupContent(Long groupId) {
        List<Chapter> chapters = chapterMapper.selectByGroupId(groupId);
        if (chapters == null || chapters.isEmpty()) {
            throw new RuntimeException("分组章节为空，请先完成小说分组");
        }

        StringBuilder contentBuilder = new StringBuilder();
        for (Chapter chapter : chapters) {
            if (chapter.getTitle() != null && !chapter.getTitle().trim().isEmpty()) {
                contentBuilder.append(chapter.getTitle()).append('\n');
            }
            if (chapter.getContent() != null && !chapter.getContent().trim().isEmpty()) {
                contentBuilder.append(chapter.getContent()).append('\n');
            }
            // 预留足够上下文，再由上层裁剪到 8000
            if (contentBuilder.length() >= 16000) {
                break;
            }
        }

        String merged = contentBuilder.toString().trim();
        if (merged.isEmpty()) {
            throw new RuntimeException("分组章节内容为空");
        }
        return merged;
    }
    
    /**
     * 从响应中提取 JSON
     */
    private String extractJsonFromResponse(String response) {
        int startIdx = response.indexOf("[");
        int endIdx = response.lastIndexOf("]");
        
        if (startIdx >= 0 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }
        
        return response.trim();
    }
    
    /**
     * 解析分镜 JSON
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseStoryboardJson(String json) {
        try {
            String cleanedJson = json.trim();
            
            if (cleanedJson.startsWith("[")) {
                return objectMapper.readValue(cleanedJson, List.class);
            }
            
            if (cleanedJson.startsWith("{")) {
                Map<String, Object> singleSb = objectMapper.readValue(cleanedJson, Map.class);
                return Arrays.asList(singleSb);
            }
            
            throw new RuntimeException("无效的 JSON 格式");
            
        } catch (Exception e) {
            log.error("解析分镜 JSON 失败：{}", json, e);
            throw new RuntimeException("JSON 解析失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 从数据创建分镜实体
     */
    @SuppressWarnings("unchecked")
    private Storyboard createStoryboardFromData(Long groupId, Map<String, Object> data, 
                                                  Map<String, NovelCharacter> characterMap) {
        Storyboard storyboard = new Storyboard();
        storyboard.setGroupId(groupId);
        
        // 提取字段
        Object sceneNum = data.get("scene_number");
        if (sceneNum instanceof Number) {
            storyboard.setSceneNumber(((Number) sceneNum).intValue());
        } else {
            try {
                storyboard.setSceneNumber(Integer.parseInt(sceneNum.toString()));
            } catch (NumberFormatException e) {
                storyboard.setSceneNumber(1);
            }
        }
        
        storyboard.setDescription(getStringField(data, "description", "场景描述"));
        
        // 处理人物 ID 列表
        Object charNamesObj = data.get("character_names");
        List<Long> characterIds = new ArrayList<>();
        if (charNamesObj instanceof List) {
            List<String> charNames = (List<String>) charNamesObj;
            for (String name : charNames) {
                NovelCharacter character = characterMap.get(name);
                if (character != null) {
                    characterIds.add(character.getId());
                }
            }
        }
        
        if (!characterIds.isEmpty()) {
            storyboard.setCharacterIds(characterIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",")));
        }
        
        // 默认状态
        storyboard.setFrameStatus(0); // 待生成
        storyboard.setIsConfirmed(0); // 未确认
        
        return storyboard;
    }
    
    private String getStringField(Map<String, Object> data, String field, String defaultValue) {
        Object value = data.get(field);
        return value != null ? value.toString().trim() : defaultValue;
    }
    
    /**
     * 获取分组涉及的人物（从项目关联的人物中获取）
     */
    private List<NovelCharacter> getGroupCharacters(Long groupId) {
        ChapterGroup group = chapterGroupMapper.selectById(groupId);
        if (group == null || group.getProjectId() == null) {
            return new ArrayList<>();
        }
        List<NovelCharacter> characters = characterMapper.selectByProjectId(group.getProjectId());
        return characters == null ? new ArrayList<>() : characters;
    }
    
    @Override
    public String generateStoryboardFrame(Long storyboardId, List<Long> characterIds) {
        log.info("生成分镜首帧图：storyboardId={}, characterIds={}", storyboardId, characterIds);
        
        Storyboard storyboard = storyboardMapper.selectById(storyboardId);
        if (storyboard == null) {
            throw new RuntimeException("分镜不存在：storyboardId=" + storyboardId);
        }
        
        if (storyboard.getDescription() == null || storyboard.getDescription().isEmpty()) {
            throw new RuntimeException("分镜描述为空，无法生图");
        }
        
        // 获取人物信息（用于组装提示词）
        List<NovelCharacter> characters = new ArrayList<>();
        if (characterIds != null && !characterIds.isEmpty()) {
            for (Long charId : characterIds) {
                NovelCharacter character = characterMapper.selectById(charId);
                if (character != null) {
                    characters.add(character);
                }
            }
        }
        
        // 组装生图 Prompt
        String prompt = buildFrameImagePrompt(storyboard.getDescription(), characters);
        
        // 更新状态为生成中
        storyboard.setFrameStatus(1);
        storyboard.setPrompt(prompt);
        storyboardMapper.update(storyboard);
        
        try {
            // 调用火山豆包生图 API
            String imageUrl = callHuoshanImageGeneration(prompt);
            
            // 更新分镜信息
            storyboard.setFrameImageUrl(imageUrl);
            storyboard.setFrameStatus(2); // 已完成
            storyboardMapper.update(storyboard);
            
            log.info("分镜首帧图生成成功：storyboardId={}, imageUrl={}", storyboardId, imageUrl);
            return imageUrl;
            
        } catch (Exception e) {
            log.error("分镜首帧图生成失败：storyboardId={}", storyboardId, e);
            storyboard.setFrameStatus(3); // 失败
            storyboardMapper.update(storyboard);
            throw new RuntimeException("生图失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 组装首帧图提示词
     */
    private String buildFrameImagePrompt(String sceneDescription, List<NovelCharacter> characters) {
        return AnimePromptUtil.buildStoryboardFramePrompt(sceneDescription, characters);
    }
    
    /**
     * 调用火山豆包生图 API
     */
    private String callHuoshanImageGeneration(String prompt) {
        log.info("调用火山豆包生图 API...");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", imageModel);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", imageSize);
        
        try {
            Map<String, Object> response = HttpUtil.postWithRetry(
                huoshanBaseUrl + "/images/generations",
                huoshanApiKey,
                requestBody,
                Map.class,
                3
            );
            
            // 解析响应
            JsonNode rootNode = objectMapper.valueToTree(response);
            JsonNode dataNode = rootNode.path("data");
            
            if (dataNode.isArray() && dataNode.size() > 0) {
                String imageUrl = dataNode.get(0).path("url").asText();
                
                if (imageUrl == null || imageUrl.isEmpty()) {
                    throw new RuntimeException("生图 API 返回空 URL");
                }
                
                return imageUrl;
            }
            
            // 检查错误信息
            String error = rootNode.path("error").path("message").asText();
            if (error != null && !error.isEmpty()) {
                throw new RuntimeException("生图 API 错误：" + error);
            }
            
            throw new RuntimeException("生图 API 响应格式异常");
            
        } catch (Exception e) {
            log.error("火山生图 API 调用失败", e);
            throw new RuntimeException("生图 API 调用失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Storyboard> getStoryboardsByGroupId(Long groupId) {
        return storyboardMapper.selectByGroupId(groupId);
    }
    
    @Override
    public void updateStoryboard(Storyboard storyboard) {
        log.info("更新分镜：storyboardId={}", storyboard.getId());
        storyboardMapper.update(storyboard);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStoryboards(List<Long> storyboardIds) {
        log.info("批量确认分镜：count={}", storyboardIds != null ? storyboardIds.size() : 0);
        
        if (storyboardIds == null || storyboardIds.isEmpty()) {
            return;
        }
        
        for (Long id : storyboardIds) {
            Storyboard storyboard = storyboardMapper.selectById(id);
            if (storyboard != null) {
                storyboard.setIsConfirmed(1);
                storyboardMapper.update(storyboard);
            }
        }
        
        log.info("分镜确认完成：count={}", storyboardIds.size());
    }
}
