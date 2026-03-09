# Multi-Agent 协作流程

## 完整工作流

```
┌─────────────────────────────────────────────────────────────────┐
│  1. 用户提出需求                                                 │
│     ↓                                                           │
│  2. Main 在 shared/forum.md 创建讨论帖                           │
│     ↓                                                           │
│  3. Main 通知各 Agent 参与讨论                                   │
│     ↓                                                           │
│  4. 各 Agent 阅读需求，发表专业意见                              │
│     ↓                                                           │
│  5. Main 汇总讨论，形成最终方案                                  │
│     ↓                                                           │
│  6. Main 向用户输出方案，等待确认                                │
│     ↓                                                           │
│  7. 用户确认后，分发执行任务                                     │
│     ↓                                                           │
│  8. 各 Agent 执行任务，更新进度                                  │
│     ↓                                                           │
│  9. Main 汇总结果，交付用户                                      │
└─────────────────────────────────────────────────────────────────┘
```

## 详细步骤

### 步骤 1-2: 创建讨论帖

**Main Agent 操作:**
```javascript
// 1. 读取论坛模板
const forum = read('shared/forum.md');

// 2. 创建新讨论帖
const newTopic = `
### [讨论中] ${需求标题}

**发起**: @main  
**时间**: ${当前时间}  
**状态**: 🟡 讨论中

#### 原始需求
> ${用户需求内容}
`;

// 3. 写入论坛
edit('shared/forum.md', insert(newTopic));

// 4. 通知各 Agent
sessions_send({ label: 'developer', message: '新需求讨论已发布，请查看 shared/forum.md' });
sessions_send({ label: 'designer', message: '新需求讨论已发布，请查看 shared/forum.md' });
sessions_send({ label: 'ops', message: '新需求讨论已发布，请查看 shared/forum.md' });
```

### 步骤 3-4: Agent 发表意见

**各 Agent 操作:**
```javascript
// 1. 阅读论坛，获取需求
const forum = read('shared/forum.md');

// 2. 分析需求，撰写专业意见
const opinion = `
#### @${agentName} 的意见
**时间**: ${当前时间}  
**状态**: ✅ 已发表

技术/产品/运维实现思路：
- 点 1
- 点 2
- ...
`;

// 3. 更新论坛
edit('shared/forum.md', replaceSection(opinion));
```

### 步骤 5: 汇总方案

**Main Agent 操作:**
```javascript
// 1. 收集所有 Agent 意见
const developerOpinion = extractOpinion('developer');
const designerOpinion = extractOpinion('designer');
const opsOpinion = extractOpinion('ops');

// 2. 综合讨论，形成方案
const finalPlan = `
#### 📋 最终方案

**综合意见**:
- 技术：${developerOpinion.summary}
- 产品：${designerOpinion.summary}
- 运维：${opsOpinion.summary}

**执行步骤**:
1. ...
2. ...

**预计时间**: ...
`;

// 3. 更新论坛
edit('shared/forum.md', replaceSection(finalPlan));
```

### 步骤 6-9: 执行与交付

**Main Agent 操作:**
```javascript
// 1. 向用户输出方案
sendToUser(finalPlan);

// 2. 等待用户确认
if (userConfirmed) {
  // 3. 分发执行任务
  sessions_send({ label: 'developer', message: '执行任务：...' });
  sessions_send({ label: 'ops', message: '执行任务：...' });
  
  // 4. 跟踪进度，汇总结果
  // ...
}
```

## 状态标记

| 状态 | 标记 | 含义 |
|------|------|------|
| 讨论中 | 🟡 | 正在收集意见 |
| 待发表 | ⏳ | Agent 尚未发表意见 |
| 已发表 | ✅ | Agent 已发表意见 |
| 已形成 | ✅ | 最终方案已确定 |
| 执行中 | 🔵 | 方案正在执行 |
| 已完成 | 🟢 | 任务完成 |
| 已归档 | ⚪ | 讨论归档到历史 |

## 争议处理

如果 Agent 间意见不一致：

1. 各 Agent 在论坛中说明理由
2. @main 组织进一步讨论
3. @main 做出最终裁决
4. 记录决策原因

---

## 示例讨论

参考 `shared/forum.md` 中的示例帖子。
