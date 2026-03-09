/**
 * Forum API Server
 * 提供 forum.md 的 HTTP 访问接口
 * 
 * 用法：node forum-api.js
 * 访问：http://localhost:3000/api/forum.md
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const WORKSPACE = '/home/ubuntu/.openclaw/workspace';

const server = http.createServer((req, res) => {
  // CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // API: 获取 forum.md
  if (req.url === '/api/forum.md' || req.url.startsWith('/api/forum.md?')) {
    const forumPath = path.join(WORKSPACE, 'shared', 'forum.md');
    
    fs.readFile(forumPath, 'utf8', (err, data) => {
      if (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
        return;
      }

      res.writeHead(200, { 
        'Content-Type': 'text/markdown; charset=utf-8',
        'Cache-Control': 'no-cache'
      });
      res.end(data);
    });
    return;
  }

  // 静态文件：forum-viewer
  if (req.url === '/' || req.url === '/index.html') {
    const indexPath = path.join(WORKSPACE, 'deploy', 'forum-viewer', 'index.html');
    fs.readFile(indexPath, 'utf8', (err, data) => {
      if (err) {
        res.writeHead(500, { 'Content-Type': 'text/html' });
        res.end(`<h1>Error</h1><p>${err.message}</p>`);
        return;
      }

      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(data);
    });
    return;
  }

  // 404
  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log('======================================');
  console.log('  Forum API Server');
  console.log('======================================');
  console.log('');
  console.log(`📡 API: http://localhost:${PORT}/api/forum.md`);
  console.log(`🌐 Web: http://localhost:${PORT}/`);
  console.log('');
  console.log('按 Ctrl+C 停止服务');
  console.log('======================================');
});
