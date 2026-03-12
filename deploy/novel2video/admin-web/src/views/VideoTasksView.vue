<template>
  <AppShell
    title="视频任务"
    :subtitle="project ? `${project.title} · 查看任务状态、回放成片、重试失败任务。` : '加载项目中...'"
    back-to="/projects"
  >
    <template #actions>
      <button class="btn btn-secondary" @click="reload">刷新</button>
      <button class="btn btn-primary" :disabled="busy || groups.length === 0" @click="startBatchGenerate">批量生成</button>
      <button class="btn btn-danger" :disabled="busy || failedTasks.length === 0" @click="retryAllFailed">重试失败任务</button>
    </template>

    <section class="panel section-gap" v-if="project">
      <div class="selection-bar">
        <div>
          <div class="card-title">{{ project.title }}</div>
          <div class="card-muted" style="margin-top: 8px;">
            共 {{ groups.length }} 个分组 · 自动刷新 {{ autoRefresh ? '已开启' : '已关闭' }}
          </div>
        </div>
        <button class="btn" :class="autoRefresh ? 'btn-primary' : 'btn-ghost'" @click="toggleAutoRefresh">
          {{ autoRefresh ? '关闭自动刷新' : '开启自动刷新' }}
        </button>
      </div>
    </section>

    <section class="grid stats section-gap">
      <StatCard label="任务总数" :value="tasks.length" meta="当前项目" />
      <StatCard label="已完成" :value="completedCount" meta="可直接播放下载" />
      <StatCard label="进行中" :value="processingCount" meta="自动轮询更新" />
      <StatCard label="失败" :value="failedCount" meta="支持单个和批量重试" />
    </section>

    <section class="filter-bar section-gap">
      <div class="filters-row">
        <select v-model="statusFilter" class="select">
          <option value="">全部状态</option>
          <option value="pending">等待中</option>
          <option value="processing">生成中</option>
          <option value="completed">已完成</option>
          <option value="failed">失败</option>
        </select>
        <select v-model="groupFilter" class="select">
          <option value="">全部分组</option>
          <option v-for="group in groups" :key="group.id" :value="String(group.id)">第{{ group.groupNumber || group.id }}组</option>
        </select>
        <input v-model="keyword" class="input" placeholder="搜索任务 ID / 场景名" />
      </div>
    </section>

    <section class="section-gap">
      <LoadingBlock v-if="loading" label="视频任务加载中..." />
      <EmptyState
        v-else-if="filteredTasks.length === 0"
        title="还没有视频任务"
        description="从分镜页发起视频生成，或者直接在这里批量创建。"
        icon="VIDEO"
      >
        <template #actions>
          <button class="btn btn-primary" :disabled="busy || groups.length === 0" @click="startBatchGenerate">立即批量生成</button>
        </template>
      </EmptyState>
      <div v-else class="grid tasks">
        <article v-for="task in filteredTasks" :key="task.id" class="task-card list-card">
          <div class="card-header">
            <div>
              <div class="card-title">{{ task.title }}</div>
              <div class="card-muted" style="margin-top: 6px;">{{ task.taskId }}</div>
            </div>
            <StatusBadge v-bind="videoStatusMeta(task.status)" />
          </div>

          <div class="card-cover">
            <video v-if="task.status === 'completed' && task.videoUrl" :src="task.videoUrl" muted preload="metadata" />
            <span v-else>{{ task.status === 'failed' ? '任务失败' : task.status === 'processing' ? '生成中...' : '暂无视频' }}</span>
          </div>

          <div class="metrics">
            <div class="metric">
              <div class="metric-label">分组</div>
              <div class="metric-value">第{{ task.groupId }}组</div>
            </div>
            <div class="metric">
              <div class="metric-label">时长</div>
              <div class="metric-value">{{ task.duration }}</div>
            </div>
            <div class="metric">
              <div class="metric-label">进度</div>
              <div class="metric-value">{{ task.progress }}%</div>
            </div>
          </div>

          <div>
            <div class="progress-track">
              <div class="progress-bar" :style="{ width: `${task.progress}%` }" />
            </div>
            <div class="card-muted" style="margin-top: 8px;">
              {{ task.failReason || formatDateTime(task.updatedAt || task.createdAt) }}
            </div>
          </div>

          <div class="card-actions">
            <button class="btn btn-secondary" @click="viewTaskDetail(task)">详情</button>
            <button class="btn btn-primary" :disabled="!task.videoUrl" @click="openPlayer(task)">播放</button>
            <button class="btn btn-success" :disabled="!task.videoUrl" @click="downloadVideo(task)">下载</button>
            <button class="btn btn-danger" :disabled="task.status !== 'failed' || busy" @click="retryTask(task)">重试</button>
          </div>
        </article>
      </div>
    </section>

    <ModalDialog
      :open="detailOpen"
      title="任务详情"
      :description="detailTask ? detailTask.taskId : ''"
      @close="detailOpen = false"
    >
      <div v-if="detailTask" class="grid" style="gap: 14px;">
        <div class="metrics">
          <div class="metric">
            <div class="metric-label">状态</div>
            <div class="metric-value">{{ videoStatusMeta(detailTask.status).label }}</div>
          </div>
          <div class="metric">
            <div class="metric-label">模型</div>
            <div class="metric-value">{{ detailTask.model }}</div>
          </div>
          <div class="metric">
            <div class="metric-label">更新时间</div>
            <div class="metric-value">{{ formatDateTime(detailTask.updatedAt) }}</div>
          </div>
        </div>
        <div class="panel" style="padding: 14px;">
          <div class="card-muted" style="margin-bottom: 8px;">失败原因 / 附加信息</div>
          <div>{{ detailTask.failReason || '当前任务无失败信息。' }}</div>
        </div>
      </div>
    </ModalDialog>

    <ModalDialog
      :open="playerOpen"
      title="视频预览"
      :description="playerTask ? playerTask.title : ''"
      @close="closePlayer"
    >
      <div class="video-player">
        <video v-if="playerTask?.videoUrl" ref="playerRef" :src="playerTask.videoUrl" controls playsinline preload="metadata" />
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closePlayer">关闭</button>
        <button class="btn btn-primary" :disabled="!playerTask?.videoUrl" @click="playerTask && downloadVideo(playerTask)">下载视频</button>
      </template>
    </ModalDialog>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import AppShell from '@/components/layout/AppShell.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import LoadingBlock from '@/components/ui/LoadingBlock.vue';
