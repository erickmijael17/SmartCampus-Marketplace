import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [FormsModule],
  template: `
    <label class="search">
      <span>⌕</span>
      <input type="search" [(ngModel)]="value" (ngModelChange)="queryChange.emit(value)" placeholder="Buscar publicaciones en el campus">
    </label>
  `,
  styles: [`
    .search {
      display: flex;
      align-items: center;
      gap: 0.65rem;
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 0 0.9rem;
      box-shadow: 0 1px 1px rgba(15, 23, 42, 0.03);
    }
    input {
      width: 100%;
      height: 48px;
      border: 0;
      outline: 0;
      background: transparent;
    }
  `]
})
export class SearchBarComponent {
  @Output() queryChange = new EventEmitter<string>();
  value = '';
}
