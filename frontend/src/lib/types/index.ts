// Enums del backend
export enum QueueType {
  CAJA = 'CAJA',
  PERSONAL_BANKER = 'PERSONAL_BANKER',
  EMPRESAS = 'EMPRESAS',
  GERENCIA = 'GERENCIA'
}

export enum TicketStatus {
  EN_ESPERA = 'EN_ESPERA',
  PROXIMO = 'PROXIMO',
  ATENDIENDO = 'ATENDIENDO',
  COMPLETADO = 'COMPLETADO',
  CANCELADO = 'CANCELADO',
  NO_ATENDIDO = 'NO_ATENDIDO'
}

export enum AdvisorStatus {
  AVAILABLE = 'AVAILABLE',
  BUSY = 'BUSY',
  OFFLINE = 'OFFLINE'
}

// Interfaces principales
export interface Ticket {
  id: number;
  codigoReferencia: string;
  numero: string;
  nationalId: string;
  telefono?: string;
  branchOffice: string;
  queueType: QueueType;
  status: TicketStatus;
  positionInQueue: number;
  estimatedWaitMinutes: number;
  assignedAdvisorId?: number;
  assignedModuleNumber?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Advisor {
  id: number;
  name: string;
  email: string;
  status: AdvisorStatus;
  moduleNumber: number;
  assignedTicketsCount: number;
  createdAt: string;
  updatedAt: string;
}

// DTOs para requests
export interface CreateTicketRequest {
  nationalId: string;
  telefono?: string;
  branchOffice: string;
  queueType: QueueType;
}

export interface CreateTicketResponse {
  ticket: Ticket;
  message: string;
}

// Dashboard types
export interface DashboardMetrics {
  totalTickets: number;
  activeTickets: number;
  completedTickets: number;
  averageWaitTime: number;
  queueStats: QueueStats[];
  advisorStats: AdvisorStats[];
}

export interface QueueStats {
  queueType: QueueType;
  totalTickets: number;
  activeTickets: number;
  averageWaitTime: number;
}

export interface AdvisorStats {
  advisor: Advisor;
  currentTickets: number;
  completedToday: number;
}

// UI types
export interface LoadingState {
  isLoading: boolean;
  message?: string;
}

export interface ErrorState {
  hasError: boolean;
  message?: string;
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}