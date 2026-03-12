<template>
  <AppShell title="小说转视频项目台" subtitle="Vue 3 重构版后台，统一了项目、人物、分镜、视频四个工作台。">
    <template #actions>
      <button class="btn btn-secondary" @click="loadProjects">刷新</button>
      <button class="btn btn-primary" @click="createModalOpen = true">新建项目</button>
    </template>

    <section class="grid stats section-gap">
      <StatCard label="项目总数" :value="projects.length" meta="当前筛选结果" />
      <StatCard label="进行中" :value="inProgressCount" meta="人物/分镜/视频处理中" />
      <StatCard label="已完成" :value="completedCount" meta="已到成片阶段" />
      <StatCard label="总字数" :value="totalWordsLabel" meta="按当前列表聚合" />
    </section>

    <section class="filter-bar section-gap">
      <div class="filters-row">
        <select v-model="filters.status" class="select">
          <option value="">全部状态</option>
          <option value="0">待开始</option>
          <option value="1">进行中</option>
          <option value="2">分镜处理中</option>
          <option value="3">已完成</option>
        </select>
        <select v-model="filters.sort" class="select">
          <option value="created_desc">最新创建</option>
          <option value="created_asc">最早创建</option>
          <option value="updated_desc">最近更新</option>
          <option value="title_asc">标题 A-Z</option>
        </select>
        <input v-model="filters.keyword" class="input" placeholder="按项目名或作者搜索" @keyup.enter="loadProjects" />
        <button class="btn btn-primary" @click="loadProjects">搜索</button>
      </div>
    </section>

    <section class="section-gap">
      <LoadingBlock v-if="loading" label="项目列表加载中..." />
      <EmptyState
        v-else-if="projects.length === 0"
        title="还没有项目"
        description="先导入一部小说，后续人物、分镜和视频都从这里接着走。"
        icon="BOOK"
      >
        <template #actions>
          <button class="btn btn-primary" @click="createModalOpen = true">创建第一个项目</button>
        </template>
      </EmptyState>
      <div v-else class="grid projects">
        <article v-for="project in projects" :key="project.id" class="project-card list-card">
          <div class="card-header">
            <div>
              <div class="card-title">{{ project.title }}</div>
              <div class="card-muted" style="margin-top: 8px;">
                作者 {{ project.author || '未知' }} · 创建于 {{ formatDateTime(project.createdAt) }}
              </div>
            </div>
            <StatusBadge v-bind="projectStatusMeta(project.status)" />
          </div>

          <div class="metrics">
            <div class="metric">
              <div class="metric-label">章节</div>
              <div class="metric-value">{{ project.chapterCount || 0 }}</div>
            </div>
            <div class="metric">
              <div class="metric-label">字数</div>
              <div class="metric-value">{{ formatWordCount(project.totalWords) }}</div>
            </div>
            <div class="metric">
              <div class="metric-label">进度</div>
              <div class="metric-value">{{ project.progress || 0 }}%</div>
            </div>
          </div>

          <div>
            <div class="card-muted" style="margin-bottom: 8px;">处理进度</div>
            <div class="progress-track">
              <div class="progress-bar" :style="{ width: `${project.progress || 0}%` }" />
            </div>
          </div>

          <div class="card-actions">
            <button class="btn btn-secondary" @click="goTo('characters', project.id)">人物</button>
            <button class="btn btn-secondary" @click="goTo('storyboards', project.id)">分镜</button>
            <button class="btn btn-secondary" @click="goTo('videos', project.id)">视频</button>
          </div>
        </article>
      </div>
    </section>

    <ModalDialog
      :open="createModalOpen"
      title="导入小说项目"
      description="支持直接粘贴正文，也支持上传本地 txt 文件。"
      @close="closeCreateModal"
    >
      <div class="grid" style="gap: 14px;">
        <div class="filters-row">
          <input v-model="form.title" class="input" placeholder="项目标题" />
          <input v-model="form.author" class="input" placeholder="作者（可选）" />
        </div>
        <div class="filters-row">
          <button class="btn" :class="uploadMode === 'text' ? 'btn-primary' : 'btn-ghost'" @click="uploadMode = 'text'">文本粘贴</button>
          <button class="btn" :class="uploadMode === 'file' ? 'btn-primary' : 'btn-ghost'" @click="uploadMode = 'file'">文件上传</button>
        </div>
        <textarea
          v-if="uploadMode === 'text'"
          v-model="form.content"
          class="textarea"
          placeholder="把小说正文粘贴到这里"
        />
        <div v-else class="panel" style="padding: 16px;">
          <input type="file" accept=".txt,.md" @change="onFileChange" />
          <div class="card-muted" style="margin-top: 10px;">
            {{ selectedFile ? `已选择：${selectedFile.name}` : '请选择 UTF-8 编码文本文件' }}
          </div>
        </div>
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closeCreateModal">取消</button>
        <button class="btn btn-primary" :disabled="submitting" @click="submitProject">
          {{ submitting ? '提交中...' : '创建项目' }}
        </button>
      </template>
    </ModalDialog>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import AppShell from '@/components/layout/AppShell.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import LoadingBlock from '@/components/ui/LoadingBlock.vue';
