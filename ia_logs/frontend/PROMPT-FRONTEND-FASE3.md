# FASE 3: Stores y Estado Global

## PASO 3.1: Crear Svelte Stores para manejo de estado

**Tareas:**
- Crear store para tickets
- Crear store para dashboard
- Crear store para UI state

**Implementación:**

Archivo: src/lib/stores/ticketStore.ts
```typescript
import { writable, derived } from 'svelte/store';
import type { Ticket, CreateTicketRequest, LoadingState, ErrorState } from '../types';
import { TicketService } from '../services/ticketService';

// State
export const tickets = writable<Ticket[]>([]);
export const currentTicket = writable<Ticket | null>(null);
export const ticketLoading = writable<LoadingState>({ isLoading: false });
export const ticketError = writable<ErrorState>({ hasError: false });

// Derived stores
export const activeTickets = derived(
  tickets,
  ($tickets) => $tickets.filter(ticket => 
    ['EN_ESPERA', 'PROXIMO', 'ATENDIENDO'].includes(ticket.status)
  )
);

export const ticketsByQueue = derived(
  tickets,
  ($tickets) => {
    const grouped = new Map();
    $tickets.forEach(ticket => {
      if (!grouped.has(ticket.queueType)) {
        grouped.set(ticket.queueType, []);
      }
      grouped.get(ticket.queueType).push(ticket);
    });
    return grouped;
  }
);

// Actions
export const ticketActions = {
  async createTicket(request: CreateTicketRequest) {
    ticketLoading.set({ isLoading: true, message: 'Creando ticket...' });
    ticketError.set({ hasError: false });
    
    try {
      const response = await TicketService.createTicket(request);
      currentTicket.set(response.ticket);
      
      // Add to tickets list
      tickets.update(list => [...list, response.ticket]);
      
      return response;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Error desconocido';
      ticketError.set({ hasError: true, message });
      throw error;
    } finally {
      ticketLoading.set({ isLoading: false });
    }
  },

  async getTicketByCode(codigoReferencia: string) {
    ticketLoading.set({ isLoading: true, message: 'Consultando ticket...' });
    ticketError.set({ hasError: false });
    
    try {
      const ticket = await TicketService.getTicketByCode(codigoReferencia);
      currentTicket.set(ticket);
      return ticket;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Ticket no encontrado';
      ticketError.set({ hasError: true, message });
      throw error;
    } finally {
      ticketLoading.set({ isLoading: false });
    }
  },

  async loadActiveTickets() {
    try {
      const activeTicketsList = await TicketService.getActiveTickets();
      tickets.set(activeTicketsList);
    } catch (error) {
      console.error('Error loading active tickets:', error);
    }
  },

  clearCurrentTicket() {
    currentTicket.set(null);
  },

  clearError() {
    ticketError.set({ hasError: false });
  }
};
```

Archivo: src/lib/stores/dashboardStore.ts
```typescript
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
```

Archivo: src/lib/stores/uiStore.ts
```typescript
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
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que stores compilan
npm run build
```

🔍 **PUNTO DE REVISIÓN 3.1:**
Después de completar el PASO 3.1, DETENTE y solicita revisión exhaustiva antes de continuar con FASE 4.