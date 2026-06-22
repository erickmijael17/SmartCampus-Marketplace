export const API_CONFIG = {
  gatewayBaseUrl: 'http://localhost:28082',
  endpoints: {
    auth: {
      login: '/auth/login',
      register: '/auth/register',
      me: '/auth/me'
    },
    marketplace: {
      products: '/api/v1/productos',
      productDetail: (id: number) => `/api/v1/productos/detalle/${id}`,
      categories: '/api/v1/categorias',
      orders: '/api/v1/ordenes',
      payments: '/api/v1/pagos'
    }
  }
} as const;

export const API_BASE_URL = API_CONFIG.gatewayBaseUrl;

export function gatewayUrl(path: string): string {
  return `${API_CONFIG.gatewayBaseUrl}${path}`;
}
