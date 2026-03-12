package com.novel2video.controller;

import com.novel2video.common.Result;
import com.novel2video.dto.NovelUploadDTO;
import com.novel2video.dto.view.ProjectView;
import com.novel2video.entity.Chapter;
import com.novel2video.entity.ChapterGroup;
import com.novel2video.entity.NovelProject;
import com.novel2video.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/novel")
public class NovelController {

    @Autowired
    private NovelService novelService;

    @PostMapping("/upload")
    public Result<Long> uploadNovel(@RequestBody NovelUploadDTO dto) {
        log.info("上传小说：title={}", dto.getTitle());

        try {
            Long projectId;
            if (dto.getFilePath() != null && !dto.getFilePath().trim().isEmpty()) {
                projectId = novelService.uploadNovel(dto);
            } else if (dto.getContent() != null && !dto.getContent().trim().isEmpty()) {
                Long userId = dto.getUserId() != null ? dto.getUserId() : 1L;
                String author = (dto.getAuthor() == null || dto.getAuthor().trim().isEmpty()) ? "未知" : dto.getAuthor();
                String fileName = (dto.getFileName() == null || dto.getFileName().trim().isEmpty()) ? "upload.txt" : dto.getFileName();
                projectId = novelService.uploadNovelFromFile(dto.getTitle(), author, userId, dto.getContent(), fileName);
            } else {
                throw new IllegalArgumentException("filePath 或 content 至少提供一个");
            }

            return Result.success("小说上传成功", projectId);
        } catch (Exception e) {
            log.error("上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/{projectId}")
    public Result<ProjectView> getProject(@PathVariable Long projectId) {
        try {
            NovelProject project = novelService.getProjectById(projectId);
            if (project == null) {
                return Result.error("项目不存在");
            }
            return Result.success(toProjectView(project));
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{projectId}/chapters")
    public Result<List<Chapter>> getChapters(@PathVariable Long projectId) {
        try {
            return Result.success(novelService.getChaptersByProjectId(projectId));
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{projectId}/groups")
    public Result<List<ChapterGroup>> getGroups(@PathVariable Long projectId) {
        try {
            return Result.success(novelService.getGroupsByProjectId(projectId));
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/{projectId}/status")
    public Result<Void> updateStatus(@PathVariable Long projectId, @RequestParam Integer status) {
        try {
            novelService.updateProjectStatus(projectId, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            log.error("更新失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public Result<Void> deleteProject(@PathVariable Long projectId) {
        try {
            novelService.deleteProject(projectId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/projects")
    public Result<List<ProjectView>> getProjects(@RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "created_desc") String sort,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<ProjectView> projects = novelService.getProjects(status, sort, keyword, page, size).stream()
                    .map(this::toProjectView)
                    .collect(Collectors.toList());
            return Result.success(projects);
        } catch (Exception e) {
            log.error("查询项目列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    private ProjectView toProjectView(NovelProject project) {
        ProjectView view = new ProjectView();
        view.setId(project.getId());
        view.setTitle(project.getTitle());
        view.setAuthor(project.getAuthor());
        view.setStatus(toUiStatus(project.getStatus()));
        view.setRawStatus(project.getStatus());
        view.setChapterCount(project.getTotalChapters());
        view.setTotalChapters(project.getTotalChapters());
        view.setTotalWords(project.getTotalWords());
        view.setProgress(estimateProgress(project.getStatus()));
        view.setCharacterCount(0);
        view.setStoryboardCount(0);
        view.setCreatedAt(project.getCreatedAt());
        view.setUpdatedAt(project.getUpdatedAt());
        view.setOriginalFilePath(project.getOriginalFilePath());
        return view;
    }

    private int toUiStatus(Integer status) {
        if (status == null) return 0;
        if (status >= 5) return 3;
        if (status == 4 || status == 1) return 1;
        if (status == 2 || status == 3) return 2;
        return 0;
    }

    private int estimateProgress(Integer status) {
        if (status == null) return 0;
        if (status >= 5) return 100;
        if (status == 4) return 80;
        if (status == 3) return 60;
        if (status == 2) return 40;
        if (status == 1) return 20;
        return 0;
    }
}
