package com.novel2video.service;

import java.io.InputStream;

/**
 * 阿里云 OSS 服务
 * 
 * @author developer
 * @since 2026-03-10
 */
public interface OssService {

    /**
     * 上传文件
     */
    String uploadFile(String key, InputStream inputStream, long contentLength);

    /**
     * 上传文件（byte 数组）
     */
    String uploadFile(String key, byte[] content);

    /**
     * 上传小说文件
     */
    String uploadNovel(Long projectId, String fileName, InputStream inputStream, long contentLength);

    /**
     * 上传人物图
     */
    String uploadCharacterImage(Long projectId, Long characterId, byte[] imageContent);

    /**
     * 上传分镜首帧图
     */
    String uploadStoryboardFrame(Long groupId, Long sceneId, byte[] imageContent);

    /**
     * 上传视频
     */
    String uploadVideo(Long groupId, Long sceneId, byte[] videoContent);

    /**
     * 获取文件 URL
     */
    String getFileUrl(String key);

    /**
     * 删除文件
     */
    void deleteFile(String key);

    /**
     * 生成小说文件 OSS 路径
     */
    String generateNovelFilePath(Long projectId, String fileName);
}
