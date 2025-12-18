import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TicketService } from '$lib/services/ticketService';
import { QueueType } from '$lib/types';

// Mock the API module
vi.mock('$lib/services/api', () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn()
  }
}));

import { api } from '$lib/services/api';

describe('TicketService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('createTicket', () => {
    it('creates ticket with correct data', async () => {
      const mockResponse = {
        ticket: {
          id: 1,
          numero: 'C01',
          codigoReferencia: 'uuid-123',
          status: 'EN_ESPERA',
          positionInQueue: 1,
          estimatedWaitMinutes: 5
        },
        message: 'Ticket created successfully'
      };

      vi.mocked(api.post).mockResolvedValue(mockResponse);

      const request = {
        nationalId: '12345678-9',
        phoneNumber: '+56912345678',
        branchOffice: 'Sucursal Centro',
        queueType: QueueType.CAJA
      };

      const result = await TicketService.createTicket(request);

      expect(api.post).toHaveBeenCalledWith('/tickets', request);
      expect(result).toEqual(mockResponse);
    });

    it('handles API errors', async () => {
      const error = new Error('API Error');
      vi.mocked(api.post).mockRejectedValue(error);

      const request = {
        nationalId: '12345678-9',
        phoneNumber: '+56912345678',
        branchOffice: 'Sucursal Centro',
        queueType: QueueType.CAJA
      };

      await expect(TicketService.createTicket(request)).rejects.toThrow('API Error');
    });
  });

  describe('getTicketByCode', () => {
    it('retrieves ticket by reference code', async () => {
      const mockTicket = {
        id: 1,
        numero: 'C01',
        codigoReferencia: 'uuid-123',
        status: 'EN_ESPERA'
      };

      vi.mocked(api.get).mockResolvedValue(mockTicket);

      const result = await TicketService.getTicketByCode('uuid-123');

      expect(api.get).toHaveBeenCalledWith('/tickets/uuid-123');
      expect(result).toEqual(mockTicket);
    });
  });
});