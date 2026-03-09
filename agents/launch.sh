#!/bin/bash
# Multi-Agent Launch Script
# 启动所有 Agent，模型配置可在此文件中修改

WORKSPACE="/home/ubuntu/.openclaw/workspace"

# ============ 模型配置区 ============
# 修改这里的模型 ID 即可切换 Agent 使用的模型
# 可用模型：
#   - bailian/qwen3.5-plus        (百炼，通用)
#   - bailian/qwen3-coder-plus    (百炼-coder，代码)
#   - bailian/qwen3-max-2026-01-23 (百炼-max，复杂推理)

MODEL_MAIN="bailian/qwen3.5-plus"
MODEL_DEVELOPER="bailian/qwen3-coder-plus"
MODEL_DESIGNER="bailian/qwen3.5-plus"
MODEL_OPS="bailian/qwen3.5-plus"

# ============ 启动函数 ============

launch_agent() {
    local label=$1
    local model=$2
    local task=$3
    
    echo "🚀 启动 $label (模型：$model)"
    
    # 使用 OpenClaw CLI 启动子 Agent
    # 注意：实际需要通过 API 或 sessions_spawn 调用
    # 这里提供配置，实际启动由 Main Agent 执行
    
    cat << EOF
{
  "runtime": "subagent",
  "mode": "session",
  "model": "$model",
  "task": "$task",
  "label": "$label",
  "cwd": "$WORKSPACE"
}
EOF
    echo ""
}

# ============ 主程序 ============

echo "======================================"
echo "  Multi-Agent System Launcher"
echo "======================================"
echo ""
echo "📋 模型配置:"
echo "   Main:      $MODEL_MAIN"
echo "   Developer: $MODEL_DEVELOPER"
echo "   Designer:  $MODEL_DESIGNER"
echo "   Ops:       $MODEL_OPS"
echo ""
echo "======================================"
echo ""

# 生成各 Agent 启动配置
launch_agent "main" "$MODEL_MAIN" "阅读 agents/main.md，协调其他 Agent 工作"
launch_agent "developer" "$MODEL_DEVELOPER" "阅读 agents/developer.md，等待开发任务"
launch_agent "designer" "$MODEL_DESIGNER" "阅读 agents/designer.md，等待设计任务"
launch_agent "ops" "$MODEL_OPS" "阅读 agents/ops.md，等待运维任务"

echo "======================================"
echo "✅ 配置生成完成"
echo ""
echo "💡 使用方法:"
echo "   1. 修改上方模型配置区"
echo "   2. 在 Main Agent 中调用 sessions_spawn 启动"
echo "   3. 或直接复制上方 JSON 配置使用"
echo "======================================"
