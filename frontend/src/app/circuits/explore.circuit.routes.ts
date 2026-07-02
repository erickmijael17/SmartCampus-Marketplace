import { Routes } from '@angular/router';
import { HomeComponent } from '../pages/home/home.component';
import { ListingDetailPageComponent } from '../pages/listing-detail-page.component';

export const exploreCircuitRoutes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'listing/:id',
    component: ListingDetailPageComponent
  },
  {
    path: 'publicacion/:id',
    redirectTo: 'listing/:id'
  }
];
