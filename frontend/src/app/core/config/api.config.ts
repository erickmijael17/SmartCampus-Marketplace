export const API_CONFIG = {
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

