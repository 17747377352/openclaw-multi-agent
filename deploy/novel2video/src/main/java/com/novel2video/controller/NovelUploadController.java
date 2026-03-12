package com.novel2video.controller;

import com.novel2video.common.Result;
import com.novel2video.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/api/novel")
public class NovelUploadController {

    @Autowired
    private NovelService novelService;

    @PostMapping("/upload-file")
    public Result<Long> uploadFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("title") String title,
                                   @RequestParam(value = "author", required = false, defaultValue = "未知") String author,
                                   @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {

        log.info("上传小说文件：title={}, fileName={}, size={}", title, file.getOriginalFilename(), file.getSize());

        try {
            Path tempFile = Files.createTempFile("novel_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);
            log.info("文件已保存到：{}", tempFile);

            String content = Files.readString(tempFile, StandardCharsets.UTF_8);
            Long projectId = novelService.uploadNovelFromFile(title, author, userId, content, file.getOriginalFilename());
            Files.deleteIfExists(tempFile);
            return Result.success("小说上传成功", projectId);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("解析失败", e);
            return Result.error("解析失败：" + e.getMessage());
        }
    }
}
