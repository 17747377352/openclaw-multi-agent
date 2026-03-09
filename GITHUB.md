# GitHub 仓库配置

## 仓库信息

- **URL**: https://github.com/17747377352/openclaw-multi-agent
- **SSH**: git@github.com:17747377352/openclaw-multi-agent.git
- **分支**: main

## Git 配置

```bash
# 用户信息
git config --global user.name "用户 576033"
git config --global user.email "761164244@qq.com"

# 凭证存储
git config --global credential.helper store
```

## 常用命令

```bash
# 提交更改
git add -A
git commit -m "描述"
git push

# 拉取更新
git pull

# 查看状态
git status
git log --oneline
```

## Token 管理

⚠️ **Token 已保存在本地 Git 凭证中**，不要提交到仓库。

如需刷新 token：
```bash
rm ~/.git-credentials
git config --global --unset credential.helper
git config --global credential.helper store
```
