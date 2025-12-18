import { describe, it, expect, vi, beforeEach } from 'vitest';
import { get } from 'svelte/store';
import { ticketActions, ticketLoading, ticketError, currentTicket } from '$lib/stores/ticketStore';
import { QueueType } from '$lib/types';

// Mock the TicketService
vi.mock('$lib/services/ticketService', () => ({
  TicketService: {
    createTicket: vi.fn(),
    getTicketByCode: vi.fn(),
    getActiveTickets: vi.fn()
  }
}));

import { TicketService } from '$lib/services/ticketService';

describe('Ticket Store', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset stores
    ticketActions.clearCurrentTicket();
    ticketActions.clearError();
  });

  describe('createTicket', () => {
    it('sets loading state during creation', async () => {
      const mockResponse = {
        ticket: {
          id: 1,
          numero: 'C01',
          codigoReferencia: 'uuid-123',
          status: 'EN_ESPERA',
          positionInQueue: 1,
          estimatedWaitMinutes: 5,
          queueType: QueueType.CAJA
        }
      };

      vi.mocked(TicketService.createTicket).mockResolvedValue(mockResponse);

      const createPromise = ticketActions.createTicket({
        nationalId: '12345678-9',
        phoneNumber: '+56912345678',
        branchOffice: 'Sucursal Centro',
        queueType: QueueType.CAJA
      });

      // Should be loading
      expect(get(ticketLoading).isLoading).toBe(true);

      await createPromise;

      // Should not be loading anymore
      expect(get(ticketLoading).isLoading).toBe(false);
      expect(get(currentTicket)).toEqual(mockResponse.ticket);
    });

    it('handles creation errors', async () => {
      const error = new Error('API Error');
      vi.mocked(TicketService.createTicket).mockRejectedValue(error);

      await expect(ticketActions.createTicket({
        nationalId: '12345678-9',
        phoneNumber: '+56912345678',
        branchOffice: 'Sucursal Centro',
        queueType: QueueType.CAJA
      })).rejects.toThrow('API Error');

      expect(get(ticketLoading).isLoading).toBe(false);
      expect(get(ticketError).hasError).toBe(true);
    });
  });

  describe('clearCurrentTicket', () => {
    it('clears current ticket', () => {
      // Set a ticket first
      currentTicket.set({
        id: 1,
        numero: 'C01',
        codigoReferencia: 'uuid-123',
        status: 'EN_ESPERA',
        positionInQueue: 1,
        estimatedWaitMinutes: 5,
        queueType: QueueType.CAJA,
        nationalId: '12345678-9',
        branchOffice: 'Test',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });

      expect(get(currentTicket)).not.toBeNull();

      ticketActions.clearCurrentTicket();

      expect(get(currentTicket)).toBeNull();
    });
  });
});