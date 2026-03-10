# 小说转短视频项目 - Redis 任务队列方案

## 📌 环境信息

- **Redis Host**: `123.56.22.101:16379`
- **Password**: `nnnnn520`
- **Database**: `0`
- **连接池**: max-active=8, max-idle=8, min-idle=0

---

## 🗂️ Key 命名规范

```
novel2video:
├── queue:video:pending          # 待处理视频任务队列 (List)
├── queue:video:processing       # 处理中任务集合 (ZSet, score=开始时间)
├── queue:video:retry            # 待重试任务队列 (List)
├── queue:video:dead             # 死信队列 (List)
│
├── task:{task_id}               # 任务详情 (Hash)
├── task:{task_id}:log           # 任务日志 (List)
│
├── project:{project_id}:status  # 项目进度 (Hash)
└── stats:daily:{date}           # 每日统计 (Hash)
```

---

## 📋 任务数据结构

### 任务详情 Hash (`task:{task_id}`)

```json
{
  "id": "tsk_a1b2c3d4e5f6",
  "type": "VIDEO_GENERATE",
  "status": "PENDING",
  "project_id": "proj_xxx",
  "group_id": "grp_xxx",
  "scene_id": "scn_xxx",
  "prompt": "一个身穿蓝衣的侠客站在山顶...",
  "image_url": "https://oss.../frame.jpg",
  "priority": 1,
  "retry_count": 0,
  "max_retry": 3,
  "created_at": 1710000000000,
  "started_at": null,
  "completed_at": null,
  "result_url": null,
  "error_msg": null
}
```

### 项目进度 Hash (`project:{project_id}:status`)

```json
{
  "total_scenes": 100,
  "pending": 50,
  "processing": 5,
  "completed": 40,
  "failed": 5,
  "progress_percent": 45,
  "estimated_remaining_minutes": 25
}
```

---

## 🔄 队列工作流程

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   PENDING   │────▶│  PROCESSING  │────▶│  COMPLETED  │
│   (队列)    │     │   (ZSet)     │     │  (更新 DB)  │
└─────────────┘     └──────────────┘     └─────────────┘
       │                    │
       │ 失败               │ 超时
       ▼                    ▼
┌─────────────┐     ┌──────────────┐
│    RETRY    │────▶│     DEAD     │
│   (重试)    │     │   (死信)     │
└─────────────┘     └──────────────┘
```

---

## 💻 Java 实现代码

### 1. 任务队列服务类

```java
@Service
public class VideoTaskQueueService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String QUEUE_PENDING = "novel2video:queue:video:pending";
    private static final String QUEUE_PROCESSING = "novel2video:queue:video:processing";
    private static final String QUEUE_RETRY = "novel2video:queue:video:retry";
    private static final String QUEUE_DEAD = "novel2video:queue:video:dead";
    private static final String TASK_PREFIX = "novel2video:task:";
    
    /**
     * 添加任务到队列
     */
    public void enqueue(VideoTask task) {
        String taskKey = TASK_PREFIX + task.getId();
        // 保存任务详情
        redisTemplate.opsForHash().putAll(taskKey, convertToHash(task));
        // 根据优先级入队
        if (task.getPriority() > 5) {
            redisTemplate.opsForList().leftPush(QUEUE_PENDING, task.getId());
        } else {
            redisTemplate.opsForList().rightPush(QUEUE_PENDING, task.getId());
        }
    }
    
    /**
     * 获取下一个待处理任务
     */
    @Transactional
    public VideoTask dequeue() {
        String taskId = (String) redisTemplate.opsForList().rightPop(QUEUE_PENDING);
        if (taskId == null) return null;
        
        String taskKey = TASK_PREFIX + taskId;
        // 更新状态为处理中
        redisTemplate.opsForHash().put(taskKey, "status", "PROCESSING");
        redisTemplate.opsForHash().put(taskKey, "started_at", String.valueOf(System.currentTimeMillis()));
        // 加入处理中集合
        redisTemplate.opsForZSet().add(QUEUE_PROCESSING, taskId, System.currentTimeMillis());
        
        return getTask(taskId);
    }
    
    /**
     * 任务完成
     */
    public void complete(String taskId, String resultUrl) {
        String taskKey = TASK_PREFIX + taskId;
        redisTemplate.opsForHash().put(taskKey, "status", "COMPLETED");
        redisTemplate.opsForHash().put(taskKey, "completed_at", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().put(taskKey, "result_url", resultUrl);
        // 从处理中移除
        redisTemplate.opsForZSet().remove(QUEUE_PROCESSING, taskId);
        // 更新项目进度
        updateProjectProgress(taskId);
    }
    
    /**
     * 任务失败，加入重试队列
     */
    public void fail(String taskId, String errorMsg) {
        String taskKey = TASK_PREFIX + taskId;
        Integer retryCount = (Integer) redisTemplate.opsForHash().get(taskKey, "retry_count");
        Integer maxRetry = (Integer) redisTemplate.opsForHash().get(taskKey, "max_retry");
        
        if (retryCount < maxRetry) {
            // 可重试
            redisTemplate.opsForHash().increment(taskKey, "retry_count", 1);
            redisTemplate.opsForHash().put(taskKey, "error_msg", errorMsg);
            redisTemplate.opsForList().rightPush(QUEUE_RETRY, taskId);
        } else {
            // 转死信
            redisTemplate.opsForHash().put(taskKey, "status", "FAILED");
            redisTemplate.opsForHash().put(taskKey, "error_msg", errorMsg);
            redisTemplate.opsForList().rightPush(QUEUE_DEAD, taskId);
        }
        // 从处理中移除
        redisTemplate.opsForZSet().remove(QUEUE_PROCESSING, taskId);
    }
    
    /**
     * 处理重试任务
     */
    public void processRetryQueue() {
        String taskId = (String) redisTemplate.opsForList().rightPop(QUEUE_RETRY);
        if (taskId != null) {
            // 延迟 5 秒后重新入队
            redisTemplate.opsForList().leftPush(QUEUE_PENDING, taskId);
        }
    }
    
    /**
     * 检测超时任务（超过 10 分钟未完成）
     */
    public void checkTimeoutTasks() {
        long timeout = System.currentTimeMillis() - 10 * 60 * 1000;
        Set<Object> timeoutTasks = redisTemplate.opsForZSet().rangeByScore(QUEUE_PROCESSING, 0, timeout);
        for (Object taskId : timeoutTasks) {
            fail((String) taskId, "Task timeout");
        }
    }
    
    /**
     * 获取队列统计
     */
    public QueueStats getStats() {
        return new QueueStats(
            redisTemplate.opsForList().size(QUEUE_PENDING),
            redisTemplate.opsForZSet().size(QUEUE_PROCESSING),
            redisTemplate.opsForList().size(QUEUE_RETRY),
            redisTemplate.opsForList().size(QUEUE_DEAD)
        );
    }
}
```

### 2. 消费者线程

```java
@Component
public class VideoTaskConsumer {
    
