import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorService } from './api-error.service';

describe('ApiErrorService', () => {
  const service = new ApiErrorService();

  it('prioritizes the first field validation message', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        code: 'VALIDATION_ERROR',
        message: 'Dados inválidos.',
        fieldErrors: { title: 'O título deve ter entre 3 e 160 caracteres.' },
      },
    });

    expect(service.toMessage(error)).toBe('O título deve ter entre 3 e 160 caracteres.');
  });

  it('exposes the business message returned for the RN-001 conflict', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: {
        code: 'HIGH_URGENCY_LIMIT',
        message: 'O técnico já possui dois chamados de urgência Alta abertos.',
      },
    });

    expect(service.toMessage(error)).toBe(
      'O técnico já possui dois chamados de urgência Alta abertos.',
    );
  });

  it('returns a useful message when the backend is unreachable', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(service.toMessage(error)).toBe('Não foi possível conectar com o backend.');
  });
});
