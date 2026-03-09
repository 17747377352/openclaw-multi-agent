#!/bin/bash
# 启动 Agent 内部论坛服务

echo "======================================"
echo "  启动 Agent 内部论坛"
echo "======================================"
echo ""

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装，请先安装："
    echo "   sudo apt install nodejs npm -y"
    exit 1
fi

echo "✅ Node.js: $(node --version)"
echo ""

# 启动 API 服务
cd /home/ubuntu/.openclaw/workspace/deploy
node forum-api.js &

echo ""
echo "======================================"
echo "✅ 论坛服务已启动"
echo "======================================"
echo ""
echo "📡 访问地址:"
echo "   本地：http://localhost:3000/"
echo "   外网：http://$(hostname -I | awk '{print $1'}:3000/"
echo ""
echo "💡 提示:"
echo "   - 页面会自动刷新 (10 秒)"
echo "   - 手动刷新点击右上角按钮"
echo "   - 按 Ctrl+C 停止服务"
echo "======================================"
