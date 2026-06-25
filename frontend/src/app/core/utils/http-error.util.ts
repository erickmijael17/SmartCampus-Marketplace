import { HttpErrorResponse } from '@angular/common/http';

export function describeHttpError(error: unknown, context = 'la solicitud', gatewayDetected = false): string {
  if (!(error instanceof HttpErrorResponse)) {
    return `No se pudo completar ${context}.`;
  }

  if (error.status === 0) {
    if (gatewayDetected) {
      return 'El Gateway esta activo, pero la peticion fue bloqueada por CORS o por una redireccion de seguridad. Revisar configuracion CORS/Security del Gateway.';
    }
    return 'No hay Gateway disponible. Se intento PROD (http://localhost:28082) y DEV (http://localhost:18080). Verifica que al menos uno este levantado y que CORS permita el frontend.';
  }

  if (error.status === 401) {
    return 'El Gateway rechazo la solicitud por falta de autenticacion. Inicia sesion e intenta nuevamente.';
  }

  if (error.status === 403) {
    return 'El Gateway rechazo la solicitud por permisos insuficientes para esta operacion.';
  }

  if (error.status === 404) {
    return 'La ruta solicitada no existe en el Gateway o el microservicio no expone ese endpoint.';
  }

  if (error.status >= 500) {
    return 'El Gateway o un microservicio respondio con error interno. Revisa que Eureka y el servicio destino esten registrados.';
  }

  return `No se pudo completar ${context}. Codigo HTTP: ${error.status}.`;
}

