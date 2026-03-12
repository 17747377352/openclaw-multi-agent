# 分镜提示词规范

目标：让分镜生成从“自由发挥”变成“结构化生成”，优先保证人物一致性和风格统一性。

适用形式：2D 动态漫画 / 2.5D 镜头视频。

---

## 1. 总原则

- 先有角色卡，再写分镜提示词
- 先有风格圣经，再写分镜提示词
- 先做首帧图，再做视频
- 每条提示词都必须分层描述，不要一句话乱写

禁止写法：

- “一个很帅的人在很美的场景里战斗，非常震撼”
- 这种描述没有任何生产价值

---

## 2. 分镜提示词标准结构

每条提示词建议固定按下面顺序写：

1. `项目风格锚点`
2. `角色锚点`
3. `镜头类型`
4. `构图`
5. `场景`
6. `动作`
7. `情绪`
8. `光线`
9. `特效`
10. `一致性约束`
11. `负向约束`

---

## 3. 项目风格锚点模板

每条分镜都要先带上这一段：

`2d anime, chinese fantasy dynamic comic, clean lineart, cel shading, consistent character design, consistent costume design, cinematic composition, not photorealistic`

如果项目不是国风，就替换成该项目的固定风格词，但“consistent character design / not photorealistic”建议保留。

---

## 4. 角色锚点模板

### 单人镜头

`[角色名], same face shape, same hairstyle, same costume, same signature accessory`

### 双人镜头

`[角色A] and [角色B], both keep original character design, correct height difference, correct costume design, no extra characters`

### 群像镜头

- 群像镜头只放 1-2 个主角色细节
- 其他人做弱化，不要所有人都要求高清特写

---

## 5. 镜头类型标准词

### 建议镜头枚举

- 远景：`wide shot`
- 中景：`medium shot`
- 近景：`close shot`
- 特写：`close-up`
- 仰角：`low angle`
- 俯角：`high angle`
- 过肩：`over shoulder shot`
- 背影：`back view`

规则：

- 每条分镜只选 1 个主镜头类型
- 不要一条提示词里混多个冲突镜头

---

## 6. 构图模板

从这些里选一个主导构图：

- 角色居中
- 三分法偏左
- 三分法偏右
- 前景遮挡构图
- 对角线构图
- 对称构图

示例写法：

`medium shot, rule of thirds composition, character placed on the left side, leaving space for subtitle on the right`

注意：

- 短视频要给字幕留空间
- 不要把人物和字幕都挤在底部中央

---

## 7. 场景模板

场景必须写到可复用层级：

- 时间：清晨 / 正午 / 黄昏 / 深夜
- 天气：晴 / 雨 / 雪 / 雾
- 场地：宗门广场 / 山林 / 房间 / 街道 / 悬崖
- 背景元素：石阶 / 灯笼 / 松树 / 云海 / 屋檐 / 火光

标准写法：

`ancient chinese training ground at dusk, stone floor, red pillars, mountain silhouette in the distance, drifting dust in the air`

错误写法：

- “在一个地方”
- “背景很宏大”

---

## 8. 动作模板

动作只写一个主要动作，不要一条里塞三件事。

### 静态戏

- 站立
- 回头
- 抬眼
- 握拳
- 伸手
- 对视
- 低头沉思

### 动作戏

- 挥剑前摇
- 躲闪
- 落地
- 释放法术
- 能量爆发瞬间

建议：

- 视频生成优先做“动作瞬间”而不是“完整连续动作”
- 比如写“挥剑前一刻”比写“连续旋身三连斩”更稳定

---

## 9. 情绪模板

情绪必须视觉化：

- 冷静：steady gaze, calm expression
- 愤怒：tight jaw, lowered brows
- 悲伤：wet eyes, lowered head
- 惊讶：wide eyes, slightly open mouth
- 杀意：cold stare, tense posture

不要只写：

- “非常生气”
- “非常伤心”

要改成：

- `lowered brows, clenched fist, tense shoulders`

---

## 10. 光线模板

每条分镜都必须指定光线来源：

- 侧光
- 顶光
- 背光
- 火光
- 月光
- 室内暖光

示例：

`warm side light from sunset, long shadow on the ground`

或者：

`cold moonlight from upper right, strong edge light on the hair`

作用：

- 不写光，画面就乱
- 写了固定光源，镜头之间统一度会高很多

---

## 11. 特效模板

特效只能作为辅助，不要喧宾夺主。

- 火焰粒子
- 灵气流动
- 落叶
- 尘土
- 光点
- 剑气轨迹

示例：

`subtle flame particles around his right hand, not covering the face`

限制：

- 特效不能挡脸
- 特效颜色必须符合风格圣经

---

## 12. 一致性约束模板

每条提示词末尾建议附上：

`keep original character design, keep same hairstyle, keep same outfit details, keep same facial proportions, no random accessories, no style drift`

如果是场景母版镜头：

`keep the same environment design as previous scene`

---

## 13. 负向约束模板

统一负向模板：

`photorealistic, realistic skin texture, 3d render, style drift, extra fingers, bad anatomy, blurry face, low detail eyes, wrong costume, wrong hairstyle, duplicate person, cropped head, text watermark`

---

## 14. 可直接执行的提示词骨架

### 文生首帧图骨架

`2d anime, chinese fantasy dynamic comic, clean lineart, cel shading, consistent character design, [角色锚点], [镜头类型], [构图], [场景], [动作], [情绪], [光线], [特效], keep original character design, keep same outfit details, no style drift -- negative: photorealistic, 3d render, bad anatomy, blurry face, wrong costume, wrong hairstyle`

### 图生视频骨架

`2d anime dynamic comic shot, [角色锚点], [镜头类型], [场景], [动作瞬间], [情绪], [光线], [特效], subtle motion, slight camera push in, keep original character design, keep same outfit details, no style drift --duration 5 --camerafixed false --watermark true`

注意：

- 视频阶段不要再大改设定
- 视频提示词应比首帧图更短，更聚焦镜头动作

---

## 15. 提示词实例

### 实例 1：主角压抑情绪的近景

`2d anime, chinese fantasy dynamic comic, clean lineart, cel shading, consistent character design, Xiao Yan, same face shape, same black tied hair, same dark martial outfit, close shot, character centered, ancient courtyard at dusk, standing still with clenched fist, lowered brows, restrained anger, warm side light from sunset, subtle dust in the air, keep original character design, keep same outfit details, no style drift -- negative: photorealistic, 3d render, blurry face, wrong hairstyle, wrong costume`

### 实例 2：女主出场的中景

`2d anime, chinese fantasy dynamic comic, clean lineart, cel shading, consistent character design, female lead, same long silver hair, same pale blue robe, medium shot, rule of thirds composition, mountain path with drifting mist, slow walking forward, calm and distant expression, cold morning light, soft mist particles, keep original character design, keep same outfit details, no style drift -- negative: photorealistic, realistic skin, 3d render, bad anatomy, wrong costume`

---

## 16. 审核标准

一条提示词写完后，检查：

- 是否写了项目风格锚点
- 是否写了角色一致性锚点
- 是否明确镜头类型
- 是否明确场景
- 是否只写一个主动作
- 是否写了光线
- 是否有统一负向约束
- 是否留出了字幕空间

一句话原则：

- 提示词不是文学描写，是“镜头生产指令”。
