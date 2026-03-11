package com.novel2video.controller;

import com.novel2video.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 小说文件上传 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/novel")
public class NovelUploadController {
    
    @Autowired
    private NovelService novelService;
    
    /**
     * 上传小说文件（支持 multipart/form-data）
     */
    @PostMapping("/upload-file")
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "author", required = false, defaultValue = "未知") String author,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {
        
        log.info("上传小说文件：title={}, fileName={}, size={}", title, file.getOriginalFilename(), file.getSize());
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("novel_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);
            log.info("文件已保存到：{}", tempFile);
            
            // 读取文件内容
            String content = Files.readString(tempFile, StandardCharsets.UTF_8);
            
            // 调用服务解析
            Long projectId = novelService.uploadNovelFromFile(title, author, userId, content, file.getOriginalFilename());
            
            // 清理临时文件
            Files.deleteIfExists(tempFile);
            
            result.put("success", true);
            result.put("projectId", projectId);
            result.put("message", "小说上传成功");
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            result.put("success", false);
            result.put("message", "文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("解析失败", e);
            result.put("success", false);
            result.put("message", "解析失败：" + e.getMessage());
        }
        
        return result;
    }
}
