# 多服务 Nginx 配置

## 服务器信息

- **IP**: `43.167.188.71`
- **端口**: `80` (统一入口)
- **配置**: `/etc/nginx/sites-available/multi-service`

---

## 当前服务列表

| 服务 | 路径 | 后端端口 | 状态 |
|------|------|----------|------|
| 论坛 | `/forum` | 3000 | ⏳ 待部署 |
| Java API | `/api` | 8080 | ⏳ 预留 |
| Vue 前端 | `/app` | 5173 | ⏳ 预留 |
| 静态文件 | `/static` | - | ✅ 就绪 |

---

## 快速部署

### 一键安装 Nginx + 配置

```bash
sudo bash /home/ubuntu/.openclaw/workspace/deploy/install-nginx.sh
```

### 手动安装

```bash
# 1. 安装 Nginx
sudo apt update && sudo apt install -y nginx

# 2. 复制配置
sudo cp /home/ubuntu/.openclaw/workspace/deploy/nginx-multi-service.conf /etc/nginx/sites-available/multi-service

# 3. 启用配置
sudo ln -s /etc/nginx/sites-available/multi-service /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# 4. 测试并重载
sudo nginx -t && sudo systemctl reload nginx
```

---

## 添加新服务

### 步骤 1: 定义上游

编辑 `/etc/nginx/sites-available/multi-service`:

```nginx
upstream my_service {
    server 127.0.0.1:9000;  # 你的服务端口
}
```

### 步骤 2: 配置路径

```nginx
location /myservice {
    proxy_pass http://my_service;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

### 步骤 3: 测试并重载

```bash
sudo nginx -t && sudo systemctl reload nginx
```

---

## 访问示例

```
论坛服务：http://43.167.188.71/forum
Java 接口：http://43.167.188.71/api/xxx
Vue 应用：http://43.167.188.71/app
静态资源：http://43.167.188.71/static/logo.png
```

---

## 日志管理

```bash
# 查看实时日志
tail -f /var/log/nginx/multi-service-access.log
tail -f /var/log/nginx/multi-service-error.log

# 查看最近 100 行
tail -n 100 /var/log/nginx/multi-service-error.log
```

---

## 常用命令

```bash
# 检查状态
sudo systemctl status nginx

# 重启
sudo systemctl restart nginx

# 重载配置 (不中断服务)
sudo systemctl reload nginx

# 测试配置
sudo nginx -t
```

---

## 安全建议

1. **防火墙**: 只开放 80/443 端口
   ```bash
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw enable
   ```

2. **HTTPS**: 配置 SSL 证书后启用 443 端口

3. **限流**: 防止滥用
   ```nginx
   location /forum {
       limit_req zone=one burst=10;
       proxy_pass http://forum_backend;
   }
   ```