import ModalDialog from '@/components/ui/ModalDialog.vue';
import StatCard from '@/components/ui/StatCard.vue';
import StatusBadge from '@/components/ui/StatusBadge.vue';
import { novelApi } from '@/api/novel';
import { videoApi } from '@/api/video';
import { pushToast } from '@/stores/toast';
import type { ChapterGroup, Project, VideoTask } from '@/types/domain';
import { formatDateTime } from '@/utils/format';
import { videoStatusMeta } from '@/utils/status';

const route = useRoute();
const projectId = Number(route.query.id || 0);
const loading = ref(false);
const busy = ref(false);
const project = ref<Project | null>(null);
const groups = ref<ChapterGroup[]>([]);
const tasks = ref<VideoTask[]>([]);
const statusFilter = ref('');
const groupFilter = ref('');
const keyword = ref('');
const autoRefresh = ref(true);
const detailOpen = ref(false);
const playerOpen = ref(false);
const detailTask = ref<VideoTask | null>(null);
const playerTask = ref<VideoTask | null>(null);
const playerRef = ref<HTMLVideoElement | null>(null);
let refreshTimer: number | null = null;

const filteredTasks = computed(() => {
  const q = keyword.value.trim().toLowerCase();
  return tasks.value.filter((item) => {
    if (statusFilter.value && item.status !== statusFilter.value) return false;
    if (groupFilter.value && String(item.groupId) !== groupFilter.value) return false;
    if (!q) return true;
    return [item.taskId, item.title].some((field) => (field || '').toLowerCase().includes(q));
  });
});

