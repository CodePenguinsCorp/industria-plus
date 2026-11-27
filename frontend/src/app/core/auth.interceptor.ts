import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './services/auth.service';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        request.url.startsWith('/api/') &&
        !request.url.startsWith('/api/auth/')
      ) {
        if (error.status === 401) {
          auth.user.set(null);
          void router.navigateByUrl('/login');
        } else if (error.status === 403 && error.error?.code === 'PASSWORD_CHANGE_REQUIRED') {
          const user = auth.user();
          if (user) auth.user.set({ ...user, mustChangePassword: true });
          void router.navigateByUrl('/primeiro-acesso');
        }
      }
      return throwError(() => error);
    }),
  );
};
