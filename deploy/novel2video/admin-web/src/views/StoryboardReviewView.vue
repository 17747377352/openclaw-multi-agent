<template>
  <AppShell
    title="分镜审核"
    :subtitle="project ? `${project.title} · 分组管理分镜，编辑提示词并直接发起视频生成。` : '加载项目中...'"
    back-to="/projects"
  >
    <template #actions>
      <button class="btn btn-secondary" @click="reload">刷新</button>
      <button class="btn btn-primary" :disabled="busy || currentGroupId === 0" @click="generateStoryboards">AI 生成分镜</button>
      <button class="btn btn-success" :disabled="busy || selectedIds.length === 0" @click="approveSelected">批量确认</button>
      <button class="btn btn-warning" :disabled="busy || currentGroupId === 0" @click="batchGenerateVideo">批量生成视频</button>
    </template>

    <section class="panel section-gap" v-if="project">
      <div class="selection-bar">
        <div>
          <div class="card-title">{{ project.title }}</div>
          <div class="card-muted" style="margin-top: 8px;">
            当前分组 {{ currentGroupLabel }} · 项目更新时间 {{ formatDateTime(project.updatedAt) }}
          </div>
        </div>
        <div class="page-actions">
          <button
            v-for="group in groups"
            :key="group.id"
            class="btn"
            :class="group.id === currentGroupId ? 'btn-primary' : 'btn-ghost'"
            @click="switchGroup(group.id)"
          >
            第{{ group.groupNumber || group.id }}组
          </button>
        </div>
      </div>
    </section>

    <section class="grid stats section-gap">
      <StatCard label="当前分镜数" :value="storyboards.length" meta="当前分组" />
      <StatCard label="已确认" :value="approvedCount" meta="可直接发视频" />
      <StatCard label="首帧完成" :value="frameReadyCount" meta="已有首帧图" />
      <StatCard label="待处理" :value="pendingCount" meta="仍需审核" />
    </section>

    <section class="filter-bar section-gap">
      <div class="selection-bar">
        <label class="inline-row" style="gap: 10px;">
          <input type="checkbox" :checked="allSelected" @change="toggleAll(($event.target as HTMLInputElement).checked)" />
          <span>全选当前分组</span>
        </label>
        <input v-model="keyword" class="input" style="max-width: 320px;" placeholder="搜索场景标题、描述或提示词" />
      </div>
    </section>

    <section class="section-gap">
      <LoadingBlock v-if="loading" label="分镜列表加载中..." />
      <EmptyState
        v-else-if="filteredStoryboards.length === 0"
        title="当前分组还没有分镜"
        description="可以先点上方的 AI 生成分镜。"
        icon="SCENE"
      >
        <template #actions>
          <button class="btn btn-primary" :disabled="busy || currentGroupId === 0" @click="generateStoryboards">开始生成</button>
        </template>
      </EmptyState>
      <div v-else class="grid storyboards">
        <article v-for="storyboard in filteredStoryboards" :key="storyboard.id" class="storyboard-card list-card">
          <div class="card-header">
            <label class="inline-row" style="gap: 10px; align-items: flex-start;">
              <input type="checkbox" :checked="selectedIds.includes(storyboard.id)" @change="toggleOne(storyboard.id)" />
              <div>
                <div class="card-title">{{ storyboard.title }}</div>
                <div class="card-muted" style="margin-top: 6px;">场景 {{ storyboard.sceneNumber }} · {{ storyboard.location }}</div>
              </div>
            </label>
            <StatusBadge v-bind="reviewStatusMeta(storyboard.status)" />
          </div>

          <div class="card-cover" @click="openPreview(storyboard)">
            <img v-if="storyboard.imageUrl || storyboard.frameImageUrl" :src="storyboard.imageUrl || storyboard.frameImageUrl" :alt="storyboard.title" />
            <span v-else>暂无首帧图</span>
          </div>

          <div class="card-muted" style="line-height: 1.7; min-height: 80px;">
            {{ storyboard.description || '暂无场景描述' }}
          </div>

          <div class="panel" style="padding: 14px;">
            <div class="card-muted" style="margin-bottom: 6px;">提示词</div>
            <div style="line-height: 1.7; font-size: 14px; color: var(--text);">
              {{ storyboard.prompt || '暂无提示词' }}
            </div>
          </div>

          <div class="card-actions">
            <button class="btn btn-secondary" :disabled="busy" @click="openPromptEditor(storyboard)">编辑提示词</button>
            <button class="btn btn-success" :disabled="busy" @click="approveStoryboard(storyboard.id)">确认</button>
            <button class="btn btn-warning" :disabled="busy" @click="generateFrame(storyboard)">首帧图</button>
            <button class="btn btn-danger" :disabled="busy" @click="rejectStoryboard(storyboard.id)">拒绝</button>
          </div>

          <div class="card-actions">
            <button class="btn btn-primary" :disabled="busy" @click="createVideoTask(storyboard.id)">生成视频</button>
            <button class="btn btn-ghost" :disabled="!(storyboard.imageUrl || storyboard.frameImageUrl)" @click="openPreview(storyboard)">预览首帧</button>
          </div>
        </article>
      </div>
    </section>

    <ModalDialog
      :open="promptEditorOpen"
      title="编辑分镜提示词"
      description="修改后会直接保存到后端，后续生成首帧图/视频都会使用新提示词。"
      @close="closePromptEditor"
    >
      <div class="grid" style="gap: 14px;">
        <textarea v-model="promptForm.description" class="textarea" placeholder="分镜描述" />
        <textarea v-model="promptForm.prompt" class="textarea" placeholder="提示词" />
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closePromptEditor">取消</button>
        <button class="btn btn-primary" :disabled="busy || promptTargetId === null" @click="savePrompt">保存</button>
      </template>
    </ModalDialog>

    <ModalDialog
      :open="previewOpen"
      title="首帧预览"
      :description="previewTarget?.title || ''"
      @close="previewOpen = false"
    >
      <div class="video-player">
        <img v-if="previewImageUrl" :src="previewImageUrl" :alt="previewTarget?.title || '首帧图'" />
      </div>
    </ModalDialog>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppShell from '@/components/layout/AppShell.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import LoadingBlock from '@/components/ui/LoadingBlock.vue';
