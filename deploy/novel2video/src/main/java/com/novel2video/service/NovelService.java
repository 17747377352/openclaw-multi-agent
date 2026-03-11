package com.novel2video.service;

import com.novel2video.entity.NovelProject;
import com.novel2video.entity.Chapter;
import com.novel2video.entity.ChapterGroup;
import com.novel2video.dto.NovelUploadDTO;
import com.novel2video.dto.ChapterGroupDTO;

import java.util.List;

/**
 * 小说服务 - 处理小说上传、章节解析、分组
 * 
 * @author developer
 * @since 2026-03-10
 */
public interface NovelService {

    /**
     * 上传小说并解析章节
     * 
     * @param dto 上传参数（包含文件 OSS 路径、标题、作者等）
     * @return 项目 ID
     */
    Long uploadNovel(NovelUploadDTO dto);
    
    /**
     * 从文件内容上传小说
     */
    Long uploadNovelFromFile(String title, String author, Long userId, String content, String fileName);

    /**
     * 解析小说文件，提取章节
     * 
     * @param projectId 项目 ID
     * @param filePath OSS 文件路径
     * @return 章节列表
     */
    List<Chapter> parseChapters(Long projectId, String filePath);

    /**
     * 按章节号自动分组（默认 5-8 章/组）
     * 
     * @param projectId 项目 ID
     * @param chaptersPerGroup 每组合并章节数
     * @return 分组列表
     */
    List<ChapterGroup> autoGroupChapters(Long projectId, int chaptersPerGroup);

    /**
     * 手动调整分组
     * 
     * @param dto 分组调整参数
     */
    void adjustChapterGroup(ChapterGroupDTO dto);

    /**
     * 获取项目详情
     * 
     * @param projectId 项目 ID
     * @return 项目信息
     */
    NovelProject getProjectById(Long projectId);

    /**
     * 获取项目的章节列表
     * 
     * @param projectId 项目 ID
     * @return 章节列表
     */
    List<Chapter> getChaptersByProjectId(Long projectId);

    /**
     * 获取项目的分组列表
     * 
     * @param projectId 项目 ID
     * @return 分组列表
     */
    List<ChapterGroup> getGroupsByProjectId(Long projectId);

    /**
     * 更新项目状态
     * 
     * @param projectId 项目 ID
     * @param status 状态
     */
    void updateProjectStatus(Long projectId, Integer status);

    /**
     * 删除项目
     * 
     * @param projectId 项目 ID
     */
    void deleteProject(Long projectId);
    
    /**
     * 获取项目列表（分页）
     * 
     * @param status 状态过滤
     * @param sort 排序方式
     * @param keyword 关键词搜索
     * @param page 页码
     * @param size 每页数量
     * @return 项目列表
     */
    List<NovelProject> getProjects(String status, String sort, String keyword, Integer page, Integer size);
}
