import { describe, it, expect, vi, beforeEach } from 'vitest';
import { get } from 'svelte/store';
import { 
  dashboardActions, 
  dashboardLoading, 
  dashboardError, 
  dashboardMetrics 
} from '$lib/stores/dashboardStore';

vi.mock('$lib/services/dashboardService', () => ({
  DashboardService: {
    getDashboardData: vi.fn(),
    updateAdvisorStatus: vi.fn()
  }
}));

import { DashboardService } from '$lib/services/dashboardService';

describe('Dashboard Store', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    dashboardActions.clearError();
  });

  describe('loadDashboardData', () => {
    it('sets loading state during data fetch', async () => {
      const mockData = {
        totalTickets: 5,
        activeTickets: 2,
        completedTickets: 3,
        averageWaitTime: 10
      };

      vi.mocked(DashboardService.getDashboardData).mockResolvedValue(mockData);

      const loadPromise = dashboardActions.loadDashboardData();

      expect(get(dashboardLoading).isLoading).toBe(true);

      await loadPromise;

      expect(get(dashboardLoading).isLoading).toBe(false);
      expect(get(dashboardMetrics)).toEqual(mockData);
    });

    it('handles fetch errors', async () => {
      const error = new Error('API Error');
      vi.mocked(DashboardService.getDashboardData).mockRejectedValue(error);

      await expect(dashboardActions.loadDashboardData()).rejects.toThrow('API Error');

      expect(get(dashboardLoading).isLoading).toBe(false);
      expect(get(dashboardError).hasError).toBe(true);
    });
  });

  describe('updateAdvisorStatus', () => {
    it('updates advisor status successfully', async () => {
      vi.mocked(DashboardService.updateAdvisorStatus).mockResolvedValue(undefined);

      await dashboardActions.updateAdvisorStatus(1, 'AVAILABLE');

      expect(DashboardService.updateAdvisorStatus).toHaveBeenCalledWith(1, 'AVAILABLE');
    });

    it('handles update errors', async () => {
      const error = new Error('Update failed');
      vi.mocked(DashboardService.updateAdvisorStatus).mockRejectedValue(error);

      await expect(dashboardActions.updateAdvisorStatus(1, 'AVAILABLE')).rejects.toThrow('Update failed');
    });
  });
});