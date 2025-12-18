import { writable, derived } from 'svelte/store';
import type { 
  DashboardMetrics, 
  Advisor, 
  LoadingState, 
  ErrorState 
} from '../types';
import { DashboardService } from '../services/dashboardService';

// State
export const dashboardMetrics = writable<DashboardMetrics | null>(null);
export const advisors = writable<Advisor[]>([]);
export const dashboardLoading = writable<LoadingState>({ isLoading: false });
export const dashboardError = writable<ErrorState>({ hasError: false });

// Derived stores
export const availableAdvisors = derived(
  advisors,
  ($advisors) => $advisors.filter(advisor => advisor.status === 'AVAILABLE')
);

export const busyAdvisors = derived(
  advisors,
  ($advisors) => $advisors.filter(advisor => advisor.status === 'BUSY')
);

export const offlineAdvisors = derived(
  advisors,
  ($advisors) => $advisors.filter(advisor => advisor.status === 'OFFLINE')
);

// Actions
export const dashboardActions = {
  async loadDashboardData() {
    dashboardLoading.set({ isLoading: true, message: 'Cargando dashboard...' });
    dashboardError.set({ hasError: false });
    
    try {
      const [metrics, advisorsList] = await Promise.all([
        DashboardService.getDashboardMetrics(),
        DashboardService.getAdvisors()
      ]);
      
      dashboardMetrics.set(metrics);
      advisors.set(advisorsList);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Error cargando dashboard';
      dashboardError.set({ hasError: true, message });
      throw error;
    } finally {
      dashboardLoading.set({ isLoading: false });
    }
  },

  async updateAdvisorStatus(advisorId: number, status: string) {
    try {
      const updatedAdvisor = await DashboardService.updateAdvisorStatus(advisorId, status);
      
      advisors.update(list => 
        list.map(advisor => 
          advisor.id === advisorId ? updatedAdvisor : advisor
        )
      );
    } catch (error) {
      console.error('Error updating advisor status:', error);
      throw error;
    }
  },

  clearError() {
    dashboardError.set({ hasError: false });
  }
};