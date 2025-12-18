import { QueueType } from '../types';
import { ENV } from './env';

// API Configuration
export const API_BASE_URL = `${ENV.API_URL}/api`;
export const WEBSOCKET_URL = ENV.WS_URL;

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