const completedCount = computed(() => tasks.value.filter((item) => item.status === 'completed').length);
const processingCount = computed(() => tasks.value.filter((item) => item.status === 'processing').length);
const failedCount = computed(() => tasks.value.filter((item) => item.status === 'failed').length);
const failedTasks = computed(() => tasks.value.filter((item) => item.status === 'failed'));

async function loadProject() {
  project.value = await novelApi.getProject(projectId);
}

async function loadGroups() {
  groups.value = await novelApi.getGroups(projectId);
}

async function loadTasks() {
  tasks.value = await videoApi.list(1, projectId);
}

async function reload() {
  if (!projectId) {
    pushToast('缺少项目 ID', 'error');
    return;
  }

  loading.value = true;
  try {
    await Promise.all([loadProject(), loadGroups(), loadTasks()]);
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '加载视频任务失败', 'error');
  } finally {
    loading.value = false;
  }
}

async function startBatchGenerate() {
  if (groups.value.length === 0) {
    pushToast('当前项目没有分组', 'warning');
    return;
  }

  busy.value = true;
  try {
    const targetGroups = groupFilter.value
      ? groups.value.filter((item) => String(item.id) === groupFilter.value)
      : groups.value;

    let submitted = 0;
    let skipped = 0;
    for (const group of targetGroups) {
      const result = await videoApi.batch(group.id);
      submitted += result.submitted;
      skipped += result.skipped;
    }

    await loadTasks();
    pushToast(`已提交 ${submitted} 个视频任务，跳过 ${skipped} 个`, 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量生成失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function retryTask(task: VideoTask) {
  busy.value = true;
  try {
    await videoApi.retry(task.id);
    await loadTasks();
    pushToast(`任务 ${task.taskId} 已重新提交`, 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '重试失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function retryAllFailed() {
  const retryCount = failedTasks.value.length;
  if (retryCount === 0) {
    pushToast('没有失败任务可重试', 'warning');
    return;
  }

  busy.value = true;
  try {
    for (const task of failedTasks.value) {
      await videoApi.retry(task.id);
    }
    await loadTasks();
    pushToast(`已重试 ${retryCount} 个任务`, 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量重试失败', 'error');
  } finally {
    busy.value = false;
  }
}

function viewTaskDetail(task: VideoTask) {
  detailTask.value = task;
  detailOpen.value = true;
}

function openPlayer(task: VideoTask) {
  if (!task.videoUrl) {
    pushToast('视频链接不存在或已过期', 'warning');
    return;
  }
  playerTask.value = task;
  playerOpen.value = true;
  window.setTimeout(() => playerRef.value?.play().catch(() => {}), 30);
}

function closePlayer() {
  playerRef.value?.pause();
  playerTask.value = null;
  playerOpen.value = false;
}

function downloadVideo(task: VideoTask) {
  if (!task.videoUrl) {
    pushToast('视频链接不存在或已过期', 'warning');
    return;
  }
  window.open(task.videoUrl, '_blank', 'noopener');
}

function toggleAutoRefresh() {
  autoRefresh.value = !autoRefresh.value;
  if (autoRefresh.value) {
    startRefreshTimer();
  } else {
    stopRefreshTimer();
  }
}

function startRefreshTimer() {
  stopRefreshTimer();
  refreshTimer = window.setInterval(() => {
    if (!autoRefresh.value || busy.value) return;
    loadTasks().catch(() => undefined);
  }, 10000);
}

function stopRefreshTimer() {
  if (refreshTimer !== null) {
    window.clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

onMounted(async () => {
  await reload();
  startRefreshTimer();
});

onBeforeUnmount(stopRefreshTimer);
</script>
