/**
 * 论坛讨论模式启动器
 * Main Agent 使用此模块发起多 Agent 讨论
 */

const CONFIG = require('./config.json');

/**
 * 发起论坛讨论
 * @param {string} title - 讨论标题
 * @param {string} requirement - 用户需求描述
 */
async function startForumDiscussion(title, requirement) {
  const timestamp = new Date().toLocaleString('zh-CN', { 
    timeZone: 'Asia/Shanghai',
    hour12: false
  });
  
  // 1. 创建讨论帖
  const topic = `
### [讨论中] ${title}

**发起**: @main  
**时间**: ${timestamp}  
**状态**: 🟡 讨论中

#### 原始需求
> ${requirement}

---

#### @developer 的意见
**时间**: ${timestamp}  
**状态**: ⏳ 待发表

技术实现思路：
- ...

---

#### @designer 的意见
**时间**: ${timestamp}  
**状态**: ⏳ 待发表

产品设计思路：
- ...

---

#### @ops 的意见
**时间**: ${timestamp}  
**状态**: ⏳ 待发表

部署运维思路：
- ...

---

#### 📋 最终方案 (由 @main 汇总)
**时间**: ${timestamp}  
**状态**: ⏳ 待形成
`;

  // 2. 写入论坛文件
  console.log('📝 创建讨论帖...');
  // 实际调用：edit('shared/forum.md', prepend(topic));
  
  // 3. 通知各 Agent
  console.log('📢 通知各 Agent 参与讨论...');
  const agents = ['developer', 'designer', 'ops'];
  
  for (const agent of agents) {
    await sessions_send({
      label: agent,
      message: `🆕 新需求讨论已发布\n\n标题：${title}\n\n请查看 shared/forum.md 并发表你的专业意见`
    });
    console.log(`   ✓ 已通知 @${agent}`);
  }
  
  console.log('');
  console.log('✅ 讨论已发起，等待各 Agent 发表意见...');
  console.log('');
  console.log('💡 Main Agent 需要：');
  console.log('   1. 等待各 Agent 更新论坛 (轮询或等待通知)');
  console.log('   2. 收集所有意见后，汇总最终方案');
  console.log('   3. 将方案发送给用户确认');
  
  return { title, timestamp, topic };
}

/**
 * 收集各 Agent 意见
 */
async function collectOpinions() {
  console.log('📋 收集各 Agent 意见...');
  
  // 读取论坛文件
  const forum = read('shared/forum.md');
  
  // 提取各 Agent 意见
  const opinions = {
    developer: extractSection(forum, '@developer'),
    designer: extractSection(forum, '@designer'),
    ops: extractSection(forum, '@ops')
  };
  
  // 检查是否全部完成
  const allReady = Object.values(opinions).every(op => op.status === '✅ 已发表');
  
  return { opinions, allReady };
}

/**
 * 形成最终方案
 */
async function finalizePlan(opinions) {
  console.log('📋 形成最终方案...');
  
  const plan = `
#### 📋 最终方案

**综合意见**:
- 技术：${opinions.developer.summary}
- 产品：${opinions.designer.summary}
- 运维：${opinions.ops.summary}

**执行步骤**:
1. ...
2. ...

**预计时间**: ...
`;
  
  // 更新论坛
  // edit('shared/forum.md', replaceSection(plan));
  
  return plan;
}

module.exports = { startForumDiscussion, collectOpinions, finalizePlan };
