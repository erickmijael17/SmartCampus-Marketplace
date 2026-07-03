const GATEWAY_URL = '';
const gatewayUrl = (path: string): string => `${path}`;

export const API_CONFIG = {
  gatewayUrl: GATEWAY_URL,
  endpoints: {
    auth: {
      login: gatewayUrl('/auth/login'),
      register: gatewayUrl('/auth/register'),
      me: gatewayUrl('/auth/me'),
      profile: gatewayUrl('/auth/profile')
    },
    marketplace: {
      products: gatewayUrl('/api/v1/productos'),
      productDetail: (id: number) => gatewayUrl(`/api/v1/productos/detalle/${id}`),
      productDelete: (id: number) => gatewayUrl(`/api/v1/productos/${id}`),
      categories: gatewayUrl('/api/v1/categorias'),
      orders: gatewayUrl('/api/v1/ordenes'),
      payments: gatewayUrl('/api/v1/pagos'),
      mercadoPagoPreference: gatewayUrl('/api/v1/pagos/mercadopago/preference'),
      mercadoPagoConfirm: gatewayUrl('/api/v1/pagos/mercadopago/confirmar'),
      mercadoPagoValidateTransaction: (pagoId: number) =>
        gatewayUrl(`/api/v1/pagos/${pagoId}/validar-transaccion`)
    },
    personas: {
      base: gatewayUrl('/auth/profile'),
      me: gatewayUrl('/auth/profile'),
      detail: (_id: number) => gatewayUrl('/auth/profile')
    },
    chats: {
      base: gatewayUrl('/api/v1/chats'),
      detail: (id: number) => gatewayUrl(`/api/v1/chats/${id}`),
      messages: (id: number) => gatewayUrl(`/api/v1/chats/${id}/mensajes`)
    },
    payments: {
      sellerSummary: (idVendedor: number) => gatewayUrl(`/api/v1/pagos/vendedor/${idVendedor}/resumen`)
    },
    media: {
      base: gatewayUrl('/api/v1/media'),
      detail: (id: number) => gatewayUrl(`/api/v1/media/${id}`)
    },
    favoritos: {
      base: gatewayUrl('/api/v1/favoritos'),
      detail: (id: number) => gatewayUrl(`/api/v1/favoritos/${id}`)
    },
    calificaciones: {
      base: gatewayUrl('/api/v1/calificaciones'),
      detail: (id: number) => gatewayUrl(`/api/v1/calificaciones/${id}`)
    },
    publicaciones: {
      base: gatewayUrl('/api/v1/publicaciones'),
      detail: (id: number) => gatewayUrl(`/api/v1/publicaciones/${id}`)
    }
  }
} as const;
