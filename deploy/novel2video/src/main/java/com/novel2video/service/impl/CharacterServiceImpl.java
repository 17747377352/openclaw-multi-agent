package com.novel2video.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel2video.entity.NovelCharacter;
import com.novel2video.mapper.CharacterMapper;
import com.novel2video.service.CharacterService;
import com.novel2video.service.OssService;
import com.novel2video.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 人物服务实现 - 处理人物提取、生图
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@Service
public class CharacterServiceImpl implements CharacterService {
    
    @Autowired
    private CharacterMapper characterMapper;
    
    @Autowired
    private OssService ossService;
    
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
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 人物提取 Prompt 模板
    private static final String CHARACTER_EXTRACT_PROMPT = 
        "请从以下小说内容中提取所有重要人物，并以 JSON 数组格式返回。\n" +
        "每个人物包含以下字段：\n" +
        "- name: 人物姓名（字符串）\n" +
        "- description: 人物外貌描述，包括年龄、身高、体型、发型、五官特征、穿着风格等（100-200 字）\n" +
        "- gender: 性别，1 表示男性，2 表示女性，0 表示未知（整数）\n" +
        "- role: 角色类型，如主角、配角、反派等（字符串）\n" +
        "\n" +
        "只提取有外貌描述的人物，不要提取没有外貌特征的人物。\n" +
        "返回纯 JSON 数组，不要有其他说明文字。\n" +
        "\n" +
        "小说内容：\n%s";
    
