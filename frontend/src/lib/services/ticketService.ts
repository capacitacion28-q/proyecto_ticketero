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