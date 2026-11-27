import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

/** Exige sessão e, enquanto a senha for provisória, leva à troca de senha. */
export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!(await auth.restore())) return router.createUrlTree(['/login']);
  return auth.mustChangePassword() ? router.createUrlTree(['/primeiro-acesso']) : true;
};
export const adminGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return (await auth.restore()) && auth.isAdmin() ? true : router.createUrlTree(['/']);
};
/** Administrador (edição) e gestor (visualização). */
export const managerGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return (await auth.restore()) && auth.canManage() ? true : router.createUrlTree(['/']);
};
/** Tela de primeiro acesso: só faz sentido com sessão e senha provisória. */
export const passwordChangeGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!(await auth.restore())) return router.createUrlTree(['/login']);
  return auth.mustChangePassword() ? true : router.createUrlTree(['/']);
};
