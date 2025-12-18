import { writable } from 'svelte/store';

// UI State
export const isMenuOpen = writable(false);
export const currentPage = writable<'home' | 'totem' | 'admin'>('home');
export const notifications = writable<Array<{
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
  duration?: number;
}>>([]);

// UI Actions
export const uiActions = {
  toggleMenu() {
    isMenuOpen.update(open => !open);
  },

  setCurrentPage(page: 'home' | 'totem' | 'admin') {
    currentPage.set(page);
  },

  showNotification(
    type: 'success' | 'error' | 'info' | 'warning',
    message: string,
    duration = 5000
  ) {
    const id = Date.now().toString();
    
    notifications.update(list => [...list, { id, type, message, duration }]);
    
    // Auto remove after duration
    if (duration > 0) {
      setTimeout(() => {
        notifications.update(list => list.filter(n => n.id !== id));
      }, duration);
    }
  },

  removeNotification(id: string) {
    notifications.update(list => list.filter(n => n.id !== id));
  },

  clearNotifications() {
    notifications.set([]);
  }
};