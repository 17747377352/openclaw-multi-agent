package com.novel2video.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;

/**
 * HTTP 工具类 - 处理 API 调用、重试逻辑
 * 
 * @author developer
 * @since 2026-03-10
 */
@Slf4j
public class HttpUtil {
    
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    /**
     * POST 请求（带重试）
     */
    public static <T> T postWithRetry(String url, String apiKey, Map<String, Object> body, Class<T> responseType) {
        return postWithRetry(url, apiKey, body, responseType, MAX_RETRIES);
    }
    
    /**
     * POST 请求（带重试和自定义重试次数）
     */
    public static <T> T postWithRetry(String url, String apiKey, Map<String, Object> body, Class<T> responseType, int maxRetries) {
        return postWithRetry(url, apiKey, body, null, responseType, maxRetries);
    }

    /**
     * POST 请求（带重试和自定义 Header）
     */
    public static <T> T postWithRetry(String url, String apiKey, Map<String, Object> body,
                                      Map<String, String> extraHeaders, Class<T> responseType, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                HttpHeaders headers = createJsonHeaders(apiKey);
                applyExtraHeaders(headers, extraHeaders);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                } else {
                    log.warn("HTTP 请求失败：url={}, status={}", url, response.getStatusCode());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("HTTP 请求异常 (attempt {}/{}): url={}, error={}", 
                    attempts + 1, maxRetries, url, e.getMessage());
                
                // 检查是否是服务过载错误，需要重试
                if (isRetriableException(e)) {
                    attempts++;
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("请求被中断", ie);
                    }
                    continue;
                }
                throw new RuntimeException("API 调用失败：" + e.getMessage(), e);
            }
            attempts++;
        }
        
        throw new RuntimeException("API 调用失败，已达到最大重试次数", lastException);
    }

    /**
     * GET 请求（带重试）
     */
    public static <T> T getWithRetry(String url, String apiKey, Class<T> responseType) {
        return getWithRetry(url, apiKey, null, responseType, MAX_RETRIES);
    }

    /**
     * GET 请求（带重试和自定义重试次数）
     */
    public static <T> T getWithRetry(String url, String apiKey, Class<T> responseType, int maxRetries) {
        return getWithRetry(url, apiKey, null, responseType, maxRetries);
    }

    /**
     * GET 请求（带重试和自定义 Header）
     */
    public static <T> T getWithRetry(String url, String apiKey, Map<String, String> extraHeaders,
                                     Class<T> responseType, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                HttpHeaders headers = createJsonHeaders(apiKey);
                applyExtraHeaders(headers, extraHeaders);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                } else {
                    log.warn("HTTP GET 请求失败：url={}, status={}", url, response.getStatusCode());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("HTTP GET 请求异常 (attempt {}/{}): url={}, error={}",
                    attempts + 1, maxRetries, url, e.getMessage());

                if (isRetriableException(e)) {
                    attempts++;
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("请求被中断", ie);
                    }
                    continue;
                }
                throw new RuntimeException("API 调用失败：" + e.getMessage(), e);
            }
            attempts++;
        }

        throw new RuntimeException("API 调用失败，已达到最大重试次数", lastException);
    }
    
    /**
     * 创建标准 HTTP 头
     */
    public static HttpHeaders createJsonHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }
        return headers;
    }
    
    /**
     * 创建请求体
     */
    public static Map<String, Object> createRequestBody(Object... pairs) {
        Map<String, Object> body = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i] instanceof String && i + 1 < pairs.length) {
                body.put((String) pairs[i], pairs[i + 1]);
            }
        }
        return body;
    }

    private static void applyExtraHeaders(HttpHeaders headers, Map<String, String> extraHeaders) {
        if (extraHeaders == null || extraHeaders.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                headers.set(entry.getKey(), entry.getValue());
            }
        }
    }

    private static boolean isRetriableException(Exception e) {
        if (e == null || e.getMessage() == null) {
            return false;
        }

        String message = e.getMessage().toLowerCase();
        return message.contains("overloaded") || message.contains("timeout");
    }
}
