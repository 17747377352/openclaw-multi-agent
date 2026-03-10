# 小说转短视频项目 - Nginx 配置建议

## 📌 服务器信息

- **域名**: `www.songtop.xyz`
- **API 路径**: `/translation/api/`
- **视频播放**: 需支持大文件流式传输

---

## 📁 目录结构

```bash
/etc/nginx/
├── nginx.conf                    # 主配置
├── conf.d/
│   ├── novel2video.conf          # 项目配置
│   └── ssl.conf                  # SSL 通用配置
├── ssl/
│   ├── songtop.xyz.crt
│   └── songtop.xyz.key
└── logs/
    └── novel2video/
        ├── access.log
        └── error.log
```

---

## 🔧 主配置 (nginx.conf)

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 4096;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for" '
                    'rt=$request_time uct="$upstream_connect_time" '
                    'uht="$upstream_header_time" urt="$upstream_response_time"';

    access_log /var/log/nginx/access.log main;

    # 性能优化
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 100M;  # 支持大文件上传

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml application/json 
               application/javascript application/xml;

    # 包含其他配置
    include /etc/nginx/conf.d/*.conf;
}
```

---

## 🌐 项目配置 (novel2video.conf)

```nginx
# 上游服务
upstream novel2video_backend {
    least_conn;
    server 127.0.0.1:8080 weight=1 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name www.songtop.xyz songtop.xyz;
    
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html;
    }
    
    location / {
        return 301 https://$host$request_uri;
    }
}

# HTTPS 主服务
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name www.songtop.xyz;

    # SSL 证书
    ssl_certificate /etc/nginx/ssl/songtop.xyz.crt;
    ssl_certificate_key /etc/nginx/ssl/songtop.xyz.key;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_session_tickets off;
    
    # 安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    ssl_prefer_server_ciphers off;
    
    # HSTS
    add_header Strict-Transport-Security "max-age=63072000" always;

    # 日志
    access_log /var/log/nginx/novel2video/access.log main;
    error_log /var/log/nginx/novel2video/error.log warn;

    # 静态资源（OSS 代理）
    location /static/ {
        alias /var/www/novel2video/static/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 视频播放（OSS 代理，支持 Range 请求）
    location /video/ {
        alias /var/www/novel2video/videos/;
        
        # 支持视频拖拽播放
        add_header Accept-Ranges bytes;
        add_header Cache-Control "public, max-age=2592000";
        
        # 跨域支持（小程序需要）
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods 'GET, OPTIONS';
        
        # 视频类型
        types {
            video/mp4 mp4;
            video/webm webm;
        }
    }

    # API 接口
    location /translation/api/ {
        proxy_pass http://novel2video_backend;
        proxy_http_version 1.1;
        
        # 请求头
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection "";
        
        # 超时设置（视频生成需要长时间）
        proxy_connect_timeout 60s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;
        
        # 缓冲
        proxy_buffering off;
        proxy_request_buffering off;
        
        # 大文件上传
        client_max_body_size 100M;
    }

    # 文件上传接口（单独配置）
    location /translation/api/upload/ {
        proxy_pass http://novel2video_backend;
        proxy_http_version 1.1;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # 上传超时更长
        proxy_read_timeout 600s;
        client_max_body_size 100M;
        
        # 关闭缓冲（实时上传）
        proxy_buffering off;
        proxy_request_buffering off;
    }

    # WebSocket 支持（进度推送）
    location /translation/ws/ {
        proxy_pass http://novel2video_backend;
        proxy_http_version 1.1;
        
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
    }

    # 健康检查
    location /health {
        proxy_pass http://novel2video_backend/actuator/health;
        access_log off;
    }

    # 错误页面
    error_page 404 /404.html;
    error_page 500 502 503 504 /50x.html;
    
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

---

## 🔒 SSL 证书配置 (ssl.conf)

```nginx
# OCSP Stapling
ssl_stapling on;
ssl_stapling_verify on;
resolver 8.8.8.8 8.8.4.4 valid=300s;
resolver_timeout 5s;

# DH 参数（可选，增强安全性）
# ssl_dhparam /etc/nginx/ssl/dhparam.pem;
```

---

## 🚀 性能优化配置

### 1. 系统级优化 (/etc/sysctl.conf)

```bash
# 增加文件描述符限制
fs.file-max = 65535

# 增加端口范围
net.ipv4.ip_local_port_range = 1024 65535

# 启用 TCP 重用
net.ipv4.tcp_tw_reuse = 1

# 增加连接队列
net.core.somaxconn = 65535
```

应用：`sysctl -p`

### 2. 用户限制 (/etc/security/limits.conf)

```bash
nginx soft nofile 65535
nginx hard nofile 65535
```

### 3. Systemd 服务优化

```ini
# /etc/systemd/system/nginx.service.d/override.conf
[Service]
LimitNOFILE=65535
LimitNPROC=65535
```

---

## 📊 日志切割 (logrotate)

```bash
# /etc/logrotate.d/nginx-novel2video
/var/log/nginx/novel2video/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 nginx nginx
    sharedscripts
    postrotate
        [ -f /var/run/nginx.pid ] && kill -USR1 `cat /var/run/nginx.pid`
    endscript
}
```

---

## 🛡️ 安全加固

### 1. 限制请求频率

```nginx
# 在 http 块中添加
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
limit_conn_zone $binary_remote_addr zone=conn_limit:10m;

# 在 location 中使用
location /translation/api/ {
    limit_req zone=api_limit burst=20 nodelay;
    limit_conn conn_limit 10;
    # ...
}
```

### 2. 屏蔽恶意 IP

```nginx
# 在 server 块开头添加
deny 1.2.3.4;  # 恶意 IP
allow all;
```

### 3. 隐藏版本信息

```nginx
server_tokens off;
```

---

## 🔍 监控与调试

### 1. 查看实时日志

```bash
# 访问日志
tail -f /var/log/nginx/novel2video/access.log

# 错误日志
tail -f /var/log/nginx/novel2video/error.log
```

### 2. 检查配置语法

```bash
nginx -t
```

### 3. 平滑重载配置

```bash
nginx -s reload
```

### 4. 查看连接状态

```bash
# 需要启用 stub_status 模块
curl http://localhost/nginx_status
```

---

## 🎯 最佳实践

1. **启用 HTTP/2** - 提升页面加载速度
2. **配置 HSTS** - 强制 HTTPS 连接
3. **合理设置超时** - 视频生成需要长超时
4. **关闭不必要缓冲** - 大文件上传更稳定
5. **定期更新证书** - 使用 Let's Encrypt 自动续期
6. **监控日志大小** - 避免磁盘爆满
