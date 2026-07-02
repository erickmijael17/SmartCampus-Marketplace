import { Routes } from '@angular/router';
import { ChatComponent } from '../pages/chat/chat.component';
import { authGuard } from '../guards/auth.guard';

export const chatCircuitRoutes: Routes = [
  {
    path: 'chat',
    canActivate: [authGuard],
    component: ChatComponent
  }
];
