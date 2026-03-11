package com.novel2video.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 配置 - 前端页面路由映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径 → index.html
        registry.addViewController("/").setViewName("forward:/index.html");
        
        // 其他页面路由
        registry.addViewController("/project-list").setViewName("forward:/project-list.html");
        registry.addViewController("/character-review").setViewName("forward:/character-review.html");
        registry.addViewController("/storyboard-review").setViewName("forward:/storyboard-review.html");
        registry.addViewController("/video-tasks").setViewName("forward:/video-tasks.html");
    }
}
