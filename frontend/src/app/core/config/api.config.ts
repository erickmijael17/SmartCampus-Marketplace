export const API_CONFIG = {
  endpoints: {
    auth: {
      login: '/auth/login',
      register: '/auth/register',
      me: '/auth/me',
      profile: '/auth/profile'
    },
    marketplace: {
      products: '/api/v1/productos',
      productDetail: (id: number) => `/api/v1/productos/detalle/${id}`,
      categories: '/api/v1/categorias',
      orders: '/api/v1/ordenes',
      payments: '/api/v1/pagos',
      mercadoPagoPreference: '/api/v1/pagos/mercadopago/preference',
      mercadoPagoConfirm: '/api/v1/pagos/mercadopago/confirmar',
      mercadoPagoValidateTransaction: (pagoId: number) => `/api/v1/pagos/${pagoId}/validar-transaccion`
    },
    personas: {
      base: '/auth/profile',
      me: '/auth/profile',
      detail: (_id: number) => '/auth/profile'
    },
    chats: {
      base: '/api/v1/chats',
      detail: (id: number) => `/api/v1/chats/${id}`,
      messages: (id: number) => `/api/v1/chats/${id}/mensajes`
    },
    payments: {
      sellerSummary: (idVendedor: number) => `/api/v1/pagos/vendedor/${idVendedor}/resumen`
    },
    media: {
      base: '/api/v1/media',
      detail: (id: number) => `/api/v1/media/${id}`
    },
    favoritos: {
      base: '/api/v1/favoritos',
      detail: (id: number) => `/api/v1/favoritos/${id}`
    },
    calificaciones: {
      base: '/api/v1/calificaciones',
      detail: (id: number) => `/api/v1/calificaciones/${id}`
    },
    publicaciones: {
      base: '/api/v1/publicaciones',
      detail: (id: number) => `/api/v1/publicaciones/${id}`
    }
  }
} as const;
