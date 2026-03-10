package com.novel2video.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.novel2video.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.net.URL;

@Slf4j
@Service
public class OssServiceImpl implements OssService {
    
    @Value("${aliyun.oss.endpoint:https://oss-cn-beijing.aliyuncs.com}")
    private String endpoint;
    
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    
    @Value("${aliyun.oss.bucket-name:translation-service}")
    private String bucketName;
    
    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength) {
        log.info("上传文件到 OSS: key={}, length={}", key, contentLength);
        OSS ossClient = createClient();
        try {
            ossClient.putObject(bucketName, key, inputStream);
            return getFileUrl(key);
        } finally {
            ossClient.shutdown();
        }
    }
    
    @Override
    public String uploadFile(String key, byte[] content) {
        return uploadFile(key, new java.io.ByteArrayInputStream(content), content.length);
    }
    
    @Override
    public String uploadNovel(Long projectId, String fileName, InputStream inputStream, long contentLength) {
        String key = String.format("novel2video/novels/%d/%s", projectId, fileName);
        return uploadFile(key, inputStream, contentLength);
    }
    
    @Override
    public String uploadCharacterImage(Long projectId, Long characterId, byte[] imageContent) {
        String key = String.format("novel2video/characters/%d/%d.jpg", projectId, characterId);
        return uploadFile(key, imageContent);
    }
    
    @Override
    public String uploadStoryboardFrame(Long groupId, Long sceneId, byte[] imageContent) {
        String key = String.format("novel2video/storyboards/%d/%d_frame.jpg", groupId, sceneId);
        return uploadFile(key, imageContent);
    }
    
    @Override
    public String uploadVideo(Long groupId, Long sceneId, byte[] videoContent) {
        String key = String.format("novel2video/videos/%d/%d.mp4", groupId, sceneId);
        return uploadFile(key, videoContent);
    }
    
    @Override
    public String getFileUrl(String key) {
        OSS ossClient = createClient();
        try {
            URL url = ossClient.generatePresignedUrl(bucketName, key, new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000));
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }
    
    @Override
    public void deleteFile(String key) {
        log.info("删除 OSS 文件：key={}", key);
        OSS ossClient = createClient();
        try {
            ossClient.deleteObject(bucketName, key);
        } finally {
            ossClient.shutdown();
        }
    }
    
    @Override
    public String generateNovelFilePath(Long projectId, String fileName) {
        return String.format("novel2video/novels/%d/%s", projectId, fileName);
    }
    
    private OSS createClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
