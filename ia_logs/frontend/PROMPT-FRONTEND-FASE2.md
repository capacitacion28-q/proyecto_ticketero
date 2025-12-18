# FASE 2: Services y API Client

## PASO 2.1: Configurar Axios y API Client

**Tareas:**
- Configurar Axios con interceptors
- Crear cliente API base
- Manejo de errores global

**Implementación:**

Archivo: src/lib/services/api.ts
```typescript
import axios, { type AxiosInstance, type AxiosResponse } from 'axios';
import { API_BASE_URL } from '../utils/constants';
import type { ApiResponse } from '../types';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ API Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('❌ API Response Error:', error.response?.data || error.message);
    
    // Handle common errors
    if (error.response?.status === 409) {
      throw new Error('Ya existe un ticket activo para este RUT');
    }
    
    if (error.response?.status === 404) {
      throw new Error('Recurso no encontrado');
    }
    
    if (error.response?.status >= 500) {
      throw new Error('Error interno del servidor');
    }
    
    throw new Error(error.response?.data?.message || 'Error de conexión');
  }
);

// Generic API methods
export const api = {
  get: async <T>(url: string): Promise<T> => {
    const response = await apiClient.get<ApiResponse<T>>(url);
    return response.data.data;
  },

  post: async <T, D = any>(url: string, data?: D): Promise<T> => {
    const response = await apiClient.post<ApiResponse<T>>(url, data);
    return response.data.data;
  },

  put: async <T, D = any>(url: string, data?: D): Promise<T> => {
    const response = await apiClient.put<ApiResponse<T>>(url, data);
    return response.data.data;
  },

  delete: async <T>(url: string): Promise<T> => {
    const response = await apiClient.delete<ApiResponse<T>>(url);
    return response.data.data;
  }
};

export default apiClient;
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que axios está instalado
npm list axios
```

---

## PASO 2.2: Crear Services para comunicación con Backend

**Tareas:**
- Crear TicketService
- Crear DashboardService
- Crear WebSocketService

**Implementación:**

Archivo: src/lib/services/ticketService.ts
```typescript
import { api } from './api';
import type { 
  Ticket, 
  CreateTicketRequest, 
  CreateTicketResponse 
} from '../types';

export class TicketService {
  // Crear nuevo ticket
  static async createTicket(request: CreateTicketRequest): Promise<CreateTicketResponse> {
    return api.post<CreateTicketResponse>('/tickets', request);
  }

  // Consultar ticket por código de referencia
  static async getTicketByCode(codigoReferencia: string): Promise<Ticket> {
    return api.get<Ticket>(`/tickets/${codigoReferencia}`);
  }

  // Obtener todos los tickets activos
  static async getActiveTickets(): Promise<Ticket[]> {
    return api.get<Ticket[]>('/tickets/active');
  }

  // Cancelar ticket
  static async cancelTicket(codigoReferencia: string): Promise<void> {
    return api.delete<void>(`/tickets/${codigoReferencia}`);
  }
}
```

Archivo: src/lib/services/dashboardService.ts
```typescript
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
```

Archivo: src/lib/services/websocketService.ts
```typescript
import { WEBSOCKET_URL, WEBSOCKET_RECONNECT_INTERVAL } from '../utils/constants';

export type WebSocketMessage = {
  type: 'TICKET_CREATED' | 'TICKET_UPDATED' | 'DASHBOARD_UPDATE';
  data: any;
};

export class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectTimer: NodeJS.Timeout | null = null;
  private listeners: Map<string, Function[]> = new Map();

  connect(): void {
    try {
      this.ws = new WebSocket(WEBSOCKET_URL);
      
      this.ws.onopen = () => {
        console.log('🔌 WebSocket connected');
        this.clearReconnectTimer();
      };

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          this.notifyListeners(message.type, message.data);
        } catch (error) {
          console.error('❌ WebSocket message parse error:', error);
        }
      };

      this.ws.onclose = () => {
        console.log('🔌 WebSocket disconnected');
        this.scheduleReconnect();
      };

      this.ws.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
      };
    } catch (error) {
      console.error('❌ WebSocket connection error:', error);
      this.scheduleReconnect();
    }
  }

  disconnect(): void {
    this.clearReconnectTimer();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  on(eventType: string, callback: Function): void {
    if (!this.listeners.has(eventType)) {
      this.listeners.set(eventType, []);
    }
    this.listeners.get(eventType)!.push(callback);
  }

  off(eventType: string, callback: Function): void {
    const callbacks = this.listeners.get(eventType);
    if (callbacks) {
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  private notifyListeners(eventType: string, data: any): void {
    const callbacks = this.listeners.get(eventType);
    if (callbacks) {
      callbacks.forEach(callback => callback(data));
    }
  }

  private scheduleReconnect(): void {
    this.clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      console.log('🔄 Attempting WebSocket reconnection...');
      this.connect();
    }, WEBSOCKET_RECONNECT_INTERVAL);
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }
}

// Singleton instance
export const websocketService = new WebSocketService();
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar build
npm run build
```

🔍 **PUNTO DE REVISIÓN 2.2:**
Después de completar el PASO 2.2, DETENTE y solicita revisión exhaustiva antes de continuar con FASE 3.