import { api } from './api';
import type { 
  DashboardMetrics, 
  Advisor, 
  Ticket 
} from '../types';

export class DashboardService {
  // Obtener métricas del dashboard
  static async getDashboardMetrics(): Promise<DashboardMetrics> {
    return api.get<DashboardMetrics>('/admin/dashboard');
  }

  // Obtener todos los asesores
  static async getAdvisors(): Promise<Advisor[]> {
    return api.get<Advisor[]>('/admin/advisors');
  }

  // Obtener tickets por asesor
  static async getTicketsByAdvisor(advisorId: number): Promise<Ticket[]> {
    return api.get<Ticket[]>(`/admin/advisors/${advisorId}/tickets`);
  }

  // Cambiar estado de asesor
  static async updateAdvisorStatus(advisorId: number, status: string): Promise<Advisor> {
    return api.put<Advisor>(`/admin/advisors/${advisorId}/status`, { status });
  }
}