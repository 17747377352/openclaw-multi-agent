# 小说转短视频平台 - 开发计划

## 项目状态

- ✅ 数据库设计完成（7 张表）
- ✅ 项目骨架搭建完成
- ✅ Entity/DTO/Mapper/Service/Controller 已创建
- ✅ 编译通过（100 个错误已修复）
- ✅ Developer Agent 配置 Kimi Code

---

## Phase 1: Mapper XML 配置（必须）

### 任务清单

- [ ] NovelProjectMapper.xml - 项目 CRUD
- [ ] ChapterMapper.xml - 章节批量插入
- [ ] ChapterGroupMapper.xml - 分组管理
- [ ] CharacterMapper.xml - 人物管理
- [ ] StoryboardMapper.xml - 分镜管理
- [ ] VideoTaskMapper.xml - 视频任务管理

**预计时间**: 2-3 小时  
**优先级**: 🔴 最高（必须，否则无法访问数据库）

---

## Phase 2: 业务逻辑完善

### 2.1 NovelService

- [ ] 小说上传接口实现
- [ ] TXT/EPUB 文件解析
- [ ] 章节自动分割算法
- [ ] 章节分组逻辑

### 2.2 CharacterService

- [ ] Kimi AI 人物提取
- [ ] 人物描述优化
- [ ] 豆包生图集成
- [ ] 人物审核流程

### 2.3 StoryboardService

- [ ] Kimi AI 分镜生成
- [ ] 提示词组装（含人物图）
- [ ] 首帧图生成
- [ ] 分镜审核流程

### 2.4 VideoService

- [ ] 火山视频生成 API 集成
- [ ] 任务状态轮询
- [ ] 失败重试机制
- [ ] 视频合成（可选）

### 2.5 OssService

- [ ] 文件上传封装
- [ ] 路径生成规则
- [ ] CDN 加速配置

**预计时间**: 2-3 天  
**优先级**: 🟡 高（核心业务逻辑）

---

## Phase 3: 前端页面实现

### 3.1 项目列表页

- [ ] 项目卡片展示
- [ ] 状态筛选
- [ ] 进度条显示
- [ ] 统计面板

### 3.2 人物审核页

- [ ] 人物列表展示
- [ ] AI 描述编辑
- [ ] 审核通过/拒绝
- [ ] 批量操作

### 3.3 分镜审核页

- [ ] 分镜预览
- [ ] 提示词编辑
- [ ] 首帧图显示
- [ ] 章节分组切换

### 3.4 视频任务管理页

- [ ] 任务队列展示
- [ ] 进度监控
- [ ] 重试机制
- [ ] 视频预览

**预计时间**: 2-3 天  
**优先级**: 🟡 高（用户界面）

---

## Phase 4: 配置优化

### 4.1 MyBatis 配置

- [ ] MybatisPlusConfig.java
- [ ] 分页插件
- [ ] 性能分析插件

### 4.2 跨域配置

- [ ] CorsConfig.java
- [ ] 允许的域名配置

### 4.3 异常处理

- [ ] GlobalExceptionHandler.java
- [ ] 自定义异常类
- [ ] 统一响应格式

### 4.4 日志配置

- [ ] logback-spring.xml
- [ ] 日志级别配置
- [ ] 日志文件滚动策略

**预计时间**: 1 天  
**优先级**: 🟢 中（提升稳定性）

---

## Phase 5: 测试与优化

### 5.1 单元测试

- [ ] Service 层测试
- [ ] Controller 层测试
- [ ] Mapper 层测试

### 5.2 集成测试

- [ ] API 接口测试
- [ ] 完整流程测试

### 5.3 性能优化

- [ ] SQL 优化
- [ ] 缓存配置
- [ ] 并发控制

**预计时间**: 1-2 天  
**优先级**: 🟢 中（提升质量）

---

## Phase 6: 部署准备

### 6.1 Docker 配置

- [ ] Dockerfile
- [ ] docker-compose.yml

### 6.2 生产环境配置

- [ ] application-prod.yml 完善
- [ ] 环境变量配置
- [ ] 数据库连接池优化

### 6.3 监控配置

- [ ] Spring Boot Admin
- [ ] Prometheus + Grafana
- [ ] 告警配置

**预计时间**: 1 天  
**优先级**: 🟢 中（生产就绪）

---

## 总览

| Phase | 内容 | 预计时间 | 状态 |
|-------|------|----------|------|
| Phase 1 | Mapper XML | 2-3 小时 | ⏳ 待开始 |
| Phase 2 | 业务逻辑 | 2-3 天 | ⏳ 待开始 |
| Phase 3 | 前端页面 | 2-3 天 | ⏳ 待开始 |
| Phase 4 | 配置优化 | 1 天 | ⏳ 待开始 |
| Phase 5 | 测试优化 | 1-2 天 | ⏳ 待开始 |
| Phase 6 | 部署准备 | 1 天 | ⏳ 待开始 |

**总计**: 约 8-10 天

---

## 开发日志

### 2026-03-10

- ✅ 项目骨架搭建完成
- ✅ 编译错误修复（100 个错误）
- ✅ Developer Agent 配置 Kimi Code
- ✅ 开发计划制定

---

## 备注

- 不着急，慢慢做
- 每个 Phase 完成后提交代码
- 遇到问题在论坛讨论：http://43.167.188.71/forum/novel2video/
