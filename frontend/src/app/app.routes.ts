import { Routes } from '@angular/router';
import { exploreCircuitRoutes } from './circuits/explore.circuit.routes';
import { authProfileCircuitRoutes } from './circuits/auth-profile.circuit.routes';
import { publishPaymentCircuitRoutes } from './circuits/publish-payment.circuit.routes';
import { chatCircuitRoutes } from './circuits/chat.circuit.routes';

export const routes: Routes = [
	...exploreCircuitRoutes,
	...authProfileCircuitRoutes,
	...publishPaymentCircuitRoutes,
	...chatCircuitRoutes,
	{
		path: '**',
		redirectTo: ''
	}
];