import ModalDialog from '@/components/ui/ModalDialog.vue';
import StatCard from '@/components/ui/StatCard.vue';
import StatusBadge from '@/components/ui/StatusBadge.vue';
import { novelApi } from '@/api/novel';
import { storyboardApi } from '@/api/storyboard';
import { videoApi } from '@/api/video';
import { pushToast } from '@/stores/toast';
import type { ChapterGroup, Project, Storyboard } from '@/types/domain';
import { formatDateTime, parseIdList } from '@/utils/format';
import { reviewStatusMeta } from '@/utils/status';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.query.id || 0);
const loading = ref(false);
const busy = ref(false);
const project = ref<Project | null>(null);
const groups = ref<ChapterGroup[]>([]);
const currentGroupId = ref(0);
const storyboards = ref<Storyboard[]>([]);
const selectedIds = ref<number[]>([]);
const keyword = ref('');
const promptEditorOpen = ref(false);
const promptTargetId = ref<number | null>(null);
const previewOpen = ref(false);
const previewTarget = ref<Storyboard | null>(null);
const promptForm = reactive({
  description: '',
  prompt: ''
});

const currentGroupLabel = computed(() => {
  const group = groups.value.find((item) => item.id === currentGroupId.value);
  return group ? `第${group.groupNumber || group.id}组` : '未选择分组';
});

const filteredStoryboards = computed(() => {
  const q = keyword.value.trim().toLowerCase();
  if (!q) return storyboards.value;
  return storyboards.value.filter((item) =>
    [item.title, item.description, item.prompt].some((field) => (field || '').toLowerCase().includes(q))
  );
});

const approvedCount = computed(() => storyboards.value.filter((item) => item.status === 'approved').length);
const frameReadyCount = computed(() => storyboards.value.filter((item) => item.imageUrl || item.frameImageUrl).length);
const pendingCount = computed(() => storyboards.value.filter((item) => item.status === 'pending' || item.status === 'rejected').length);
const allSelected = computed(() => filteredStoryboards.value.length > 0 && filteredStoryboards.value.every((item) => selectedIds.value.includes(item.id)));
const previewImageUrl = computed(() => previewTarget.value?.imageUrl || previewTarget.value?.frameImageUrl || '');

async function loadProject() {
  project.value = await novelApi.getProject(projectId);
}

async function loadGroups() {
  groups.value = await novelApi.getGroups(projectId);
  if (!currentGroupId.value && groups.value.length > 0) {
    currentGroupId.value = groups.value[0].id;
  }
}

