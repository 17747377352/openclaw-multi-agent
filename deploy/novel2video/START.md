# 小说转短视频平台 - 快速开始

## 项目结构

```
novel2video/
├── pom.xml                          # Maven 配置
├── start.sh                         # 启动脚本
├── src/main/java/com/novel2video/
│   ├── Novel2VideoApplication.java  # 启动类
│   ├── controller/                  # 4 个 Controller
│   ├── service/                     # 5 个 Service 接口 + 实现
│   ├── mapper/                      # 6 个 Mapper 接口
│   ├── entity/                      # 6 个实体类
│   └── dto/                         # 4 个 DTO
└── src/main/resources/
    ├── application.yml              # 主配置
    ├── application-dev.yml          # 开发环境
    └── application-prod.yml         # 生产环境
```

---

## 快速启动

### 方式 1: 使用启动脚本（推荐）

```bash
chmod +x start.sh
./start.sh
```

### 方式 2: Maven 命令

```bash
cd /home/ubuntu/.openclaw/workspace/deploy/novel2video
mvn clean package -DskipTests
java -jar target/novel2video-1.0.0-SNAPSHOT.jar
```

### 方式 3: IDE 运行

1. 用 IDEA 打开 `novel2video` 文件夹
2. 运行 `Novel2VideoApplication.java`

---

## 访问地址

启动成功后访问：

- **服务地址**: `http://localhost:8081`
- **健康检查**: `http://localhost:8081/actuator/health` (需添加依赖)

---

## API 接口

### 项目管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/novel/upload` | POST | 上传小说 |
| `/api/novel/{projectId}` | GET | 获取项目详情 |
| `/api/novel/{projectId}/chapters` | GET | 获取章节列表 |
| `/api/novel/{projectId}/groups` | GET | 获取分组列表 |
| `/api/novel/{projectId}/status` | POST | 更新项目状态 |
| `/api/novel/{projectId}` | DELETE | 删除项目 |

### 人物管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/character/project/{projectId}` | GET | 获取人物列表 |
| `/api/character/extract` | POST | AI 提取人物 |
| `/api/character/{characterId}/image` | POST | 生成人物图 |
| `/api/character/{characterId}` | POST | 更新人物信息 |
| `/api/character/confirm` | POST | 批量确认人物 |

### 分镜管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/storyboard/group/{groupId}` | GET | 获取分镜列表 |
| `/api/storyboard/generate` | POST | AI 生成分镜 |
| `/api/storyboard/{storyboardId}/frame` | POST | 生成首帧图 |
| `/api/storyboard/{storyboardId}` | POST | 更新分镜 |
| `/api/storyboard/confirm` | POST | 批量确认分镜 |

### 视频管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/video/group/{groupId}` | GET | 获取视频任务列表 |
| `/api/video/create` | POST | 创建视频任务 |
| `/api/video/{taskId}` | GET | 获取任务详情 |
| `/api/video/{taskId}/retry` | POST | 重试任务 |
| `/api/video/batch/{groupId}` | POST | 批量生成 |

---

## 测试示例

### 1. 上传小说

```bash
curl -X POST http://localhost:8081/api/novel/upload \
  -H "Content-Type: application/json" \
  -d '{
    "title": "斗破苍穹",
    "author": "天蚕土豆",
    "filePath": "https://oss.example.com/novels/doupo.txt",
    "userId": 1
  }'
```

### 2. 获取项目详情

```bash
curl http://localhost:8081/api/novel/1
```

### 3. 提取人物

```bash
curl -X POST "http://localhost:8081/api/character/extract?projectId=1&content=小说内容..."
```

---

## 环境变量（可选）

敏感信息建议用环境变量：

```bash
export HUOSHAN_API_KEY="你的火山 API Key"
export KIMI_API_KEY="你的 Kimi API Key"
export OSS_ACCESS_KEY_ID="你的 OSS AccessKey"
export OSS_ACCESS_KEY_SECRET="你的 OSS Secret"
```

---

## 常见问题

### Q: 端口被占用

修改 `application.yml`:
```yaml
server:
  port: 8082  # 改成其他端口
```

### Q: 数据库连接失败

检查 `application-dev.yml` 中的数据库配置，确认：
- 数据库地址正确
- 用户名密码正确
- 数据库 `ai-translation` 已创建

### Q: API 调用失败

1. 检查 API Key 是否正确
2. 检查网络连接
3. 查看日志：`tail -f logs/novel2video.log`

---

## 下一步

1. **启动服务** → `./start.sh`
2. **测试接口** → 用 Postman 或 curl 测试
3. **集成前端** → 使用 `frontend/` 目录的 HTML 原型
4. **部署上线** → 参考 `ops/` 目录的运维文档

---

## 联系

有问题在论坛讨论：http://43.167.188.71/forum/novel2video/
