import { request } from '@/api/http';
import type { VideoBatchResult, VideoTask } from '@/types/domain';

export const videoApi = {
  list(groupId: number, projectId?: number) {
    return request<VideoTask[]>(`/video/group/${groupId}`, undefined, { projectId });
  },
  get(taskId: number) {
    return request<VideoTask>(`/video/${taskId}`);
  },
  create(storyboardId: number) {
    return request<number>('/video/create', { method: 'POST' }, { storyboardId });
  },
  retry(taskId: number) {
    return request<void>(`/video/${taskId}/retry`, { method: 'POST' });
  },
  batch(groupId: number) {
    return request<VideoBatchResult>(`/video/batch/${groupId}`, { method: 'POST' });
  }
};