async function loadStoryboards() {
  if (!currentGroupId.value) {
    storyboards.value = [];
    return;
  }
  storyboards.value = await storyboardApi.list(currentGroupId.value);
}

async function reload() {
  if (!projectId) {
    pushToast('缺少项目 ID', 'error');
    return;
  }

  loading.value = true;
  try {
    await Promise.all([loadProject(), loadGroups()]);
    await loadStoryboards();
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '加载分镜失败', 'error');
  } finally {
    loading.value = false;
  }
}

async function switchGroup(groupId: number) {
  currentGroupId.value = groupId;
  selectedIds.value = [];
  await loadStoryboards();
}

function toggleOne(id: number) {
  selectedIds.value = selectedIds.value.includes(id)
    ? selectedIds.value.filter((item) => item !== id)
    : [...selectedIds.value, id];
}

function handleToggleAll(event: Event) {
  const target = event.target as HTMLInputElement;
  toggleAll(target.checked);
}

function toggleAll(checked: boolean) {
  selectedIds.value = checked ? filteredStoryboards.value.map((item) => item.id) : [];
}

async function generateStoryboards() {
  if (!currentGroupId.value) {
    pushToast('请先选择分组', 'warning');
    return;
  }

  busy.value = true;
  try {
    storyboards.value = await storyboardApi.generate(currentGroupId.value);
    selectedIds.value = [];
    pushToast('分镜生成完成', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '分镜生成失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function approveStoryboard(id: number) {
  busy.value = true;
  try {
    await storyboardApi.confirm([id]);
    await loadStoryboards();
    pushToast('分镜已确认', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '确认失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function approveSelected() {
  if (selectedIds.value.length === 0) {
    pushToast('请先选择分镜', 'warning');
    return;
  }

  busy.value = true;
  try {
    await storyboardApi.confirm(selectedIds.value);
    selectedIds.value = [];
    await loadStoryboards();
    pushToast('批量确认完成', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量确认失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function rejectStoryboard(id: number) {
  busy.value = true;
  try {
    await storyboardApi.update(id, { frameStatus: 3, isConfirmed: 0 });
    await loadStoryboards();
    pushToast('分镜已标记为拒绝', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '拒绝失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function generateFrame(storyboard: Storyboard) {
  busy.value = true;
  try {
    await storyboardApi.generateFrame(storyboard.id, parseIdList(storyboard.characterIds));
    await loadStoryboards();
    pushToast('首帧图生成请求已提交', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '首帧图生成失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function createVideoTask(storyboardId: number) {
  busy.value = true;
  try {
    await videoApi.create(storyboardId);
    pushToast('视频任务已创建', 'success');
    router.push({ name: 'videos', query: { id: String(projectId) } });
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '创建视频任务失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function batchGenerateVideo() {
  if (!currentGroupId.value) {
    pushToast('请先选择分组', 'warning');
    return;
  }

  busy.value = true;
  try {
    const result = await videoApi.batch(currentGroupId.value);
    pushToast(`已提交 ${result.submitted} 个视频任务，跳过 ${result.skipped} 个`, 'success');
    router.push({ name: 'videos', query: { id: String(projectId) } });
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量创建失败', 'error');
  } finally {
    busy.value = false;
  }
}

function openPromptEditor(storyboard: Storyboard) {
  promptTargetId.value = storyboard.id;
  promptForm.description = storyboard.description || '';
  promptForm.prompt = storyboard.prompt || '';
  promptEditorOpen.value = true;
}

function closePromptEditor() {
  promptEditorOpen.value = false;
  promptTargetId.value = null;
  promptForm.description = '';
  promptForm.prompt = '';
}

async function savePrompt() {
  if (promptTargetId.value === null) return;
  busy.value = true;
  try {
    await storyboardApi.update(promptTargetId.value, {
      description: promptForm.description,
      prompt: promptForm.prompt
    });
    await loadStoryboards();
    closePromptEditor();
    pushToast('提示词已保存', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '保存失败', 'error');
  } finally {
    busy.value = false;
  }
}

function openPreview(storyboard: Storyboard) {
  if (!(storyboard.imageUrl || storyboard.frameImageUrl)) {
    pushToast('当前分镜还没有首帧图', 'warning');
    return;
  }
  previewTarget.value = storyboard;
  previewOpen.value = true;
}

onMounted(reload);
</script>
