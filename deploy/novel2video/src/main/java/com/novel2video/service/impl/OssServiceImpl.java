package com.novel2video.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.novel2video.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云 OSS 服务实现
 * 
 * @author developer
 * @since 2026-03-10
 */
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
    
    @Value("${aliyun.oss.region:cn-beijing}")
    private String region;
    
    private static final int MAX_RETRIES = 3;
    
    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength) {
        log.info("上传文件到 OSS: key={}, length={}", key, contentLength);
        
        OSS ossClient = null;
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < MAX_RETRIES) {
            try {
                ossClient = createClient();
                
                // 设置对象元数据
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(contentLength);
                
                // 根据文件扩展名设置 Content-Type
                String contentType = getContentType(key);
                metadata.setContentType(contentType);
                
                PutObjectResult result = ossClient.putObject(bucketName, key, inputStream, metadata);
                log.info("文件上传成功：key={}, etag={}", key, result.getETag());
                
                return getFileUrl(key);
                
            } catch (Exception e) {
                lastException = e;
                log.warn("OSS 上传失败 (attempt {}/{}): key={}, error={}", 
                    attempts + 1, MAX_RETRIES, key, e.getMessage());
                attempts++;
                
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000 * attempts); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("上传被中断", ie);
                    }
                }
            } finally {
                if (ossClient != null) {
                    try {
                        ossClient.shutdown();
                    } catch (Exception e) {
                        log.warn("关闭 OSS 客户端失败", e);
                    }
                }
            }
        }
        
        throw new RuntimeException("OSS 上传失败，已达到最大重试次数：" + lastException.getMessage(), lastException);
    }
    
    @Override
    public String uploadFile(String key, byte[] content) {
        log.debug("上传字节数组到 OSS: key={}, length={}", key, content.length);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            return uploadFile(key, inputStream, content.length);
        } catch (Exception e) {
            throw new RuntimeException("上传失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public String uploadNovel(Long projectId, String fileName, InputStream inputStream, long contentLength) {
        String key = generateNovelFilePath(projectId, fileName);
        log.info("上传小说文件：projectId={}, fileName={}, key={}", projectId, fileName, key);
        return uploadFile(key, inputStream, contentLength);
    }
    
    @Override
    public String uploadCharacterImage(Long projectId, Long characterId, byte[] imageContent) {
        String key = String.format("novel2video/characters/%d/%d_%s.jpg", 
            projectId, characterId, UUID.randomUUID().toString().substring(0, 8));
        log.info("上传人物图：projectId={}, characterId={}, key={}", projectId, characterId, key);
        return uploadFile(key, imageContent);
    }
    
    @Override
    public String uploadStoryboardFrame(Long groupId, Long sceneId, byte[] imageContent) {
        String key = String.format("novel2video/storyboards/%d/%d_frame_%s.jpg", 
            groupId, sceneId, UUID.randomUUID().toString().substring(0, 8));
        log.info("上传分镜首帧图：groupId={}, sceneId={}, key={}", groupId, sceneId, key);
        return uploadFile(key, imageContent);
    }
    
    @Override
    public String uploadVideo(Long groupId, Long sceneId, byte[] videoContent) {
        String key = String.format("novel2video/videos/%d/%d_%s.mp4", 
            groupId, sceneId, UUID.randomUUID().toString().substring(0, 8));
        log.info("上传视频：groupId={}, sceneId={}, key={}", groupId, sceneId, key);
        return uploadFile(key, videoContent);
    }
    
    @Override
    public String getFileUrl(String key) {
        log.debug("生成文件 URL: key={}", key);
        OSS ossClient = null;
        try {
            ossClient = createClient();
            // 生成 365 天有效的预签名 URL
            Date expiration = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("生成文件 URL 失败：key={}", key, e);
            // 如果生成预签名 URL 失败，返回公共读 URL（如果 bucket 是公共读）
            return String.format("https://%s.%s/%s", bucketName, endpoint.replace("https://", ""), key);
        } finally {
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                } catch (Exception e) {
                    log.warn("关闭 OSS 客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public void deleteFile(String key) {
        log.info("删除 OSS 文件：key={}", key);
        OSS ossClient = null;
        try {
            ossClient = createClient();
            ossClient.deleteObject(bucketName, key);
            log.info("文件删除成功：key={}", key);
        } catch (Exception e) {
            log.error("删除文件失败：key={}", key, e);
            throw new RuntimeException("删除失败：" + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                } catch (Exception e) {
                    log.warn("关闭 OSS 客户端失败", e);
                }
            }
        }
    }
    
    @Override
    public String generateNovelFilePath(Long projectId, String fileName) {
        // 确保文件名安全
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("novel2video/novels/%d/%s", projectId, safeFileName);
    }
    
    /**
     * 创建 OSS 客户端
     */
    private OSS createClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String key) {
        if (key == null) return "application/octet-stream";
        
        String lowerKey = key.toLowerCase();
        if (lowerKey.endsWith(".txt")) return "text/plain; charset=utf-8";
        if (lowerKey.endsWith(".epub")) return "application/epub+zip";
        if (lowerKey.endsWith(".pdf")) return "application/pdf";
        if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg")) return "image/jpeg";
        if (lowerKey.endsWith(".png")) return "image/png";
        if (lowerKey.endsWith(".gif")) return "image/gif";
        if (lowerKey.endsWith(".webp")) return "image/webp";
        if (lowerKey.endsWith(".mp4")) return "video/mp4";
        if (lowerKey.endsWith(".webm")) return "video/webm";
        if (lowerKey.endsWith(".json")) return "application/json";
        
        return "application/octet-stream";
    }
}
