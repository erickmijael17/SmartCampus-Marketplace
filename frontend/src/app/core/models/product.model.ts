export interface CategoriaDto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion?: string | null;
  activo?: boolean | null;
}

export interface ProductResponse {
  id: number;
  titulo: string;
  descripcion?: string | null;
  precio: number | string;
  moneda: string;
  estado: string;
  idCategoria: number;
  idVendedor: number;
  publicadoEn?: string | null;
  actualizadoEn?: string | null;
  categoria?: CategoriaDto | null;
}

export interface ProductRequest {
  titulo: string;
  descripcion?: string | null;
  precio: number;
  moneda: string;
  estado: string;
  idCategoria: number;
  idVendedor: number;
}

export interface CheckoutRequest {
  idComprador: number;
  idProducto: number;
  cantidad: number;
  precioUnitario: number;
  metodoPago: string;
  referenciaTransaccion?: string;
}

export interface MarketplaceListing {
  id: number;
  title: string;
  description: string;
  price: number;
  currency: string;
  status: string;
  categoryId: number;
  categoryLabel: string;
  sellerId: number;
  sellerLabel: string;
  imageUrl: string;
  publishedAt?: string | null;
}

export interface PurchaseSummary {
  orderId: number;
  paymentId: number;
  status: string;
}
