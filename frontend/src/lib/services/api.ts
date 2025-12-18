import axios, { type AxiosInstance, type AxiosResponse } from 'axios';
import { API_BASE_URL } from '../utils/constants';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ API Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('❌ API Response Error:', error.response?.data || error.message);

    // Handle common errors
    if (error.response?.status === 409) {
      throw new Error('Ya existe un ticket activo para este RUT');
    }

    if (error.response?.status === 404) {
      throw new Error('Recurso no encontrado');
    }

    if (error.response?.status >= 500) {
      throw new Error('Error interno del servidor');
    }

    throw new Error(error.response?.data?.message || 'Error de conexión');
  }
);

// Generic API methods
export const api = {
  get: async <T>(url: string): Promise<T> => {
    const response = await apiClient.get<T>(url);
    return response.data;
  },

  post: async <T, D = any>(url: string, data?: D): Promise<T> => {
    const response = await apiClient.post<T>(url, data);
    return response.data;
  },

  put: async <T, D = any>(url: string, data?: D): Promise<T> => {
    const response = await apiClient.put<T>(url, data);
    return response.data;
  },

  delete: async <T>(url: string): Promise<T> => {
    const response = await apiClient.delete<T>(url);
    return response.data;
  }
};

export default apiClient;