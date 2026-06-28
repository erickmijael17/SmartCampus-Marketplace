import { Component } from '@angular/core';
import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';

@Component({
  selector: 'app-root',
  imports: [MainLayoutComponent],
  template: '<app-main-layout />',
  styleUrl: './app.css'
})
export class App {}
