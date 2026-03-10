# 小说转短视频平台 - 项目输出

## 项目进度

✅ **Phase 1 完成** - 数据库 + Service 代码框架

---

## 文件清单

### 1. 数据库

| 文件 | 说明 | 状态 |
|------|------|------|
| `database.sql` | 7 张表完整 SQL | ✅ 已执行 |

**表结构**:
- `novel_project` - 小说项目表
- `chapter` - 章节表
- `chapter_group` - 章节分组表
- `character` - 人物表
- `storyboard` - 分镜表
- `video_task` - 视频任务表
- `sys_config` - 系统配置表

---

### 2. Entity 实体类

```
src/main/java/com/novel2video/entity/
├── NovelProject.java      ✅
├── Chapter.java           ✅
├── ChapterGroup.java      ✅
├── Character.java         ✅
├── Storyboard.java        ✅
└── VideoTask.java         ✅
```

---

### 3. DTO 数据传输对象

```
src/main/java/com/novel2video/dto/
├── NovelUploadDTO.java         ✅
├── ChapterGroupDTO.java        ✅
├── CharacterExtractDTO.java    ✅
└── StoryboardGenerateDTO.java  ✅
```

---

### 4. Mapper 接口

```
src/main/java/com/novel2video/mapper/
├── NovelProjectMapper.java    ✅
├── ChapterMapper.java         ✅
├── ChapterGroupMapper.java    ✅
├── CharacterMapper.java       ✅
├── StoryboardMapper.java      ✅
└── VideoTaskMapper.java       ✅
```

---

### 5. Service 接口 + 实现

```
src/main/java/com/novel2video/service/
├── NovelService.java          ✅ 接口
│   └── impl/NovelServiceImpl.java          ✅ 实现（小说上传、章节解析、自动分组）
├── CharacterService.java      ✅ 接口
│   └── impl/CharacterServiceImpl.java      ✅ 实现（Kimi 人物提取、豆包生图）
├── StoryboardService.java     ✅ 接口
│   └── impl/StoryboardServiceImpl.java     ✅ 实现（Kimi 分镜、首帧图生成）
├── VideoService.java          ✅ 接口
│   └── impl/VideoServiceImpl.java          ✅ 实现（火山视频生成、状态轮询）
└── OssService.java            ✅ 接口
    └── impl/OssServiceImpl.java          ✅ 实现（阿里云 OSS 封装）
```

---

### 6. 运维文档

```
ops/
├── 01-oss-storage-structure.md   - OSS 目录结构
├── 02-api-cost-estimation.md     - 成本估算（单本约¥102）
├── 03-redis-queue-solution.md    - Redis 队列方案
├── 04-nginx-config.md            - Nginx 配置
└── README.md                     - 文档索引
```

---

### 7. 前端原型

```
frontend/
├── project-list.html          - 项目列表页
├── character-review.html      - 人物审核页
├── storyboard-review.html     - 分镜审核页
└── video-tasks.html           - 视频任务管理页
```

---

## 下一步

### Phase 2 - 集成到现有项目

1. **复制代码到你的 Spring Boot 项目**
   ```bash
   # Entity
   cp -r deploy/novel2video/src/main/java/com/novel2video/entity \
       your-project/src/main/java/com/yourcompany/
   
   # Mapper
   cp -r deploy/novel2video/src/main/java/com/novel2video/mapper \
       your-project/src/main/java/com/yourcompany/
   
   # Service
   cp -r deploy/novel2video/src/main/java/com/novel2video/service \
       your-project/src/main/java/com/yourcompany/
   ```

2. **添加依赖** (`pom.xml`)
   ```xml
   <!-- 火山引擎 Ark SDK -->
   <dependency>
       <groupId>com.volcengine</groupId>
       <artifactId>volcengine-java-sdk-ark-runtime</artifactId>
       <version>LATEST</version>
   </dependency>
   
   <!-- 阿里云 OSS -->
   <dependency>
       <groupId>com.aliyun.oss</groupId>
       <artifactId>aliyun-sdk-oss</artifactId>
       <version>3.17.4</version>
   </dependency>
   ```

3. **配置 API Key** (`application-dev.yml`)
   ```yaml
   huoshan:
     api-key: ${HUOSHAN_API_KEY:8a1298b6-a9fa-4cd5-b915-fee0bfe8ec90}
     base-url: https://ark.cn-beijing.volces.com/api/v3
     video-model: doubao-seedance-1-5-pro-251215
     image-model: doubao-seedream-4-5-251128
   
   ai:
     kimi:
       api-key: ${KIMI_API_KEY:sk-BI29b3F3D50HfPa72NQgHhPbmtoja26hyebt6xTeNU49Ctzm}
       base-url: https://api.moonshot.cn/v1
       model: kimi-latest
   
   aliyun:
     oss:
       endpoint: https://oss-cn-beijing.aliyuncs.com
       access-key-id: ${OSS_ACCESS_KEY_ID}
       access-key-secret: ${OSS_ACCESS_KEY_SECRET}
       bucket-name: translation-service
   ```

4. **启用定时任务** (启动类添加注解)
   ```java
   @EnableScheduling
   @SpringBootApplication
   public class Application { ... }
   ```

5. **创建 Mapper XML** (如果使用 MyBatis XML)

6. **测试接口**

---

## 核心流程

```
1. 上传小说 → NovelService.uploadNovel()
   ↓
2. 提取人物 → CharacterService.extractCharacters()
   ↓ [人工审核]
3. 生成人物图 → CharacterService.generateCharacterImage()
   ↓
4. 生成分镜 → StoryboardService.generateStoryboards()
   ↓ [人工审核]
5. 生成首帧图 → StoryboardService.generateStoryboardFrame()
   ↓
6. 生成视频 → VideoService.createVideoTask()
   ↓ [自动轮询]
7. 完成 → 视频 URL 存入数据库
```

---

## 联系

有问题在论坛讨论：http://43.167.188.71/forum/novel2video/
