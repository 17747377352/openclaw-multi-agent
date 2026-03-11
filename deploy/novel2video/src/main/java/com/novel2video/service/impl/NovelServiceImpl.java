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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 小说服务实现 - 处理小说上传、章节解析、分组
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@Service
public class NovelServiceImpl implements NovelService {
    
    @Autowired
    private NovelProjectMapper projectMapper;
    
    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private ChapterGroupMapper groupMapper;
    
    // 章节标题匹配模式（支持中文数字和阿拉伯数字）
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
        "^(第 [零一二三四五六七八九十百千\\d]+章|Chapter\\s*[\\d]+|CHAPTER\\s*[\\d]+)", 
        Pattern.CASE_INSENSITIVE
    );
    
    // EPUB 内容文件路径
    private static final String[] EPUB_CONTENT_PATHS = {
        "OEBPS/content.opf",
        "content.opf",
        "OEBPS/text/",
        "OEBPS/"
    };
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadNovel(NovelUploadDTO dto) {
        log.info("上传小说：title={}, filePath={}, userId={}", dto.getTitle(), dto.getFilePath(), dto.getUserId());
        
        try {
            // 创建项目记录
            NovelProject project = new NovelProject();
            project.setTitle(dto.getTitle());
            project.setAuthor(dto.getAuthor());
            project.setOriginalFilePath(dto.getFilePath());
            project.setStatus(1); // 处理中
            project.setUserId(dto.getUserId());
            projectMapper.insert(project);
            
            Long projectId = project.getId();
            log.info("项目创建成功：projectId={}", projectId);
            
            // 解析章节
            List<Chapter> chapters = parseChapters(projectId, dto.getFilePath());
            log.info("章节解析完成：projectId={}, chapterCount={}", projectId, chapters.size());
            
            if (chapters.isEmpty()) {
                throw new RuntimeException("未解析到任何章节，请检查文件格式");
            }
            
            // 更新项目统计信息
            project.setTotalChapters(chapters.size());
            long totalWords = chapters.stream().mapToLong(Chapter::getWordCount).sum();
            project.setTotalWords(totalWords);
            projectMapper.update(project);
            
            // 自动分组（默认 5 章/组）
            autoGroupChapters(projectId, 5);
            log.info("章节分组完成：projectId={}", projectId);
            
            return projectId;
            
        } catch (Exception e) {
            log.error("上传小说失败", e);
            throw new RuntimeException("上传失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Chapter> parseChapters(Long projectId, String filePath) {
        log.info("解析章节：projectId={}, filePath={}", projectId, filePath);
        
        List<Chapter> chapters = new ArrayList<>();
        
        try {
            String content = readOssFile(filePath);
            
            // 检测文件类型
            String fileExt = getFileExtension(filePath).toLowerCase();
            
            if ("epub".equals(fileExt)) {
                chapters = parseEpubContent(content, projectId);
            } else {
                // 默认按 TXT 解析
                chapters = parseTxtContent(content, projectId);
            }
            
            // 批量插入章节
            if (!chapters.isEmpty()) {
                chapterMapper.batchInsert(chapters);
                log.info("章节入库成功：count={}", chapters.size());
            }
            
        } catch (Exception e) {
            log.error("解析章节失败：projectId={}, filePath={}", projectId, filePath, e);
            throw new RuntimeException("解析失败：" + e.getMessage(), e);
        }
        
        return chapters;
    }
    
    /**
     * 解析 TXT 格式小说
     */
    private List<Chapter> parseTxtContent(String content, Long projectId) {
        List<Chapter> chapters = new ArrayList<>();
        String[] lines = content.split("\n");
        
        Chapter currentChapter = null;
        StringBuilder contentBuilder = new StringBuilder();
        int chapterNumber = 0;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // 检测章节标题
            Matcher matcher = CHAPTER_PATTERN.matcher(trimmedLine);
            if (matcher.find()) {
                // 保存上一章节
                if (currentChapter != null) {
                    saveChapter(currentChapter, contentBuilder.toString());
                    chapters.add(currentChapter);
                }
                
                // 创建新章节
                chapterNumber++;
                currentChapter = new Chapter();
                currentChapter.setProjectId(projectId);
                currentChapter.setChapterNumber(chapterNumber);
                currentChapter.setTitle(extractChapterTitle(trimmedLine));
                contentBuilder = new StringBuilder();
            } else if (currentChapter != null && !trimmedLine.isEmpty()) {
                contentBuilder.append(line).append("\n");
            }
        }
        
        // 保存最后一章
        if (currentChapter != null) {
            saveChapter(currentChapter, contentBuilder.toString());
            chapters.add(currentChapter);
        }
        
        log.info("TXT 解析完成：chapterCount={}", chapters.size());
        return chapters;
    }
    
    /**
     * 解析 EPUB 格式小说
     */
    private List<Chapter> parseEpubContent(String content, Long projectId) {
        List<Chapter> chapters = new ArrayList<>();
        
        // EPUB 解析逻辑（简化版，实际可能需要更复杂的解析）
        // 这里假设 content 是 EPUB 中的文本内容
        String[] lines = content.split("\n");
        
        Chapter currentChapter = null;
        StringBuilder contentBuilder = new StringBuilder();
        int chapterNumber = 0;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // 检测章节标题（EPUB 中可能使用 <h1>, <h2> 等标签）
            if (trimmedLine.startsWith("<h1>") || trimmedLine.startsWith("<h2>") || 
                trimmedLine.startsWith("第") || trimmedLine.startsWith("Chapter")) {
                
                String cleanLine = trimmedLine.replaceAll("<[^>]+>", "");
                Matcher matcher = CHAPTER_PATTERN.matcher(cleanLine);
                
                if (matcher.find() || cleanLine.contains("第") || cleanLine.toLowerCase().contains("chapter")) {
                    if (currentChapter != null) {
                        saveChapter(currentChapter, contentBuilder.toString());
                        chapters.add(currentChapter);
                    }
                    
                    chapterNumber++;
                    currentChapter = new Chapter();
                    currentChapter.setProjectId(projectId);
                    currentChapter.setChapterNumber(chapterNumber);
                    currentChapter.setTitle(extractChapterTitle(cleanLine));
                    contentBuilder = new StringBuilder();
                }
            } else if (currentChapter != null) {
                String cleanLine = trimmedLine.replaceAll("<[^>]+>", "");
                if (!cleanLine.isEmpty()) {
                    contentBuilder.append(cleanLine).append("\n");
                }
            }
        }
        
        if (currentChapter != null) {
            saveChapter(currentChapter, contentBuilder.toString());
            chapters.add(currentChapter);
        }
        
        log.info("EPUB 解析完成：chapterCount={}", chapters.size());
        return chapters;
    }
    
    /**
     * 保存章节信息
     */
    private void saveChapter(Chapter chapter, String content) {
        String trimmedContent = content.trim();
        chapter.setContent(trimmedContent);
        chapter.setWordCount(trimmedContent.length());
        chapter.setGroupId(null); // 初始为空，分组时设置
    }
    
    /**
     * 从标题行提取章节标题
     */
    private String extractChapterTitle(String line) {
        // 去除可能的 HTML 标签
        String cleanTitle = line.replaceAll("<[^>]+>", "").trim();
        
        // 限制标题长度
        if (cleanTitle.length() > 100) {
            cleanTitle = cleanTitle.substring(0, 100) + "...";
        }
        
        return cleanTitle;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChapterGroup> autoGroupChapters(Long projectId, int chaptersPerGroup) {
        log.info("自动分组：projectId={}, chaptersPerGroup={}", projectId, chaptersPerGroup);
        
        if (chaptersPerGroup < 1) {
            chaptersPerGroup = 5; // 默认值
        }
        
        List<Chapter> chapters = chapterMapper.selectByProjectId(projectId);
        if (chapters.isEmpty()) {
            log.warn("没有章节可分组：projectId={}", projectId);
            return new ArrayList<>();
        }
        
        List<ChapterGroup> groups = new ArrayList<>();
        int groupNumber = 0;
        
        for (int i = 0; i < chapters.size(); i += chaptersPerGroup) {
            groupNumber++;
            int end = Math.min(i + chaptersPerGroup, chapters.size());
            List<Chapter> groupChapters = chapters.subList(i, end);
            
            ChapterGroup group = new ChapterGroup();
            group.setProjectId(projectId);
            group.setGroupNumber(groupNumber);
            
            // 生成分组名称
            int startChapterNum = groupChapters.get(0).getChapterNumber();
            int endChapterNum = groupChapters.get(groupChapters.size() - 1).getChapterNumber();
            group.setName("第" + groupNumber + "组（第" + startChapterNum + "-" + endChapterNum + "章）");
            
            group.setStartChapter(startChapterNum);
            group.setEndChapter(endChapterNum);
            group.setStatus(0); // 待处理
            
            // 设置章节 ID 列表
            String chapterIds = groupChapters.stream()
                .map(Chapter::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            group.setChapterIds(chapterIds);
            
            groupMapper.insert(group);
            
            // 更新章节的分组 ID
            for (Chapter chapter : groupChapters) {
                chapterMapper.updateGroupId(chapter.getId(), group.getId());
            }
            
            groups.add(group);
        }
        
        log.info("自动分组完成：projectId={}, groupCount={}", projectId, groups.size());
        return groups;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustChapterGroup(com.novel2video.dto.ChapterGroupDTO dto) {
        log.info("调整分组：groupId={}", dto.getGroupId());
        
        ChapterGroup group = groupMapper.selectById(dto.getGroupId());
        if (group == null) {
            throw new RuntimeException("分组不存在：groupId=" + dto.getGroupId());
        }
        
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            group.setName(dto.getName());
        }
        
        if (dto.getChapterIds() != null && !dto.getChapterIds().isEmpty()) {
            String chapterIds = dto.getChapterIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            group.setChapterIds(chapterIds);
            
            // 更新起始和结束章节号
            List<Chapter> chapters = chapterMapper.selectByProjectId(group.getProjectId());
            Map<Long, Chapter> chapterMap = chapters.stream()
                .collect(Collectors.toMap(Chapter::getId, c -> c));
            
            List<Long> ids = dto.getChapterIds();
            if (!ids.isEmpty()) {
                Chapter firstChapter = chapterMap.get(ids.get(0));
                Chapter lastChapter = chapterMap.get(ids.get(ids.size() - 1));
                if (firstChapter != null && lastChapter != null) {
                    group.setStartChapter(firstChapter.getChapterNumber());
                    group.setEndChapter(lastChapter.getChapterNumber());
                }
            }
        }
        
        groupMapper.update(group);
        log.info("分组调整完成：groupId={}", dto.getGroupId());
    }
    
    @Override
    public NovelProject getProjectById(Long projectId) {
        NovelProject project = projectMapper.selectById(projectId);
        if (project == null) {
            log.warn("项目不存在：projectId={}", projectId);
        }
        return project;
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
        if (project == null) {
            throw new RuntimeException("项目不存在：projectId=" + projectId);
        }
        project.setStatus(status);
        projectMapper.update(project);
        log.info("项目状态更新：projectId={}, status={}", projectId, status);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        log.info("删除项目：projectId={}", projectId);
        
        // 先删除关联数据
        List<ChapterGroup> groups = groupMapper.selectByProjectId(projectId);
        for (ChapterGroup group : groups) {
            groupMapper.deleteById(group.getId());
        }
        
        chapterMapper.deleteByProjectId(projectId);
        projectMapper.deleteById(projectId);
        
        log.info("项目删除完成：projectId={}", projectId);
    }
    
    /**
     * 从 OSS 读取文件内容
     */
    private String readOssFile(String filePath) throws Exception {
        log.debug("从 OSS 读取文件：filePath={}", filePath);
        
        URL url = new URL(filePath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filePath) {
        if (filePath == null) return "";
        int lastDot = filePath.lastIndexOf(".");
        if (lastDot < 0) return "";
        int lastSlash = filePath.lastIndexOf("/");
        if (lastSlash > lastDot) return "";
        return filePath.substring(lastDot + 1);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadNovelFromFile(String title, String author, Long userId, String content, String fileName) {
        log.info("从文件内容上传小说：title={}, fileName={}, contentLength={}", title, fileName, content.length());
        
        try {
            // 创建项目记录
            NovelProject project = new NovelProject();
            project.setTitle(title);
            project.setAuthor(author);
            project.setOriginalFilePath("upload:" + fileName);
            project.setStatus(1);
            project.setUserId(userId);
            projectMapper.insert(project);
            
            Long projectId = project.getId();
            log.info("项目创建成功：projectId={}", projectId);
            
            // 解析章节
            List<Chapter> chapters;
            String fileExt = getFileExtension(fileName).toLowerCase();
            
            if ("epub".equals(fileExt)) {
                chapters = parseEpubContent(content, projectId);
            } else {
                chapters = parseTxtContent(content, projectId);
            }
            
            if (chapters.isEmpty()) {
                throw new RuntimeException("未解析到任何章节，请检查文件格式");
            }
            
            // 批量插入章节
            chapterMapper.batchInsert(chapters);
            log.info("章节入库成功：count={}", chapters.size());
            
            // 更新项目统计
            project.setTotalChapters(chapters.size());
            long totalWords = chapters.stream().mapToLong(Chapter::getWordCount).sum();
            project.setTotalWords(totalWords);
            projectMapper.update(project);
            
            // 自动分组
            autoGroupChapters(projectId, 5);
            
            return projectId;
            
        } catch (Exception e) {
            log.error("上传小说失败", e);
            throw new RuntimeException("上传失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public List<NovelProject> getProjects(String status, String sort, String keyword, Integer page, Integer size) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        
        if (status != null && !status.isEmpty()) {
            params.put("status", Integer.parseInt(status));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", "%" + keyword + "%");
        }
        
        // 处理分页
        int offset = (page - 1) * size;
        params.put("offset", offset);
        params.put("limit", size);
        
        // 处理排序
        String orderBy = "created_at DESC";
        if ("created_asc".equals(sort)) {
            orderBy = "created_at ASC";
        } else if ("title_desc".equals(sort)) {
            orderBy = "title DESC";
        } else if ("title_asc".equals(sort)) {
            orderBy = "title ASC";
        }
        params.put("orderBy", orderBy);
        
        return projectMapper.selectProjects(params);
    }
}
