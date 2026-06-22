import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page.component';
import { ListingDetailPageComponent } from './pages/listing-detail-page.component';
import { LoginPageComponent } from './pages/login-page.component';
import { RegisterPageComponent } from './pages/register-page.component';
import { PublishPageComponent } from './pages/publish-page.component';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
	{
		path: '',
		component: HomePageComponent
	},
	{
		path: 'publish',
		canActivate: [authGuard],
		component: PublishPageComponent
	},
	{
		path: 'listing/:id',
		component: ListingDetailPageComponent
	},
	{
		path: 'login',
		canActivate: [guestGuard],
		component: LoginPageComponent
	},
	{
		path: 'register',
		canActivate: [guestGuard],
		component: RegisterPageComponent
	},
	{
		path: 'publicar',
		redirectTo: 'publish'
	},
	{
		path: 'publicacion/:id',
		redirectTo: 'listing/:id'
	},
	{
		path: 'registro',
		redirectTo: 'register'
	},
	{
		path: '**',
		redirectTo: ''
	}
];