    @Autowired
    private VideoTaskQueueService queueService;
    
    @Autowired
    private VolcVideoService volcVideoService;
    
    @Scheduled(fixedDelay = 1000) // 每秒检查
    public void consume() {
        VideoTask task = queueService.dequeue();
        if (task == null) return;
        
        try {
            String videoUrl = volcVideoService.generateVideo(task.getImageUrl(), task.getPrompt());
            queueService.complete(task.getId(), videoUrl);
        } catch (Exception e) {
            queueService.fail(task.getId(), e.getMessage());
        }
    }
}
```

### 3. 重试任务处理器

```java
@Component
public class RetryTaskHandler {
    
    @Autowired
    private VideoTaskQueueService queueService;
    
    @Scheduled(fixedDelay = 5000) // 每 5 秒检查重试队列
    public void handleRetry() {
        queueService.processRetryQueue();
    }
    
    @Scheduled(fixedDelay = 60000) // 每分钟检查超时
    public void checkTimeout() {
        queueService.checkTimeoutTasks();
    }
}
```

---

## ⚙️ Redis 配置优化

### application.yml

```yaml
spring:
  redis:
    host: 123.56.22.101
    port: 16379
    password: nnnnn520
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: 3000ms
      cluster:
        refresh:
          adaptive: true
```

### 连接池监控

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("123.56.22.101");
        config.setPort(16379);
        config.setPassword(RedisPassword.of("nnnnn520"));
        
        LettuceClientConfigurationBuilderCustomizer customizer = 
            builder -> builder.clientOptions(
                ClientOptions.builder()
                    .timeoutOptions(TimeoutOptions.builder()
                        .fixedTimeout(Duration.ofSeconds(3))
                        .build())
                    .build());
        
        return new LettuceConnectionFactory(config, 
            LettucePoolingClientConfiguration.builder()
                .poolConfig(GenericObjectPoolConfig.builder()
                    .maxTotal(8)
                    .maxIdle(8)
                    .minIdle(0)
                    .maxWait(Duration.ofMillis(3000))
                    .build())
                .build());
    }
}
```

---

## 📊 监控指标

### 关键指标

| 指标 | 阈值 | 告警 |
|------|------|------|
| 待处理队列长度 | >500 | 警告 |
| 处理中任务超时 | >10 分钟 | 严重 |
| 失败率 | >20% | 警告 |
| 死信队列增长 | >10/小时 | 严重 |

### 监控查询

```bash
# 查看队列长度
LLEN novel2video:queue:video:pending
ZCARD novel2video:queue:video:processing

# 查看任务详情
HGETALL novel2video:task:tsk_xxx

# 查看死信队列
LRANGE novel2video:queue:video:dead 0 -1

# 查看项目进度
HGETALL novel2video:project:proj_xxx:status
```

---

## 🎯 最佳实践

1. **任务幂等性** - 任务 ID 全局唯一，重复提交自动去重
2. **优雅关闭** - 应用关闭前完成当前任务
3. **批量操作** - 使用 Pipeline 减少网络往返
4. **过期清理** - 已完成任务 7 天后自动删除
5. **限流保护** - 单 IP 每秒最多 10 个新任务
