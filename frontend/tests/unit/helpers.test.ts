import { describe, it, expect } from 'vitest';
import { 
  formatWaitTime, 
  formatDateTime, 
  getStatusColor, 
  getQueueDisplayName 
} from '$lib/utils/helpers';
import { QueueType, TicketStatus } from '$lib/types';

describe('Helper Functions', () => {
  describe('formatWaitTime', () => {
    it('formats minutes correctly', () => {
      expect(formatWaitTime(5)).toBe('5 min');
      expect(formatWaitTime(1)).toBe('1 min');
      expect(formatWaitTime(0)).toBe('0 min');
    });

    it('handles undefined/null values', () => {
      expect(formatWaitTime(undefined)).toBe('0 min');
      expect(formatWaitTime(null)).toBe('0 min');
    });
  });

  describe('formatDateTime', () => {
    it('formats ISO date string', () => {
      const date = '2025-12-17T10:30:00.000Z';
      const formatted = formatDateTime(date);
      
      expect(formatted).toMatch(/\d{2}\/\d{2}\/\d{4} \d{2}:\d{2}/);
    });

    it('handles invalid dates', () => {
      expect(formatDateTime('invalid-date')).toBe('Fecha inválida');
      expect(formatDateTime('')).toBe('Fecha inválida');
    });
  });

  describe('getStatusColor', () => {
    it('returns correct colors for each status', () => {
      expect(getStatusColor(TicketStatus.EN_ESPERA)).toContain('yellow');
      expect(getStatusColor(TicketStatus.ATENDIENDO)).toContain('blue');
      expect(getStatusColor(TicketStatus.COMPLETADO)).toContain('green');
      expect(getStatusColor(TicketStatus.CANCELADO)).toContain('red');
    });

    it('returns default color for unknown status', () => {
      expect(getStatusColor('UNKNOWN' as TicketStatus)).toContain('gray');
    });
  });

  describe('getQueueDisplayName', () => {
    it('returns correct display names', () => {
      expect(getQueueDisplayName(QueueType.CAJA)).toBe('Caja');
      expect(getQueueDisplayName(QueueType.PERSONAL_BANKER)).toBe('Personal Banker');
      expect(getQueueDisplayName(QueueType.EMPRESAS)).toBe('Empresas');
      expect(getQueueDisplayName(QueueType.GERENCIA)).toBe('Gerencia');
    });

    it('handles unknown queue types', () => {
      expect(getQueueDisplayName('UNKNOWN' as QueueType)).toBe('Desconocido');
    });
  });
});