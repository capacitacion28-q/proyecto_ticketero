// Environment variables with fallbacks
export const ENV = {
  API_URL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  WS_URL: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
  APP_NAME: import.meta.env.VITE_APP_NAME || 'Sistema Ticketero',
  ENVIRONMENT: import.meta.env.VITE_ENVIRONMENT || 'development',
  ENABLE_DEBUG: import.meta.env.VITE_ENABLE_DEBUG === 'true',
  AUTO_REFRESH_INTERVAL: parseInt(import.meta.env.VITE_AUTO_REFRESH_INTERVAL || '5000')
} as const;

// Development helpers
export const isDevelopment = ENV.ENVIRONMENT === 'development';
export const isProduction = ENV.ENVIRONMENT === 'production';