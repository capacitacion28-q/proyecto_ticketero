import { writable } from 'svelte/store';
import { websocketService, type WebSocketMessage } from '../services/websocketService';
import { tickets, dashboardMetrics, advisors } from './index';

// WebSocket connection state
export const wsConnected = writable(false);
export const wsReconnecting = writable(false);

// WebSocket actions
export const websocketActions = {
  connect() {
    websocketService.connect();
    
    // Listen for connection events
    websocketService.on('TICKET_CREATED', (data: any) => {
      tickets.update(list => [...list, data]);
    });
    
    websocketService.on('TICKET_UPDATED', (data: any) => {
      tickets.update(list => 
        list.map(ticket => 
          ticket.id === data.id ? { ...ticket, ...data } : ticket
        )
      );
    });
    
    websocketService.on('DASHBOARD_UPDATE', (data: any) => {
      if (data.metrics) {
        dashboardMetrics.set(data.metrics);
      }
      if (data.advisors) {
        advisors.set(data.advisors);
      }
    });
  },

  disconnect() {
    websocketService.disconnect();
    wsConnected.set(false);
    wsReconnecting.set(false);
  }
};