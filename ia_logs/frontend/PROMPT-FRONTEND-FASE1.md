# FASE 1: Types y Configuración Base

## PASO 1.1: Crear TypeScript Types e Interfaces

**Tareas:**
- Crear interfaces para API responses
- Crear types para componentes
- Crear types para stores

**Implementación:**

Archivo: src/lib/types/index.ts
```typescript
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
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que no hay errores de tipos
tsc --noEmit
```

---

## PASO 1.2: Crear Enums y Constantes

**Tareas:**
- Crear constantes de configuración
- Crear utilidades para enums
- Crear constantes de UI

**Implementación:**

Archivo: src/lib/utils/constants.ts
```typescript
import { QueueType } from '../types';

// API Configuration
export const API_BASE_URL = 'http://localhost:8080/api';
export const WEBSOCKET_URL = 'ws://localhost:8080/ws';

// UI Constants
export const REFRESH_INTERVAL = 5000; // 5 segundos
export const WEBSOCKET_RECONNECT_INTERVAL = 3000; // 3 segundos

// Queue Configuration
export const QUEUE_CONFIG = {
  [QueueType.CAJA]: {
    displayName: 'Caja',
    color: 'bg-blue-500',
    icon: '💰',
    avgTime: 5
  },
  [QueueType.PERSONAL_BANKER]: {
    displayName: 'Personal Banker',
    color: 'bg-green-500',
    icon: '👤',
    avgTime: 15
  },
  [QueueType.EMPRESAS]: {
    displayName: 'Empresas',
    color: 'bg-purple-500',
    icon: '🏢',
    avgTime: 20
  },
  [QueueType.GERENCIA]: {
    displayName: 'Gerencia',
    color: 'bg-red-500',
    icon: '👔',
    avgTime: 30
  }
} as const;

// Branch Offices
export const BRANCH_OFFICES = [
  'Sucursal Centro',
  'Sucursal Norte',
  'Sucursal Sur',
  'Sucursal Oriente',
  'Sucursal Poniente'
] as const;

// Validation
export const VALIDATION = {
  RUT_REGEX: /^[0-9]+-[0-9kK]{1}$/,
  PHONE_REGEX: /^(\+?56)?[0-9]{8,9}$/,
  MIN_PHONE_LENGTH: 8,
  MAX_PHONE_LENGTH: 12
} as const;
```

Archivo: src/lib/utils/helpers.ts
```typescript
import { QueueType, TicketStatus } from '../types';
import { QUEUE_CONFIG, VALIDATION } from './constants';

// Queue utilities
export function getQueueDisplayName(queueType: QueueType): string {
  return QUEUE_CONFIG[queueType].displayName;
}

export function getQueueColor(queueType: QueueType): string {
  return QUEUE_CONFIG[queueType].color;
}

export function getQueueIcon(queueType: QueueType): string {
  return QUEUE_CONFIG[queueType].icon;
}

// Validation utilities
export function validateRUT(rut: string): boolean {
  return VALIDATION.RUT_REGEX.test(rut.trim());
}

export function validatePhone(phone: string): boolean {
  if (!phone) return true; // Optional field
  return VALIDATION.PHONE_REGEX.test(phone.trim());
}

// Status utilities
export function getStatusColor(status: TicketStatus): string {
  switch (status) {
    case TicketStatus.EN_ESPERA:
      return 'text-yellow-600 bg-yellow-100';
    case TicketStatus.PROXIMO:
      return 'text-orange-600 bg-orange-100';
    case TicketStatus.ATENDIENDO:
      return 'text-blue-600 bg-blue-100';
    case TicketStatus.COMPLETADO:
      return 'text-green-600 bg-green-100';
    case TicketStatus.CANCELADO:
    case TicketStatus.NO_ATENDIDO:
      return 'text-red-600 bg-red-100';
    default:
      return 'text-gray-600 bg-gray-100';
  }
}

// Time utilities
export function formatWaitTime(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} min`;
  }
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  return `${hours}h ${remainingMinutes}m`;
}

// Date utilities
export function formatDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleString('es-CL', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar imports
npm run build
```

🔍 **PUNTO DE REVISIÓN 1.2:**
Después de completar el PASO 1.2, DETENTE y solicita revisión exhaustiva antes de continuar con FASE 2.