#!/bin/bash
# 小说转短视频平台 - 启动脚本

echo "======================================"
echo "  小说转短视频平台"
echo "======================================"
echo ""

cd /home/ubuntu/.openclaw/workspace/deploy/novel2video

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装"
    echo "   请运行：sudo apt install maven"
    exit 1
fi

echo "✅ Maven: $(mvn --version | head -1)"
echo ""

# 构建项目
echo "🔨 构建项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

echo "✅ 构建完成"
echo ""

# 启动服务
echo "🚀 启动服务..."
echo "💡 提示：按 Ctrl+C 停止服务"
echo ""

java -jar target/novel2video-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
