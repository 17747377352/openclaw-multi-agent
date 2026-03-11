package com.novel2video.controller;

import com.novel2video.dto.NovelUploadDTO;
import com.novel2video.entity.Chapter;
import com.novel2video.entity.ChapterGroup;
import com.novel2video.entity.NovelProject;
import com.novel2video.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小说项目管理 Controller
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping("/api/novel")
public class NovelController {
    
    @Autowired
    private NovelService novelService;
    
    /**
     * 上传小说
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadNovel(@RequestBody NovelUploadDTO dto) {
        log.info("上传小说：title={}", dto.getTitle());
        
        Map<String, Object> result = new HashMap<>();
        try {
            Long projectId = novelService.uploadNovel(dto);
            result.put("success", true);
            result.put("projectId", projectId);
            result.put("message", "小说上传成功");
        } catch (Exception e) {
            log.error("上传失败", e);
            result.put("success", false);
            result.put("message", "上传失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取项目详情
     */
    @GetMapping("/{projectId}")
    public Map<String, Object> getProject(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            NovelProject project = novelService.getProjectById(projectId);
            result.put("success", true);
            result.put("data", project);
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取章节列表
     */
    @GetMapping("/{projectId}/chapters")
    public Map<String, Object> getChapters(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Chapter> chapters = novelService.getChaptersByProjectId(projectId);
            result.put("success", true);
            result.put("data", chapters);
            result.put("count", chapters.size());
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取分组列表
     */
    @GetMapping("/{projectId}/groups")
    public Map<String, Object> getGroups(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ChapterGroup> groups = novelService.getGroupsByProjectId(projectId);
            result.put("success", true);
            result.put("data", groups);
            result.put("count", groups.size());
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 更新项目状态
     */
    @PostMapping("/{projectId}/status")
    public Map<String, Object> updateStatus(@PathVariable Long projectId, 
                                            @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            novelService.updateProjectStatus(projectId, status);
            result.put("success", true);
            result.put("message", "状态更新成功");
        } catch (Exception e) {
            log.error("更新失败", e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 删除项目
     */
    @DeleteMapping("/{projectId}")
    public Map<String, Object> deleteProject(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            novelService.deleteProject(projectId);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("删除失败", e);
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取项目列表（分页）
     */
    @GetMapping("/projects")
    public Map<String, Object> getProjects(@RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "created_desc") String sort,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<NovelProject> projects = novelService.getProjects(status, sort, keyword, page, size);
            result.put("success", true);
            result.put("data", projects);
            result.put("total", projects.size()); // 实际应该查询总记录数
        } catch (Exception e) {
            log.error("查询项目列表失败", e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
}
