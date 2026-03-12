import type { ApiResult } from '@/types/api';

const API_BASE = import.meta.env.VITE_API_BASE || '/api';

export class ApiError extends Error {
  code?: number;

  constructor(message: string, code?: number) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
  }
}

function buildUrl(path: string, query?: Record<string, unknown>) {
  const url = new URL(`${API_BASE}${path}`, window.location.origin);
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') return;
      url.searchParams.set(key, String(value));
    });
  }
  return `${url.pathname}${url.search}`;
}

async function parseResponse<T>(response: Response): Promise<ApiResult<T>> {
  const text = await response.text();
  let payload: ApiResult<T> | null = null;

  try {
    payload = text ? (JSON.parse(text) as ApiResult<T>) : null;
  } catch (error) {
    throw new ApiError(`接口返回了无效 JSON: ${String(error)}`);
  }

  if (!response.ok) {
    throw new ApiError(payload?.message || `请求失败: ${response.status}`, payload?.code || response.status);
  }

  if (!payload) {
    throw new ApiError('接口未返回数据');
  }

  if (payload.code !== 200) {
    throw new ApiError(payload.message || '请求失败', payload.code);
  }

  return payload;
}

export async function request<T>(path: string, options?: RequestInit, query?: Record<string, unknown>): Promise<T> {
  const response = await fetch(buildUrl(path, query), {
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers || {})
    },
    ...options
  });

  const result = await parseResponse<T>(response);
  return result.data;
}

export async function uploadForm<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(buildUrl(path), {
    method: 'POST',
    body: formData
  });

  const result = await parseResponse<T>(response);
  return result.data;
}
