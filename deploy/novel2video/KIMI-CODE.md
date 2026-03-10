# Kimi Code 接入配置

## Kimi Code 模型

**Kimi Code** 是月之暗面推出的代码专用模型，擅长：
- 代码生成
- 代码审查
- Bug 修复
- 技术文档编写

## 配置信息

| 配置项 | 值 |
|--------|-----|
| **Base URL** | `https://api.moonshot.cn/v1` |
| **模型名称** | `kimi-code-preview` |
| **API Key** | `sk-BI29b3F3D50HfPa72NQgHhPbmtoja26hyebt6xTeNU49Ctzm` ✅ 已验证 |

## 当前状态

✅ **密钥已验证可用**

**注意**：Kimi API 偶尔会返回 `engine_overloaded_error`（服务过载），请稍后重试。

## 解决方案

### 方案 1: 使用官方密钥

1. 登录 [Kimi 开放平台](https://platform.moonshot.cn/console)
2. 创建新的 API Key
3. 更新配置文件：

```yaml
ai:
  kimi:
    api-key: sk-xxxxxxxxxxxx  # 你的官方密钥
    model: kimi-code-preview
```

### 方案 2: 继续使用备用密钥

当前配置已回退到备用密钥：
```yaml
ai:
  kimi:
    api-key: sk-BI29b3F3D50HfPa72NQgHhPbmtoja26hyebt6xTeNU49Ctzm
    model: kimi-latest
```

### 方案 3: 使用第三方 API

如果密钥来自第三方平台，需要确认：
- 平台的 API Base URL
- 认证方式
- 模型名称映射

## 模型对比

| 模型 | 用途 | 状态 |
|------|------|------|
| `kimi-code-preview` | 代码生成/审查 | ⏳ 待验证 |
| `kimi-k2-0711-preview` | Kimi 2.5（通用） | ⏳ 待验证 |
| `kimi-latest` | 经典版（通用） | ✅ 可用 |

## 测试命令

```bash
# 测试 Kimi Code
curl -X POST https://api.moonshot.cn/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -d '{
    "model": "kimi-code-preview",
    "messages": [{"role": "user", "content": "print(\"Hello\")"}]
  }'
```

## 参考链接

- 官方文档：https://platform.moonshot.cn/
- 控制台：https://platform.moonshot.cn/console
- API 文档：https://platform.moonshot.cn/docs/api
