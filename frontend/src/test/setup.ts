import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock environment variables
vi.mock('$env/static/public', () => ({
  PUBLIC_API_URL: 'http://localhost:8081',
  PUBLIC_WS_URL: 'ws://localhost:8081/ws'
}));

// Mock SvelteKit modules
vi.mock('$app/environment', () => ({
  browser: false,
  dev: true,
  building: false,
  version: '1.0.0'
}));

vi.mock('$app/navigation', () => ({
  goto: vi.fn(),
  invalidate: vi.fn(),
  invalidateAll: vi.fn(),
  preloadData: vi.fn(),
  preloadCode: vi.fn(),
  beforeNavigate: vi.fn(),
  afterNavigate: vi.fn()
}));

vi.mock('$app/stores', () => ({
  page: {
    subscribe: vi.fn()
  },
  navigating: {
    subscribe: vi.fn()
  },
  updated: {
    subscribe: vi.fn()
  }
}));