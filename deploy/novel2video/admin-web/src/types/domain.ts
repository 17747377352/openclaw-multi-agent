export interface Project {
  id: number;
  title: string;
  author: string;
  status: number;
  rawStatus: number;
  chapterCount: number;
  totalChapters: number;
  totalWords: number;
  progress: number;
  characterCount: number;
  storyboardCount: number;
  createdAt: string;
  updatedAt: string;
  originalFilePath?: string;
}

export interface ChapterGroup {
  id: number;
  projectId: number;
  groupNumber: number;
  name: string;
  chapterIds?: string;
  startChapter?: number;
  endChapter?: number;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Character {
  id: number;
  projectId: number;
  name: string;
  role: string;
  description: string;
  imageUrl?: string;
  seedImageUrl?: string;
  seedStatus?: number;
  isConfirmed?: number;
  status: 'pending' | 'approved' | 'generating' | 'rejected';
  createdAt?: string;
  updatedAt?: string;
}

export interface Storyboard {
  id: number;
  groupId: number;
  sceneNumber: number;
  title: string;
  location: string;
  description: string;
  prompt: string;
  characterIds?: string;
  imageUrl?: string;
  frameImageUrl?: string;
  frameStatus?: number;
  isConfirmed?: number;
  status: 'pending' | 'approved' | 'generating' | 'rejected';
  createdAt?: string;
  updatedAt?: string;
}

export interface VideoTask {
  id: number;
  taskId: string;
  groupId: number;
  storyboardId: number;
  title: string;
  videoUrl?: string;
  duration: string;
  videoDuration?: number;
  resolution: string;
  model: string;
  progress: number;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  rawStatus?: number;
  failReason?: string;
  retryCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface UploadNovelPayload {
  title: string;
  author?: string;
  filePath?: string;
  content?: string;
  fileName?: string;
  description?: string;
  userId?: number;
}

export interface VideoBatchResult {
  submitted: number;
  skipped: number;
}
