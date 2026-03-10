# 小说转短视频项目 - 运维文档索引

## 📁 文档列表

| 序号 | 文档 | 说明 |
|------|------|------|
| 01 | [OSS 存储目录结构](./01-oss-storage-structure.md) | Bucket 规划、目录结构、命名规范、生命周期管理 |
| 02 | [API 调用成本估算](./02-api-cost-estimation.md) | 火山/Kimi/OSS 价格、单本成本、月度预算、优化建议 |
| 03 | [Redis 任务队列方案](./03-redis-queue-solution.md) | Key 设计、队列流程、Java 实现、监控指标 |
| 04 | [Nginx 配置建议](./04-nginx-config.md) | 主配置、项目配置、SSL、性能优化、安全加固 |

---

## 🚀 快速开始

### 1. 创建 OSS 目录结构

```bash
# 参考文档 01，在 OSS 控制台创建目录
# 或调用 OSS SDK 自动创建
```

### 2. 配置 Redis 队列

```bash
# 连接 Redis
redis-cli -h 123.56.22.101 -p 16379 -a nnnnn520

# 验证队列 Key
KEYS novel2video:queue:*
```

### 3. 部署 Nginx 配置

```bash
# 复制配置文件
sudo cp novel2video.conf /etc/nginx/conf.d/

# 检查语法
sudo nginx -t

# 重载配置
sudo nginx -s reload
```

---

## 📊 成本速查

| 项目 | 单本成本（100 章） |
|------|------------------|
| 火山生图 | ¥18.00 |
| 火山视频 | ¥80.00 |
| Kimi 文本 | ¥4.20 |
| OSS 存储 | ¥0.19/月 |
| **合计** | **¥102.39** |

---

## 🔧 关键配置

### Redis 连接
```
Host: 123.56.22.101:16379
Password: nnnnn520
Database: 0
```

### OSS 配置
```
Bucket: translation-service
Region: cn-beijing
Endpoint: https://oss-cn-beijing.aliyuncs.com
```

### API 模型
```
生图：doubao-seedream-4-5-251128
视频：doubao-seedance-1-5-pro-251215
文本：kimi-latest
```

---

## 📝 更新记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-03-10 | v1.0 | 初始版本 |

---

**文档生成**: 2026-03-10  
**维护**: Ops Agent
