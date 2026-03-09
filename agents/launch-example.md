# Agent 启动示例 - 不同模型配置

## Main Agent 启动子 Agent 示例

```javascript
// 启动 Developer Agent (用 coder 模型)
const devSession = await sessions_spawn({
  runtime: "subagent",
  mode: "session",
  model: "bailian/qwen3-coder-plus",  // 百炼-coder
  task: "阅读 agents/developer.md，等待开发任务",
  label: "developer",
  cwd: "/home/ubuntu/.openclaw/workspace"
});

// 启动 Designer Agent (用 max 模型做创意)
const designSession = await sessions_spawn({
  runtime: "subagent",
  mode: "session",
  model: "bailian/qwen3-max-2026-01-23",  // 百炼-max
  task: "阅读 agents/designer.md，等待设计任务",
  label: "designer",
  cwd: "/home/ubuntu/.openclaw/workspace"
});

// 启动 Ops Agent (用 plus 模型)
const opsSession = await sessions_spawn({
  runtime: "subagent",
  mode: "session",
  model: "bailian/qwen3.5-plus",  // 百炼
  task: "阅读 agents/ops.md，等待运维任务",
  label: "ops",
  cwd: "/home/ubuntu/.openclaw/workspace"
});
```

## 模型别名对照

| 别名 | 完整模型 ID | 适用场景 |
|------|-------------|----------|
| `bailian/qwen3.5-plus` | 百炼 | 通用任务 |
| `bailian/qwen3-coder-plus` | 百炼-coder | 代码生成 |
| `bailian/qwen3-max-2026-01-23` | 百炼-max | 复杂推理/创意 |

## 动态切换模型

```javascript
// 任务中需要切换模型时，用 session_status 设置
await session_status({
  sessionKey: devSession.sessionKey,
  model: "bailian/qwen3-max-2026-01-23"  // 临时切换到 max
});
```

## 成本优化建议

- **日常开发**: `qwen3.5-plus` (性价比高)
- **复杂算法**: `qwen3-coder-plus` (代码能力强)
- **产品创意**: `qwen3-max` (推理能力强，贵)
- **Main 决策**: `qwen3.5-plus` 或 `qwen3-max` (看任务复杂度)
