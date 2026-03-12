<template>
  <AppShell
    title="人物审核"
    :subtitle="project ? `${project.title} · 先确认角色，再批量生成人物图。` : '加载项目中...'"
    back-to="/projects"
  >
    <template #actions>
      <button class="btn btn-secondary" @click="reload">刷新</button>
      <button class="btn btn-primary" :disabled="busy" @click="extractCharacters">AI 提取人物</button>
      <button class="btn btn-success" :disabled="busy || characters.length === 0" @click="generateAllImages">批量生图</button>
    </template>

    <section class="panel section-gap" v-if="project">
      <div class="selection-bar">
        <div>
          <div class="card-title">{{ project.title }}</div>
          <div class="card-muted" style="margin-top: 8px;">
            作者 {{ project.author || '未知' }} · 章节 {{ project.chapterCount || 0 }} · 更新时间 {{ formatDateTime(project.updatedAt) }}
          </div>
        </div>
        <div class="page-actions">
          <button class="btn btn-secondary" :disabled="selectedIds.length === 0 || busy" @click="approveSelected">批量确认</button>
          <button class="btn btn-danger" :disabled="selectedIds.length === 0 || busy" @click="rejectSelected">批量拒绝</button>
        </div>
      </div>
    </section>

    <section class="grid stats section-gap">
      <StatCard label="人物总数" :value="characters.length" meta="当前项目已识别角色" />
      <StatCard label="已确认" :value="approvedCount" meta="可进入分镜阶段" />
      <StatCard label="生图完成" :value="imageReadyCount" meta="已有标准照" />
      <StatCard label="待处理" :value="pendingCount" meta="仍需人工审核" />
    </section>

    <section class="filter-bar section-gap">
      <div class="selection-bar">
        <label class="inline-row" style="gap: 10px;">
          <input type="checkbox" :checked="allSelected" @change="toggleAll(($event.target as HTMLInputElement).checked)" />
          <span>全选当前列表</span>
        </label>
        <input v-model="keyword" class="input" style="max-width: 280px;" placeholder="搜索人物名或角色类型" />
      </div>
    </section>

    <section class="section-gap">
      <LoadingBlock v-if="loading" label="人物列表加载中..." />
      <EmptyState
        v-else-if="filteredCharacters.length === 0"
        title="还没有人物"
        description="先运行一次人物提取，或者调整上面的筛选条件。"
        icon="CAST"
      >
        <template #actions>
          <button class="btn btn-primary" :disabled="busy" @click="extractCharacters">开始提取</button>
        </template>
      </EmptyState>
      <div v-else class="grid characters">
        <article v-for="character in filteredCharacters" :key="character.id" class="character-card list-card">
          <div class="card-header">
            <label class="inline-row" style="gap: 10px; align-items: flex-start;">
              <input type="checkbox" :checked="selectedIds.includes(character.id)" @change="toggleOne(character.id)" />
              <div>
                <div class="card-title">{{ character.name }}</div>
                <div class="card-muted" style="margin-top: 6px;">{{ character.role || '未分类角色' }}</div>
              </div>
            </label>
            <StatusBadge v-bind="reviewStatusMeta(character.status)" />
          </div>

          <div class="card-cover">
            <img v-if="character.imageUrl || character.seedImageUrl" :src="character.imageUrl || character.seedImageUrl" :alt="character.name" />
            <span v-else>暂无人物图</span>
          </div>

          <div class="card-muted" style="line-height: 1.7; min-height: 88px;">
            {{ character.description || '暂无描述' }}
          </div>

          <div class="card-actions">
            <button class="btn btn-secondary" :disabled="busy" @click="openEditor(character)">编辑</button>
            <button class="btn btn-success" :disabled="busy" @click="approveCharacter(character.id)">确认</button>
            <button class="btn btn-warning" :disabled="busy" @click="generateImage(character.id)">生图</button>
            <button class="btn btn-danger" :disabled="busy" @click="rejectCharacter(character.id)">拒绝</button>
          </div>
        </article>
      </div>
    </section>

    <ModalDialog
      :open="editorOpen"
      title="编辑人物信息"
      description="这里的修改会直接写回后端，后续生图会使用更新后的描述。"
      @close="closeEditor"
    >
      <div class="grid" style="gap: 14px;">
        <input v-model="editorForm.name" class="input" placeholder="人物名称" />
        <input v-model="editorForm.role" class="input" placeholder="角色类型" />
        <textarea v-model="editorForm.description" class="textarea" placeholder="人物描述" />
      </div>
      <template #footer>
        <button class="btn btn-ghost" @click="closeEditor">取消</button>
        <button class="btn btn-primary" :disabled="busy || editorTargetId === null" @click="saveCharacter">保存</button>
      </template>
    </ModalDialog>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import AppShell from '@/components/layout/AppShell.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import LoadingBlock from '@/components/ui/LoadingBlock.vue';
