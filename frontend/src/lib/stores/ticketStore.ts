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
      currentTicket.set(response);

      // Add to tickets list
      tickets.update(list => [...list, response]);

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