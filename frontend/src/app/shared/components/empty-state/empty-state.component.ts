import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <section class="empty panel">
      <div>{{ icon }}</div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
    </section>
  `,
  styles: [`
    .empty {
      text-align: center;
      padding: 3rem 1.5rem;
    }
    div {
      color: var(--primary);
      font-size: 2rem;
      font-weight: 900;
    }
    h3 {
      margin: 0.75rem 0 0.3rem;
    }
    p {
      margin: 0;
      color: var(--text-muted);
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon = '□';
  @Input() title = 'Sin resultados';
  @Input() description = 'Prueba con otra búsqueda o categoría.';
}
