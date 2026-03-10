package com.novel2video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 小说转短视频平台 - 启动类
 * 
 * @author developer
 * @since 2026-03-10
 */
@SpringBootApplication
@MapperScan("com.novel2video.mapper")
@EnableScheduling
public class Novel2VideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Novel2VideoApplication.class, args);
        System.out.println("======================================");
        System.out.println("  小说转短视频平台 启动成功!");
        System.out.println("======================================");
    }
}
