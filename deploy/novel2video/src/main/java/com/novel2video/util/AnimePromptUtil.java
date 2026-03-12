package com.novel2video.util;

import com.novel2video.entity.NovelCharacter;

import java.util.Collections;
import java.util.List;

/**
 * 统一 2D 动漫 / 动态漫画风格提示词工具。
 */
public final class AnimePromptUtil {

    private static final String STYLE_ANCHOR =
        "国风2D动漫，动态漫画风格，清晰线稿，赛璐璐上色，统一角色设计，统一服装设计，统一发型设计，非写实，非3D渲染";

    private static final String CHARACTER_CONSISTENCY =
        "保持人物脸型、发型、服装、配色、年龄感一致，不要随镜头改变人设";

    private static final String VIDEO_MOTION_RULE =
        "适合5秒动漫视频镜头，以轻微推镜、拉镜、头发衣摆细微动态、粒子特效为主，避免复杂长动作和写实摄影感";

    private static final String NEGATIVE_RULE =
        "不要写实皮肤，不要3D渲染，不要随机换服装，不要随机换发型，不要脸部崩坏，不要复杂背景干扰主体";

    private AnimePromptUtil() {
    }

    public static String buildCharacterDesignPrompt(String name, String description) {
        String safeDescription = isBlank(description) ? "根据小说设定生成统一标准人设" : description.trim();
        return STYLE_ANCHOR + "。角色标准设定图，全身立绘，正面站姿，纯净浅色背景，便于后续所有分镜和视频复用。"
            + "人物：" + name + "。角色设定：" + safeDescription + "。"
            + CHARACTER_CONSISTENCY + "。五官清晰，服装结构明确，配色稳定。"
            + NEGATIVE_RULE + "。";
    }

    public static String buildStoryboardFramePrompt(String sceneDescription, List<NovelCharacter> characters) {
        String safeSceneDescription = isBlank(sceneDescription) ? "统一风格的动漫分镜场景" : sceneDescription.trim();
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(STYLE_ANCHOR).append("。动漫分镜首帧，适合后续图生视频。\n");
        promptBuilder.append("场景：").append(safeSceneDescription).append("\n");
        appendCharacterBlock(promptBuilder, characters);
        promptBuilder.append("画面要求：16:9 动漫分镜画幅，突出单个关键动作和明确情绪，构图清晰，光影统一，背景服务人物。")
            .append(CHARACTER_CONSISTENCY).append("。")
            .append(NEGATIVE_RULE).append("。");
        return promptBuilder.toString();
    }

    public static String buildVideoPrompt(String sceneDescription, List<NovelCharacter> characters) {
        String safeSceneDescription = isBlank(sceneDescription) ? "统一风格的动漫动态漫画镜头" : sceneDescription.trim();
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(STYLE_ANCHOR).append("。2D 动漫动态漫画镜头。\n");
        promptBuilder.append("镜头场景：").append(safeSceneDescription).append("\n");
        appendCharacterBlock(promptBuilder, characters);
        promptBuilder.append("镜头要求：").append(VIDEO_MOTION_RULE).append("，动作克制，镜头干净，情绪明确。")
            .append(CHARACTER_CONSISTENCY).append("。")
            .append(NEGATIVE_RULE).append("。");
        return promptBuilder.toString();
    }

    private static void appendCharacterBlock(StringBuilder promptBuilder, List<NovelCharacter> characters) {
        List<NovelCharacter> safeCharacters = characters == null ? Collections.emptyList() : characters;
        if (safeCharacters.isEmpty()) {
            return;
        }

        promptBuilder.append("出镜角色（必须保持统一人设）：\n");
        for (NovelCharacter character : safeCharacters) {
            promptBuilder.append("- ")
                .append(character.getName())
                .append("：")
                .append(resolveCharacterDescription(character))
                .append("。保持该角色既定脸型、发型、服装和配色一致。\n");
        }
    }

    private static String resolveCharacterDescription(NovelCharacter character) {
        if (!isBlank(character.getUserEditedDescription())) {
            return character.getUserEditedDescription().trim();
        }
        if (!isBlank(character.getDescription())) {
            return character.getDescription().trim();
        }
        if (!isBlank(character.getSeedPrompt())) {
            return character.getSeedPrompt().trim();
        }
        return "保持该角色既定标准人设";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
