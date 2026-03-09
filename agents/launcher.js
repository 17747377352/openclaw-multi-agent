/**
 * Multi-Agent Launcher
 * Main Agent 使用此模块启动其他 Agent
 * 
 * 用法：在 Main Agent 中调用 launchAll() 或 launchSingle('developer')
 */

const CONFIG = {
  workspace: "/home/ubuntu/.openclaw/workspace",
  
  // ============ 模型配置 (可修改) ============
  models: {
    main: "bailian/qwen3.5-plus",        // 百炼
    developer: "bailian/qwen3-coder-plus", // 百炼-coder
    designer: "bailian/qwen3.5-plus",     // 百炼
    ops: "bailian/qwen3.5-plus"           // 百炼
  }
  // ===========================================
};

const AGENTS = {
  developer: {
    label: "developer",
    task: "阅读 agents/developer.md，等待开发任务",
    model: CONFIG.models.developer
  },
  designer: {
    label: "designer",
    task: "阅读 agents/designer.md，等待设计任务",
    model: CONFIG.models.designer
  },
  ops: {
    label: "ops",
    task: "阅读 agents/ops.md，等待运维任务",
    model: CONFIG.models.ops
  }
};

/**
 * 启动单个 Agent
 * @param {string} agentKey - 'developer' | 'designer' | 'ops'
 */
async function launchSingle(agentKey) {
  const agent = AGENTS[agentKey];
  if (!agent) {
    throw new Error(`Unknown agent: ${agentKey}`);
  }
  
  console.log(`🚀 启动 ${agent.label} (模型：${agent.model})`);
  
  // 调用 OpenClaw sessions_spawn
  const session = await sessions_spawn({
    runtime: "subagent",
    mode: "session",
    model: agent.model,
    task: agent.task,
    label: agent.label,
    cwd: CONFIG.workspace
  });
  
  console.log(`✅ ${agent.label} 已启动，sessionKey: ${session.sessionKey}`);
  return session;
}

/**
 * 启动所有 Agent
 */
async function launchAll() {
  console.log("======================================");
  console.log("  Multi-Agent System Launcher");
  console.log("======================================");
  console.log("");
  
  const sessions = {};
  
  for (const [key, agent] of Object.entries(AGENTS)) {
    try {
      sessions[key] = await launchSingle(key);
    } catch (error) {
      console.error(`❌ 启动 ${key} 失败：${error.message}`);
    }
  }
  
  console.log("");
  console.log("======================================");
  console.log(`✅ 已启动 ${Object.keys(sessions).length} 个 Agent`);
  console.log("======================================");
  
  return sessions;
}

/**
 * 切换 Agent 模型
 * @param {string} sessionKey - 会话 ID
 * @param {string} newModel - 新模型 ID
 */
async function switchModel(sessionKey, newModel) {
  await session_status({
    sessionKey: sessionKey,
    model: newModel
  });
  console.log(`🔄 已切换模型：${newModel}`);
}

// 导出供 Main Agent 使用
module.exports = { launchSingle, launchAll, switchModel, CONFIG, AGENTS };
