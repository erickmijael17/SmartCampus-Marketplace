import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Category } from '../../../core/models/product.model';

@Component({
  selector: 'app-category-chip',
  standalone: true,
  template: `
    <button type="button" class="chip" [class.active]="active" (click)="selected.emit(category.label)">
      <span>{{ category.icon }}</span>{{ category.label }}
    </button>
  `,
  styles: [`
    .chip {
      border: 1px solid var(--border);
      background: #fff;
      color: var(--text-muted);
      border-radius: 999px;
      padding: 0.62rem 0.9rem;
      font-weight: 800;
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      white-space: nowrap;
    }
    .chip.active,
    .chip:hover {
      background: var(--primary);
      color: #fff;
      border-color: var(--primary);
    }
  `]
})
export class CategoryChipComponent {
  @Input({ required: true }) category!: Category;
  @Input() active = false;
  @Output() selected = new EventEmitter<string>();
}
