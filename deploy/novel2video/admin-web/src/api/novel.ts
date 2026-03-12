import { request, uploadForm } from '@/api/http';
import type { ChapterGroup, Project, UploadNovelPayload } from '@/types/domain';

export interface ProjectQuery {
  status?: string;
  sort?: string;
  keyword?: string;
  page?: number;
  size?: number;
}

export const novelApi = {
  getProjects(query: ProjectQuery = {}) {
    return request<Project[]>('/novel/projects', undefined, query);
  },
  getProject(projectId: number) {
    return request<Project>(`/novel/${projectId}`);
  },
  getGroups(projectId: number) {
    return request<ChapterGroup[]>(`/novel/${projectId}/groups`);
  },
  uploadNovel(payload: UploadNovelPayload) {
    return request<number>('/novel/upload', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  uploadFile(payload: { file: File; title: string; author?: string; userId?: number }) {
    const formData = new FormData();
    formData.append('file', payload.file);
    formData.append('title', payload.title);
    formData.append('author', payload.author || '未知');
    formData.append('userId', String(payload.userId || 1));
    return uploadForm<number>('/novel/upload-file', formData);
  }
};
