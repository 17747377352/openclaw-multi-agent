import { reactive } from 'vue';

export type ToastTone = 'info' | 'success' | 'warning' | 'error';

export interface ToastItem {
  id: number;
  message: string;
  tone: ToastTone;
}

export const toastState = reactive<{ items: ToastItem[] }>({
  items: []
});

let seed = 1;

export function pushToast(message: string, tone: ToastTone = 'info') {
  const item: ToastItem = { id: seed++, message, tone };
  toastState.items.push(item);
  window.setTimeout(() => removeToast(item.id), 2600);
}

export function removeToast(id: number) {
  const index = toastState.items.findIndex((item) => item.id === id);
  if (index >= 0) {
    toastState.items.splice(index, 1);
  }
}
