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