    // 生图 Prompt 模板
    private static final String IMAGE_GENERATION_PROMPT = 
        "角色设计图，全身像，正面视角，高清细节，专业角色设定图风格，" +
        "白色背景，三视图风格，清晰的轮廓和细节。" +
        "人物：%s，描述：%s";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<NovelCharacter> extractCharacters(Long projectId, String novelContent) {
        log.info("提取人物：projectId={}, contentLength={}", projectId, 
            novelContent != null ? novelContent.length() : 0);
        
        if (novelContent == null || novelContent.isEmpty()) {
            throw new RuntimeException("小说内容为空");
        }
        
        // 截取前 10000 字用于分析（避免 token 超限）
        String contentToAnalyze = novelContent.length() > 10000 
            ? novelContent.substring(0, 10000) 
            : novelContent;
        
        try {
            // 调用 Kimi AI 提取人物
            String extractedJson = callKimiForCharacterExtraction(contentToAnalyze);
            log.debug("Kimi 返回的人物 JSON: {}", extractedJson);
            
            // 解析 JSON
            List<Map<String, Object>> characterList = parseCharacterJson(extractedJson);
            
            // 保存到数据库
            List<NovelCharacter> characters = new ArrayList<>();
            for (Map<String, Object> charData : characterList) {
                NovelCharacter character = createCharacterFromData(projectId, charData);
                characterMapper.insert(character);
                characters.add(character);
            }
            
            log.info("人物提取完成：projectId={}, count={}", projectId, characters.size());
            return characters;
            
        } catch (Exception e) {
            log.error("人物提取失败：projectId={}", projectId, e);
            throw new RuntimeException("人物提取失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 调用 Kimi AI 提取人物
     */
    private String callKimiForCharacterExtraction(String content) {
        log.info("调用 Kimi AI 提取人物...");
        
        String prompt = String.format(CHARACTER_EXTRACT_PROMPT, content);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "kimi-code-preview"); // 使用 Kimi Code 模型
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "system", "content", "你是一个专业的小说人物分析助手，擅长从文本中提取人物信息并以 JSON 格式返回。"),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
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
    
    /**
     * 从响应中提取 JSON
     */
    private String extractJsonFromResponse(String response) {
        // 尝试找到 JSON 数组的开始和结束
        int startIdx = response.indexOf("[");
        int endIdx = response.lastIndexOf("]");
        
        if (startIdx >= 0 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }
        
        // 尝试找到 JSON 对象的开始和结束
        startIdx = response.indexOf("{");
        endIdx = response.lastIndexOf("}");
        
        if (startIdx >= 0 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }
        
        // 如果找不到，返回原始响应
        return response.trim();
    }
    
    /**
     * 解析人物 JSON
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseCharacterJson(String json) {
        try {
            // 处理可能的 JSON 格式问题
            String cleanedJson = json.trim();
            
            // 尝试解析为数组
            if (cleanedJson.startsWith("[")) {
                return objectMapper.readValue(cleanedJson, List.class);
            }
            
            // 如果是单个对象，包装成数组
            if (cleanedJson.startsWith("{")) {
                Map<String, Object> singleChar = objectMapper.readValue(cleanedJson, Map.class);
                return Arrays.asList(singleChar);
            }
            
            throw new RuntimeException("无效的 JSON 格式");
            
        } catch (Exception e) {
            log.error("解析人物 JSON 失败：{}", json, e);
            throw new RuntimeException("JSON 解析失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 从数据创建人物实体
     */
    private NovelCharacter createCharacterFromData(Long projectId, Map<String, Object> data) {
        NovelCharacter character = new NovelCharacter();
        character.setProjectId(projectId);
        
        // 提取字段
        character.setName(getStringField(data, "name", "未知人物"));
        character.setDescription(getStringField(data, "description", ""));
        character.setGender(getIntField(data, "gender", 0));
        character.setRole(getStringField(data, "role", "配角"));
        
        // 默认状态
        character.setSeedStatus(0); // 待生成
        character.setIsConfirmed(0); // 未确认
        
        return character;
    }
    
    private String getStringField(Map<String, Object> data, String field, String defaultValue) {
        Object value = data.get(field);
        return value != null ? value.toString().trim() : defaultValue;
    }
    
    private Integer getIntField(Map<String, Object> data, String field, Integer defaultValue) {
        Object value = data.get(field);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public String generateCharacterImage(Long characterId) {
        log.info("生成人物图：characterId={}", characterId);
        
        NovelCharacter character = characterMapper.selectById(characterId);
        if (character == null) {
            throw new RuntimeException("人物不存在：characterId=" + characterId);
        }
        
        // 使用用户编辑的描述或 AI 提取的描述
        String description = character.getUserEditedDescription();
        if (description == null || description.isEmpty()) {
            description = character.getDescription();
        }
        
        if (description == null || description.isEmpty()) {
            throw new RuntimeException("人物描述为空，无法生图");
        }
        
        // 组装生图 Prompt
        String prompt = String.format(IMAGE_GENERATION_PROMPT, character.getName(), description);
        
        // 更新状态为生成中
        character.setSeedStatus(1);
        character.setSeedPrompt(prompt);
        characterMapper.update(character);
        
        try {
            // 调用火山豆包生图 API
            String imageUrl = callHuoshanImageGeneration(prompt);
            
            // 更新人物信息
            character.setSeedImageUrl(imageUrl);
            character.setSeedStatus(2); // 已完成
            characterMapper.update(character);
            
            log.info("人物图生成成功：characterId={}, imageUrl={}", characterId, imageUrl);
            return imageUrl;
            
        } catch (Exception e) {
            log.error("豆包生图失败：characterId={}", characterId, e);
            character.setSeedStatus(3); // 失败
            character.setFailReason(e.getMessage());
            characterMapper.update(character);
            throw new RuntimeException("生图失败：" + e.getMessage(), e);
        }
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
        requestBody.put("size", "1024x1024");
        
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
    public List<NovelCharacter> getCharactersByProjectId(Long projectId) {
        return characterMapper.selectByProjectId(projectId);
    }
    
    @Override
    public void updateCharacter(NovelCharacter character) {
        log.info("更新人物信息：characterId={}", character.getId());
        characterMapper.update(character);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmCharacters(List<Long> characterIds) {
        log.info("批量确认人物：count={}", characterIds != null ? characterIds.size() : 0);
        
        if (characterIds == null || characterIds.isEmpty()) {
            return;
        }
        
        characterMapper.batchUpdateConfirmStatus(characterIds, 1);
        log.info("人物确认完成：count={}", characterIds.size());
    }
}
