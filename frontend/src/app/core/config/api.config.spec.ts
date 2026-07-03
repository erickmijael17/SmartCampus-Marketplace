import { API_CONFIG } from './api.config';

describe('API_CONFIG', () => {
  it('centralizes Gateway URLs as absolute localhost:18080 endpoints', () => {
    expect(API_CONFIG.gatewayUrl).toBe('http://localhost:18080');
    expect(API_CONFIG.endpoints.marketplace.categories).toBe('http://localhost:18080/api/v1/categorias');
    expect(API_CONFIG.endpoints.marketplace.products).toBe('http://localhost:18080/api/v1/productos');
    expect(API_CONFIG.endpoints.publicaciones.base).toBe('http://localhost:18080/api/v1/publicaciones');
    expect(API_CONFIG.endpoints.auth.login).toBe('http://localhost:18080/auth/login');
    expect(API_CONFIG.endpoints.chats.base).toBe('http://localhost:18080/api/v1/chats');
    expect(API_CONFIG.endpoints.marketplace.orders).toBe('http://localhost:18080/api/v1/ordenes');
    expect(API_CONFIG.endpoints.marketplace.payments).toBe('http://localhost:18080/api/v1/pagos');
  });
});