import ModalDialog from '@/components/ui/ModalDialog.vue';
import StatCard from '@/components/ui/StatCard.vue';
import StatusBadge from '@/components/ui/StatusBadge.vue';
import { characterApi } from '@/api/character';
import { novelApi } from '@/api/novel';
import { pushToast } from '@/stores/toast';
import type { Character, Project } from '@/types/domain';
import { formatDateTime } from '@/utils/format';
import { reviewStatusMeta } from '@/utils/status';

const route = useRoute();
const projectId = Number(route.query.id || 0);
const loading = ref(false);
const busy = ref(false);
const project = ref<Project | null>(null);
const characters = ref<Character[]>([]);
const keyword = ref('');
const selectedIds = ref<number[]>([]);
const editorOpen = ref(false);
const editorTargetId = ref<number | null>(null);
const editorForm = reactive({
  name: '',
  role: '',
  description: ''
});

const filteredCharacters = computed(() => {
  const q = keyword.value.trim().toLowerCase();
  if (!q) return characters.value;
  return characters.value.filter((item) =>
    [item.name, item.role, item.description].some((field) => (field || '').toLowerCase().includes(q))
  );
});

const approvedCount = computed(() => characters.value.filter((item) => item.status === 'approved').length);
const imageReadyCount = computed(() => characters.value.filter((item) => item.imageUrl || item.seedImageUrl).length);
const pendingCount = computed(() => characters.value.filter((item) => item.status === 'pending' || item.status === 'rejected').length);
const allSelected = computed(() => filteredCharacters.value.length > 0 && filteredCharacters.value.every((item) => selectedIds.value.includes(item.id)));

async function loadProject() {
  project.value = await novelApi.getProject(projectId);
}

async function loadCharacters() {
  characters.value = await characterApi.list(projectId);
}

async function reload() {
  if (!projectId) {
    pushToast('缺少项目 ID', 'error');
    return;
  }

  loading.value = true;
  try {
    await Promise.all([loadProject(), loadCharacters()]);
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '加载人物失败', 'error');
  } finally {
    loading.value = false;
  }
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
  selectedIds.value = checked ? filteredCharacters.value.map((item) => item.id) : [];
}

async function extractCharacters() {
  busy.value = true;
  try {
    characters.value = await characterApi.extract(projectId);
    selectedIds.value = [];
    pushToast('人物提取完成', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '人物提取失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function approveCharacter(id: number) {
  busy.value = true;
  try {
    await characterApi.confirm([id]);
    await loadCharacters();
    pushToast('人物已确认', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '确认失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function approveSelected() {
  if (selectedIds.value.length === 0) {
    pushToast('请先选择人物', 'warning');
    return;
  }

  busy.value = true;
  try {
    await characterApi.confirm(selectedIds.value);
    selectedIds.value = [];
    await loadCharacters();
    pushToast('批量确认完成', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量确认失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function rejectCharacter(id: number) {
  busy.value = true;
  try {
    await characterApi.update(id, { seedStatus: 3, isConfirmed: 0 });
    await loadCharacters();
    pushToast('人物已标记为拒绝', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '拒绝失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function rejectSelected() {
  if (selectedIds.value.length === 0) {
    pushToast('请先选择人物', 'warning');
    return;
  }

  busy.value = true;
  try {
    await Promise.all(selectedIds.value.map((id) => characterApi.update(id, { seedStatus: 3, isConfirmed: 0 })));
    selectedIds.value = [];
    await loadCharacters();
    pushToast('批量拒绝完成', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量拒绝失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function generateImage(id: number) {
  busy.value = true;
  try {
    await characterApi.generateImage(id);
    await loadCharacters();
    pushToast('人物图生成请求已提交', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '人物生图失败', 'error');
  } finally {
    busy.value = false;
  }
}

async function generateAllImages() {
  const candidates = characters.value.filter((item) => !item.imageUrl && !item.seedImageUrl);
  if (candidates.length === 0) {
    pushToast('没有待生图的人物', 'warning');
    return;
  }

  busy.value = true;
  try {
    await Promise.allSettled(candidates.map((item) => characterApi.generateImage(item.id)));
    await loadCharacters();
    pushToast(`已提交 ${candidates.length} 个人物的生图请求`, 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '批量生图失败', 'error');
  } finally {
    busy.value = false;
  }
}

function openEditor(character: Character) {
  editorTargetId.value = character.id;
  editorForm.name = character.name;
  editorForm.role = character.role || '';
  editorForm.description = character.description || '';
  editorOpen.value = true;
}

function closeEditor() {
  editorOpen.value = false;
  editorTargetId.value = null;
  editorForm.name = '';
  editorForm.role = '';
  editorForm.description = '';
}

async function saveCharacter() {
  if (editorTargetId.value === null) return;
  busy.value = true;
  try {
    await characterApi.update(editorTargetId.value, {
      name: editorForm.name,
      role: editorForm.role,
      description: editorForm.description,
      userEditedDescription: editorForm.description
    });
    await loadCharacters();
    closeEditor();
    pushToast('人物信息已更新', 'success');
  } catch (error) {
    pushToast(error instanceof Error ? error.message : '保存失败', 'error');
  } finally {
    busy.value = false;
  }
}

onMounted(reload);
</script>
