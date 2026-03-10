package com.novel2video.service;

import com.novel2video.entity.NovelCharacter;

import java.util.List;

/**
 * 人物服务 - 处理人物提取、生图
 * 
 * @author developer
 * @since 2026-03-10
 */
public interface CharacterService {

    /**
     * 使用 Kimi AI 从小说内容中提取人物
     * 
     * @param projectId 项目 ID
     * @param novelContent 小说内容
     * @return 人物列表
     */
    List<Character> extractCharacters(Long projectId, String novelContent);

    /**
     * 生成人物标准照（使用火山豆包生图）
     * 
     * @param characterId 人物 ID
     * @return 人物图 OSS 路径
     */
    String generateCharacterImage(Long characterId);

    /**
     * 获取人物列表
     */
    List<Character> getCharactersByProjectId(Long projectId);

    /**
     * 更新人物信息
     */
    void updateCharacter(NovelNovelCharacter character);

    /**
     * 批量确认人物
     */
    void confirmCharacters(List<Long> characterIds);
}
