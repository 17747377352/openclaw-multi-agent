import { request } from '@/api/http';
import type { Storyboard } from '@/types/domain';

export interface StoryboardUpdatePayload {
  description?: string;
  prompt?: string;
  characterIds?: string;
  frameStatus?: number;
  isConfirmed?: number;
}

export const storyboardApi = {
  list(groupId: number) {
    return request<Storyboard[]>(`/storyboard/group/${groupId}`);
  },
  generate(groupId: number, content?: string) {
    return request<Storyboard[]>('/storyboard/generate', { method: 'POST' }, { groupId, content });
  },
  generateFrame(storyboardId: number, characterIds: number[]) {
    return request<string>(`/storyboard/${storyboardId}/frame`, {
      method: 'POST',
      body: JSON.stringify(characterIds)
    });
  },
  update(storyboardId: number, payload: StoryboardUpdatePayload) {
    return request<void>(`/storyboard/${storyboardId}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  confirm(storyboardIds: number[]) {
    return request<void>('/storyboard/confirm', {
      method: 'POST',
      body: JSON.stringify(storyboardIds)
    });
  }
};
