import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  standalone: true,
  template: '<div class="loading">{{ label }}</div>',
  styles: [`
    .loading {
      padding: 2rem;
      text-align: center;
      color: var(--text-muted);
      font-weight: 800;
    }
  `]
})
export class LoadingComponent {
  @Input() label = 'Cargando...';
}
