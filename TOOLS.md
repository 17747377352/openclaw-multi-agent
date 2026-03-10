# TOOLS.md - Local Notes

Skills define _how_ tools work. This file is for _your_ specifics — the stuff that's unique to your setup.

## What Goes Here

Things like:

- Camera names and locations
- SSH hosts and aliases
- Preferred voices for TTS
- Speaker/room names
- Device nicknames
- Anything environment-specific

## Examples

```markdown
### Cameras

- living-room → Main area, 180° wide angle
- front-door → Entrance, motion-triggered

### SSH

- home-server → 192.168.1.100, user: admin

### TTS

- Preferred voice: "Nova" (warm, slightly British)
- Default speaker: Kitchen HomePod
```

## Why Separate?

Skills are shared. Your setup is yours. Keeping them apart means you can update skills without losing your notes, and share skills without leaking your infrastructure.

---

Add whatever helps you do your job. This is your cheat sheet.

---

## 中间件配置 (application-dev.yml)

### MySQL
- **Host**: `123.56.22.101:13306`
- **Database**: `ai-translation`
- **Username**: `root`
- **Password**: `nnnnn520`
- **连接池**: Druid (initial=5, min-idle=5, max-active=20)
- **JDBC URL**: `jdbc:mysql://123.56.22.101:13306/ai-translation?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true`

### Redis
- **Host**: `123.56.22.101:16379`
- **Password**: `nnnnn520`
- **Database**: `0`
- **连接池**: max-active=8, max-idle=8, min-idle=0

### 火山引擎 Ark (AI)
- **API Key**: `8a1298b6-a9fa-4cd5-b915-fee0bfe8ec90`
- **Base URL**: `https://ark.cn-beijing.volces.com/api/v3`
- **生图模型**: `doubao-seedream-4-5-251128` (人物照/分镜图)
- **视频生成**: `doubao-seedance-1-5-pro-251215` (图生视频)
- **OCR 模型**: `doubao-1-5-vision-pro-32k-250115`

### Kimi (Moonshot AI)
- **API Key**: `sk-BI29b3F3D50HfPa72NQgHhPbmtoja26hyebt6xTeNU49Ctzm` ✅ 已验证（服务偶尔过载）
- **Base URL**: `https://api.moonshot.cn/v1`
- **经典模型**: `kimi-latest` ✅ 可用
- **Kimi Code**: `kimi-code-preview` (代码专用)
- **Kimi 2.5**: `kimi-k2-0711-preview`

### 微信小程序
- **AppID**: `wxd9e44d7cb4c73f79`
- **Secret**: `01bb8be94f036b10a62027b8a8038d39`
- **商户号**: `1626229064`
- **API Key**: `Hhh9BmRp6vNEkaJDf52eMnN46CVOwuqz`
- **回调 URL**: `https://www.songtop.xyz/translation/api/pay/notify`

### 阿里云 OSS
- **Endpoint**: `https://oss-cn-beijing.aliyuncs.com`
- **Bucket**: `translation-service`
- **Region**: `cn-beijing`
- **Access Key ID**: `${OSS_ACCESS_KEY_ID}`
- **Access Key Secret**: `${OSS_ACCESS_KEY_SECRET}`

### 小牛翻译
- **App ID**: `Nvq1770003050814`
- **API Key**: `f02d8be29a3abcf775230433b1a09491`

### 额度配置
- **新用户默认额度**: 3
- **分享奖励**: 3/次
- **广告奖励**: 3/次

---

> ⚠️ **安全提醒**: 以上为开发环境配置，生产环境请使用环境变量注入敏感信息
