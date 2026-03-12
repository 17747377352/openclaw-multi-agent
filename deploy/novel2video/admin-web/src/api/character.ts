import { request } from '@/api/http';
import type { Character } from '@/types/domain';

export interface CharacterUpdatePayload {
  name?: string;
  description?: string;
  userEditedDescription?: string;
  role?: string;
  gender?: number;
  seedStatus?: number;
  isConfirmed?: number;
}

export const characterApi = {
  list(projectId: number) {
    return request<Character[]>(`/character/project/${projectId}`);
  },
  extract(projectId: number, content?: string) {
    return request<Character[]>('/character/extract', { method: 'POST' }, { projectId, content });
  },
  generateImage(characterId: number) {
    return request<string>(`/character/${characterId}/image`, { method: 'POST' });
  },
  update(characterId: number, payload: CharacterUpdatePayload) {
    return request<void>(`/character/${characterId}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  confirm(characterIds: number[]) {
    return request<void>('/character/confirm', {
      method: 'POST',
      body: JSON.stringify(characterIds)
    });
  }
};
