import { HttpErrorResponse } from '@angular/common/http';

export function describeHttpError(error: unknown, context = 'la solicitud', gatewayDetected = false): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

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

  if (error.status === 409) {
    return extractBackendMessage(error) ?? 'Ya existe un registro con esos datos. Verifica el correo o usuario e intenta nuevamente.';
  }

  if (error.status === 404) {
    return 'La ruta solicitada no existe en el Gateway o el microservicio no expone ese endpoint.';
  }

  if (error.status >= 500) {
    return 'El Gateway o un microservicio respondio con error interno. Revisa que Eureka y el servicio destino esten registrados.';
  }

  return `No se pudo completar ${context}. Codigo HTTP: ${error.status}.`;
}

function extractBackendMessage(error: HttpErrorResponse): string | null {
  const body = error.error;

  if (typeof body === 'string' && body.trim()) {
    return body;
  }

  if (body && typeof body === 'object') {
    const message = body.message ?? body.errorMessage ?? body.detail ?? body.error;
    return typeof message === 'string' && message.trim() ? message : null;
  }

  return null;
}
