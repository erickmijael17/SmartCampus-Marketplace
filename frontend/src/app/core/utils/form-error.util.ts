import { AbstractControl } from '@angular/forms';

export function getFieldError(control: AbstractControl | null, label: string): string | null {
  if (!control || !control.touched || !control.errors) {
    return null;
  }

  if (control.errors['required']) {
    return `${label} es obligatorio.`;
  }

  if (control.errors['minlength']) {
    const requiredLength = control.errors['minlength'].requiredLength;
    return `${label} debe tener al menos ${requiredLength} caracteres.`;
  }

  if (control.errors['min']) {
    return `${label} debe ser mayor o igual a ${control.errors['min'].min}.`;
  }

  if (control.errors['email']) {
    return `${label} debe ser un correo valido.`;
  }

  if (control.errors['pattern']) {
    return `${label} no tiene un formato valido.`;
  }

  return `${label} no es valido.`;
}
