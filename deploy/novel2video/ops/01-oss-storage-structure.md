# 小说转短视频项目 - OSS 存储目录结构规划

## 📁 Bucket 信息

- **Bucket 名称**: `translation-service`
- **Region**: `cn-beijing`
- **Endpoint**: `https://oss-cn-beijing.aliyuncs.com`

---

## 🗂️ 目录结构

```
novel2video/
├── projects/                    # 项目原始文件
│   └── {project_id}/
│       ├── novel/               # 上传的小说原文
│       │   └── {novel_name}.txt
│       └── metadata.json        # 项目元信息
│
├── characters/                  # 人物标准照
│   └── {project_id}/
│       ├── {character_id}_seed.json    # 人物特征种子
│       ├── {character_id}.jpg          # 人物标准照
│       └── {character_id}_variants/    # 备选方案（可选）
│           ├── v1.jpg
│           └── v2.jpg
│
├── storyboards/                 # 分镜首帧图
│   └── {group_id}/
│       ├── {scene_id}_prompt.txt       # 分镜提示词
│       ├── {scene_id}_frame.jpg        # 首帧图
│       └── {scene_id}_meta.json        # 分镜元数据
│
├── videos/                      # 成品视频
│   └── {group_id}/
│       ├── {scene_id}.mp4              # 视频文件
│       ├── {scene_id}_thumbnail.jpg    # 视频封面
│       └── {scene_id}_meta.json        # 视频元数据
│
├── temp/                        # 临时文件（定期清理）
│   └── {task_id}/
│       └── ...
│
└── exports/                     # 导出打包
    └── {project_id}/
        └── {project_id}_full.zip       # 完整项目导出
```

---

## 📋 命名规范

| 类型 | 格式 | 示例 |
|------|------|------|
| Project ID | `proj_{uuid8}` | `proj_a1b2c3d4` |
| Character ID | `char_{uuid8}` | `char_e5f6g7h8` |
| Group ID | `grp_{uuid8}` | `grp_i9j0k1l2` |
| Scene ID | `scn_{uuid8}` | `scn_m3n4o5p6` |
| Task ID | `tsk_{uuid8}` | `tsk_q7r8s9t0` |

---

## 🔒 访问控制

### Bucket Policy 建议

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": ["oss:GetObject"],
      "Resource": "acs:oss:*:*:translation-service/novel2video/videos/*",
      "Condition": {
        "IpAddress": {
          "acs:SourceIp": ["YOUR_SERVER_IP/32"]
        }
      }
    }
  ]
}
```

### CDN 配置（可选）

- **域名**: `video.songtop.xyz`
- **源站**: OSS Bucket
- **缓存策略**:
  - `/videos/*.mp4`: 缓存 30 天
  - `/characters/*.jpg`: 缓存 7 天
  - `/storyboards/*.jpg`: 缓存 1 天

---

## 🧹 生命周期管理

| 目录 | 规则 | 说明 |
|------|------|------|
| `temp/` | 7 天后删除 | 临时文件自动清理 |
| `exports/` | 30 天后转 IA | 导出文件转低频存储 |
| `storyboards/` | 永久保留 | 分镜图需长期保存 |
| `videos/` | 永久保留 | 成品视频需长期保存 |

---

## 📊 存储预估

以单本 100 章小说为例：

| 类型 | 单文件大小 | 数量 | 总计 |
|------|-----------|------|------|
| 人物照 | 500 KB | 20 人 | 10 MB |
| 分镜首帧图 | 800 KB | 100 场景 | 80 MB |
| 视频文件 | 15 MB | 100 场景 | 1.5 GB |
| **合计** | - | - | **~1.6 GB / 本小说** |

---

## 💡 最佳实践

1. **上传时使用分片上传** - 视频文件 >100MB 必须分片
2. **启用传输加速** - 大文件上传体验更好
3. **设置回调通知** - 上传完成触发业务逻辑
4. **图片使用 WebP 格式** - 节省 30% 存储（首帧图可考虑）
5. **视频使用 HLS 切片** - 如需在线播放优化
