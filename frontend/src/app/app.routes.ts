import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ListingDetailPageComponent } from './pages/listing-detail-page.component';
import { LoginPageComponent } from './pages/login-page.component';
import { RegisterComponent } from './pages/register/register.component';
import { PublishPageComponent } from './pages/publish-page.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { ChatComponent } from './pages/chat/chat.component';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
	{
		path: '',
		component: HomeComponent
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
		component: RegisterComponent
	},
	{
		path: 'profile',
		canActivate: [authGuard],
		component: ProfileComponent
	},
	{
		path: 'chat',
		canActivate: [authGuard],
		component: ChatComponent
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
