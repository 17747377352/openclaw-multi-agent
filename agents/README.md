# Multi-Agent System

## 架构

```
┌─────────────┐
│   Main      │ ← 用户交互入口，决策协调
│  Agent      │
└──────┬──────┘
       │ sessions_send / sessions_spawn
       ├─────────────┬─────────────┬─────────────┐
       ▼             ▼             ▼             ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│Developer │  │ Designer │  │   Ops    │  │  Shared  │
│  Agent   │  │  Agent   │  │  Agent   │  │  Memory  │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

## Agent 列表

| Agent | 文件 | 职责 |
|-------|------|------|
| Main | `main.md` | 决策、协调、输出 |
| Developer | `developer.md` | 前后端开发 |
| Designer | `designer.md` | 产品设计、需求 |
| Ops | `ops.md` | 测试、部署、运维 |

## 通信方式

### 1. 直接消息
```
sessions_send({
  sessionKey: "developer-session",
  message: "创建用户登录接口"
})
```

### 2. 共享消息板
读写 `../shared/message-board.md`

### 3. 共享记忆
读写 `../MEMORY.md` 和 `../memory/YYYY-MM-DD.md`

## 启动子 Agent

```javascript
// Main Agent 启动其他 Agent
sessions_spawn({
  runtime: "subagent",
  mode: "session",
  task: "阅读 agents/developer.md，等待任务",
  label: "developer"
})
```

## 协作流程

### 模式 A: 论坛讨论 (推荐)

1. **Main** 在 `shared/forum.md` 创建讨论帖
2. **Main** 通知各 Agent 参与讨论
3. 各 Agent 阅读需求，发表专业意见
4. **Main** 汇总讨论，形成最终方案
5. 用户确认后，分发执行任务
6. 各 Agent 执行，**Main** 汇总交付

### 模式 B: 直接任务分发

1. **Main** 接收用户需求
2. **Main** 分析需要哪些 Agent
3. **Main** 通过 `sessions_send` 分发任务
4. 各 Agent 执行任务，更新共享状态
5. **Main** 汇总结果，输出给用户
