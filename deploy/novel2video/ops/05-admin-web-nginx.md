# Admin Web 部署示例

## 1. 打包

如果前端挂在域名根路径：

```bash
cd admin-web
npm install
npm run build
```

如果前端挂在 `/novel-admin/`：

```bash
cd admin-web
npm install
VITE_PUBLIC_BASE=/novel-admin/ npm run build
```

## 2. 上传静态文件

```bash
mkdir -p /var/www/novel2video/admin-web
rsync -av --delete dist/ /var/www/novel2video/admin-web/
```

## 3. Nginx 配置示例

假设：

- 前端访问路径：`https://your-domain.com/novel-admin/`
- Spring Boot 后端：`127.0.0.1:8081`
- 后端接口前缀：`/api/`

```nginx
server {
    listen 80;
    server_name your-domain.com;

    client_max_body_size 100m;

    # Vite 构建产物的静态资源缓存
    location /novel-admin/assets/ {
        alias /var/www/novel2video/admin-web/assets/;
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
    }

    # Vue 管理端入口
    location /novel-admin/ {
        alias /var/www/novel2video/admin-web/;
        index index.html;
        try_files $uri $uri/ /novel-admin/index.html;
    }

    # Spring Boot API
    location /api/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_connect_timeout 60s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;

        proxy_buffering off;
        proxy_request_buffering off;
    }
}
```

## 4. 如果走 HTTPS

把上面的 `server` 改成常规 `443 ssl http2` 配置即可，核心不变：

- `/novel-admin/` 指向 `dist/`
- `/api/` 反代到 `8081`

## 5. 验证

```bash
nginx -t
systemctl reload nginx
```

浏览器访问：

- `https://your-domain.com/novel-admin/`
- 页面里的 API 请求会自动走同域 `/api/`
