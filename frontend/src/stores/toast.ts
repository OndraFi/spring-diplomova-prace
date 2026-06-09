import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<Toast[]>([]);
  let counter = 0;

  const add = (message: string, type: Toast['type'] = 'info') => {
    const id = ++counter;
    toasts.value.push({ id, message, type });
    setTimeout(() => {
      remove(id);
    }, 4000);
  };

  const remove = (id: number) => {
    toasts.value = toasts.value.filter(t => t.id !== id);
  };

  return { toasts, add, remove };
});
