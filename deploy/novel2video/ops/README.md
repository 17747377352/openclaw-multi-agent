# 小说转短视频项目 - 运维文档索引

## 📁 文档列表

| 序号 | 文档 | 说明 |
|------|------|------|
| 01 | [OSS 存储目录结构](./01-oss-storage-structure.md) | Bucket 规划、目录结构、命名规范、生命周期管理 |
| 02 | [API 调用成本估算](./02-api-cost-estimation.md) | 火山/Kimi/OSS 价格、单本成本、月度预算、优化建议 |
| 03 | [Redis 任务队列方案](./03-redis-queue-solution.md) | Key 设计、队列流程、Java 实现、监控指标 |
| 04 | [Nginx 配置建议](./04-nginx-config.md) | 主配置、项目配置、SSL、性能优化、安全加固 |
| 05 | [Admin Web Nginx 部署](./05-admin-web-nginx.md) | Vue 管理端打包、上传、子路径部署、反向代理 |
| 06 | [项目数据清理 SQL](./06-clean-project-data.sql) | 清空业务表数据并重置自增 ID |
| 07 | [风格圣经模板](./07-style-bible-template.md) | 2D/2.5D 项目的统一视觉规范模板 |
| 08 | [角色卡模板](./08-character-card-template.md) | 核心人物一致性标准模板 |
| 09 | [分镜提示词规范](./09-storyboard-prompt-spec.md) | 可直接执行的分镜提示词写法 |
| 10 | [示例风格圣经（斗破苍穹）](./10-example-style-bible-dpcq.md) | 以斗破苍穹前期篇章为样板的完整风格设定 |
| 11 | [示例角色卡（斗破苍穹）](./11-example-character-cards-dpcq.md) | 萧炎、药老、纳兰嫣然、薰儿的角色标准件 |
| 12 | [示例分镜提示词（斗破苍穹）](./12-example-storyboard-prompts-dpcq.md) | 6 条可直接落地的首帧图/视频提示词 |

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
# 检查语法
sudo nginx -t

# 重载配置
sudo nginx -s reload
```

### 4. 建立内容生产标准

建议顺序：

1. 先填写 [风格圣经模板](./07-style-bible-template.md)
2. 再给核心角色建立 [角色卡模板](./08-character-card-template.md)
3. 最后按 [分镜提示词规范](./09-storyboard-prompt-spec.md) 批量产出镜头
4. 参考 [斗破苍穹示例风格圣经](./10-example-style-bible-dpcq.md)、[示例角色卡](./11-example-character-cards-dpcq.md)、[示例分镜提示词](./12-example-storyboard-prompts-dpcq.md) 快速套模板

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
| 2026-03-12 | v1.1 | 新增 Admin Web 部署、数据清理 SQL、2D 动态漫画生产模板 |
| 2026-03-12 | v1.2 | 新增斗破苍穹样板风格圣经、角色卡、分镜提示词示例 |

---

**文档生成**: 2026-03-10  
**维护**: Ops Agent
