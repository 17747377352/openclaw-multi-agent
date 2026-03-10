#!/bin/bash
# Nginx 多服务配置安装脚本
# 自动安装 Nginx 并配置多服务映射

set -e

WORKSPACE="/home/ubuntu/.openclaw/workspace"
CONFIG_SRC="$WORKSPACE/deploy/nginx-multi-service.conf"
CONFIG_DEST="/etc/nginx/sites-available/multi-service"

echo "======================================"
echo "  Nginx 多服务配置安装"
echo "======================================"
echo ""

# 检查是否以 root 运行
if [ "$EUID" -ne 0 ]; then
    echo "❌ 请使用 sudo 运行此脚本"
    echo "   sudo $0"
    exit 1
fi

# 1. 安装 Nginx
echo "📦 检查 Nginx 安装状态..."
if ! command -v nginx &> /dev/null; then
    echo "⬇️  安装 Nginx..."
    apt update
    apt install -y nginx
    echo "✅ Nginx 安装完成"
else
    echo "✅ Nginx 已安装 ($(nginx -v 2>&1))"
fi
echo ""

# 2. 复制配置文件
echo "📋 复制配置文件..."
cp "$CONFIG_SRC" "$CONFIG_DEST"
echo "✅ 配置已复制到 $CONFIG_DEST"
echo ""

# 3. 启用配置
echo "🔗 启用配置..."
if [ -f /etc/nginx/sites-enabled/default ]; then
    echo "⚠️  检测到默认配置，建议禁用以避免冲突"
    read -p "是否禁用默认站点？(y/n): " disable_default
    if [ "$disable_default" = "y" ]; then
        rm -f /etc/nginx/sites-enabled/default
        echo "✅ 默认站点已禁用"
    fi
fi

ln -sf "$CONFIG_DEST" /etc/nginx/sites-enabled/multi-service
echo "✅ 配置已启用"
echo ""

# 4. 创建静态文件目录
echo "📁 创建静态文件目录..."
mkdir -p /home/ubuntu/.openclaw/workspace/static
echo "✅ 静态文件目录已创建"
echo ""

# 5. 测试配置
echo "🧪 测试 Nginx 配置..."
nginx -t
echo "✅ 配置测试通过"
echo ""

# 6. 重启 Nginx
echo "🔄 重启 Nginx 服务..."
systemctl restart nginx
systemctl enable nginx
echo "✅ Nginx 已重启并设置开机自启"
echo ""

# 7. 检查状态
echo "📊 Nginx 状态:"
systemctl status nginx --no-pager -l
echo ""

echo "======================================"
echo "  ✅ 安装完成!"
echo "======================================"
echo ""
echo "📍 服务访问地址:"
echo "   论坛服务：http://43.167.188.71/forum"
echo "   静态文件：http://43.167.188.71/static/"
echo ""
echo "📝 添加新服务:"
echo "   1. 编辑 $CONFIG_DEST"
echo "   2. 添加 upstream 和 location 配置"
echo "   3. 运行：sudo nginx -t && sudo systemctl reload nginx"
echo ""
echo "📖 日志位置:"
echo "   访问日志：/var/log/nginx/multi-service-access.log"
echo "   错误日志：/var/log/nginx/multi-service-error.log"
echo "======================================"