import ModalDialog from '@/components/ui/ModalDialog.vue';
import StatCard from '@/components/ui/StatCard.vue';
import StatusBadge from '@/components/ui/StatusBadge.vue';
import { novelApi } from '@/api/novel';
import { pushToast } from '@/stores/toast';
import type { Project } from '@/types/domain';
import { formatDateTime, formatWordCount } from '@/utils/format';
import { projectStatusMeta } from '@/utils/status';

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const createModalOpen = ref(false);
const projects = ref<Project[]>([]);
const uploadMode = ref<'text' | 'file'>('text');
const selectedFile = ref<File | null>(null);

const filters = reactive({
  status: '',
  sort: 'created_desc',
  keyword: ''
});

const form = reactive({
  title: '',
  author: '',
  content: ''
});

const inProgressCount = computed(() => projects.value.filter((item) => item.status === 1 || item.status === 2).length);
const completedCount = computed(() => projects.value.filter((item) => item.status === 3).length);
const totalWordsLabel = computed(() => formatWordCount(projects.value.reduce((sum, item) => sum + (item.totalWords || 0), 0)));

async function loadProjects() {
  loading.value = true;
  try {
    projects.value = await novelApi.getProjects({
      status: filters.status,
      sort: filters.sort,
      keyword: filters.keyword,
      page: 1,
      size: 100
    });
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '加载项目失败', 'error');
  } finally {
    loading.value = false;
  }
}

function goTo(name: 'characters' | 'storyboards' | 'videos', projectId: number) {
  router.push({ name, query: { id: String(projectId) } });
}

function onFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  selectedFile.value = target.files?.[0] || null;
}

function closeCreateModal() {
  createModalOpen.value = false;
  uploadMode.value = 'text';
  selectedFile.value = null;
  form.title = '';
  form.author = '';
  form.content = '';
}

async function submitProject() {
  if (!form.title.trim()) {
    pushToast('请填写项目标题', 'warning');
    return;
  }

  submitting.value = true;
  try {
    let projectId: number;

    if (uploadMode.value === 'file') {
      if (!selectedFile.value) {
        pushToast('请先选择文件', 'warning');
        return;
      }
      projectId = await novelApi.uploadFile({
        file: selectedFile.value,
        title: form.title.trim(),
        author: form.author.trim() || '未知'
      });
    } else {
      if (!form.content.trim()) {
        pushToast('请输入小说内容', 'warning');
        return;
      }
      projectId = await novelApi.uploadNovel({
        title: form.title.trim(),
        author: form.author.trim() || '未知',
        content: form.content,
        fileName: `${form.title.trim()}.txt`
      });
    }

    pushToast('项目创建成功，进入人物审核', 'success');
    closeCreateModal();
    await loadProjects();
    goTo('characters', projectId);
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '创建项目失败', 'error');
  } finally {
    submitting.value = false;
  }
}

onMounted(loadProjects);
</script>
