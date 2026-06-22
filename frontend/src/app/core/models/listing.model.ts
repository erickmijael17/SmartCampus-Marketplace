export type ListingType = 'producto' | 'servicio';

export interface Listing {
  id: number;
  title: string;
  price: number;
  description: string;
  category: string;
  type: ListingType;
  seller: string;
  imageUrl: string;
  stock: number;
}

export interface NewListing {
  title: string;
  price: number;
  description: string;
  category: string;
  type: ListingType;
  seller: string;
  imageUrl: string;
  stock: number;
}
