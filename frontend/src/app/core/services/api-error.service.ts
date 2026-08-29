import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApiErrorResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiErrorService {
  toMessage(error: unknown, fallback = 'Não foi possível concluir a operação.'): string {
    if (!(error instanceof HttpErrorResponse)) {
      return fallback;
    }

    if (error.status === 0) {
      return 'Não foi possível conectar com o backend.';
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error.trim();
    }

    const payload = error.error as ApiErrorResponse | null;
    const fieldMessage = this.firstFieldMessage(payload?.fieldErrors);

    if (fieldMessage) {
      return fieldMessage;
    }

    if (payload?.message?.trim()) {
      return payload.message.trim();
    }

    if (payload?.error?.trim()) {
      return payload.error.trim();
    }

    return fallback;
  }

  private firstFieldMessage(fieldErrors: ApiErrorResponse['fieldErrors']): string | null {
    if (!fieldErrors) {
      return null;
    }

    return (
      Object.values(fieldErrors)
        .find((message) => message.trim())
        ?.trim() ?? null
    );
  }
}
