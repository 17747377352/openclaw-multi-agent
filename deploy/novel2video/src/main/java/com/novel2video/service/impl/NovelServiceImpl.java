package com.novel2video.service.impl;

import com.novel2video.dto.NovelUploadDTO;
import com.novel2video.entity.Chapter;
import com.novel2video.entity.ChapterGroup;
import com.novel2video.entity.NovelProject;
import com.novel2video.mapper.ChapterGroupMapper;
import com.novel2video.mapper.ChapterMapper;
import com.novel2video.mapper.NovelProjectMapper;
import com.novel2video.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NovelServiceImpl implements NovelService {
    
    @Autowired
    private NovelProjectMapper projectMapper;
    
    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private ChapterGroupMapper groupMapper;
    
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("^(第 [零一二三四五六七八九十百千\\d]+章)");
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadNovel(NovelUploadDTO dto) {
        log.info("上传小说：title={}, filePath={}", dto.getTitle(), dto.getFilePath());
        NovelProject project = new NovelProject();
        project.setTitle(dto.getTitle());
        project.setAuthor(dto.getAuthor());
        project.setOriginalFilePath(dto.getFilePath());
        project.setStatus(1);
        project.setUserId(dto.getUserId());
        projectMapper.insert(project);
        List<Chapter> chapters = parseChapters(project.getId(), dto.getFilePath());
        project.setTotalChapters(chapters.size());
        long totalWords = chapters.stream().mapToLong(Chapter::getWordCount).sum();
        project.setTotalWords(totalWords);
        projectMapper.update(project);
        autoGroupChapters(project.getId(), 5);
        return project.getId();
    }
    
    @Override
    public List<Chapter> parseChapters(Long projectId, String filePath) {
        log.info("解析章节：projectId={}, filePath={}", projectId, filePath);
        List<Chapter> chapters = new ArrayList<>();
        try {
            String content = readOssFile(filePath);
            String[] lines = content.split("\n");
            Chapter currentChapter = null;
            StringBuilder contentBuilder = new StringBuilder();
            int chapterNumber = 0;
            for (String line : lines) {
                Matcher matcher = CHAPTER_PATTERN.matcher(line.trim());
                if (matcher.find()) {
                    if (currentChapter != null) {
                        currentChapter.setContent(contentBuilder.toString().trim());
                        currentChapter.setWordCount(currentChapter.getContent().length());
                        chapters.add(currentChapter);
                    }
                    chapterNumber++;
                    currentChapter = new Chapter();
                    currentChapter.setProjectId(projectId);
                    currentChapter.setChapterNumber(chapterNumber);
                    currentChapter.setTitle(matcher.group(1));
                    contentBuilder = new StringBuilder();
                } else if (currentChapter != null) {
                    contentBuilder.append(line).append("\n");
                }
            }
            if (currentChapter != null) {
                currentChapter.setContent(contentBuilder.toString().trim());
                currentChapter.setWordCount(currentChapter.getContent().length());
                chapters.add(currentChapter);
            }
            if (!chapters.isEmpty()) {
                chapterMapper.batchInsert(chapters);
            }
        } catch (Exception e) {
            log.error("解析章节失败", e);
            throw new RuntimeException("解析失败：" + e.getMessage());
        }
        return chapters;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChapterGroup> autoGroupChapters(Long projectId, int chaptersPerGroup) {
        log.info("自动分组：projectId={}, chaptersPerGroup={}", projectId, chaptersPerGroup);
        List<Chapter> chapters = chapterMapper.selectByProjectId(projectId);
        List<ChapterGroup> groups = new ArrayList<>();
        int groupNumber = 0;
        for (int i = 0; i < chapters.size(); i += chaptersPerGroup) {
            groupNumber++;
            int end = Math.min(i + chaptersPerGroup, chapters.size());
            List<Chapter> groupChapters = chapters.subList(i, end);
            ChapterGroup group = new ChapterGroup();
            group.setProjectId(projectId);
            group.setGroupNumber(groupNumber);
            group.setName("第" + groupNumber + "组（第" + groupChapters.get(0).getChapterNumber() + "-" + groupChapters.get(groupChapters.size()-1).getChapterNumber() + "章）");
            group.setStartChapter(groupChapters.get(0).getChapterNumber());
            group.setEndChapter(groupChapters.get(groupChapters.size()-1).getChapterNumber());
            group.setStatus(0);
            group.setChapterIds(groupChapters.stream().map(Chapter::getId).map(String::valueOf).collect(Collectors.joining(",")));
            groupMapper.insert(group);
            for (Chapter chapter : groupChapters) {
                chapterMapper.updateGroupId(chapter.getId(), group.getId());
            }
            groups.add(group);
        }
        return groups;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustChapterGroup(com.novel2video.dto.ChapterGroupDTO dto) {
        log.info("调整分组：groupId={}", dto.getGroupId());
        ChapterGroup group = groupMapper.selectById(dto.getGroupId());
        if (group == null) throw new RuntimeException("分组不存在");
        if (dto.getName() != null) group.setName(dto.getName());
        if (dto.getChapterIds() != null) {
            group.setChapterIds(dto.getChapterIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        groupMapper.update(group);
    }
    
    @Override
    public NovelProject getProjectById(Long projectId) {
        return projectMapper.selectById(projectId);
    }
    
    @Override
    public List<Chapter> getChaptersByProjectId(Long projectId) {
        return chapterMapper.selectByProjectId(projectId);
    }
    
    @Override
    public List<ChapterGroup> getGroupsByProjectId(Long projectId) {
        return groupMapper.selectByProjectId(projectId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProjectStatus(Long projectId, Integer status) {
        NovelProject project = projectMapper.selectById(projectId);
        if (project == null) throw new RuntimeException("项目不存在");
        project.setStatus(status);
        projectMapper.update(project);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        projectMapper.deleteById(projectId);
    }
    
    private String readOssFile(String filePath) throws Exception {
        URL url = new URL(filePath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
}
