import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { FirstAccessComponent } from './first-access.component';

describe('FirstAccessComponent', () => {
  const auth = {
    user: () => ({ name: 'Ana' }),
    changePassword: vi.fn(() => of({ mustChangePassword: false })),
    logout: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    auth.changePassword.mockReturnValue(of({ mustChangePassword: false }));
    await TestBed.configureTestingModule({
      imports: [FirstAccessComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    }).compileComponents();
  });

  function render() {
    const fixture = TestBed.createComponent(FirstAccessComponent);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('validates length, confirmation and reuse before calling the API', () => {
    const component = render();

    component.form.setValue({
      currentPassword: 'Provisoria1234',
      newPassword: 'curta',
      confirmPassword: 'curta',
    });
    component.submit();
    expect(component.error()).toBe('A nova senha precisa ter ao menos 12 caracteres.');

    component.form.setValue({
      currentPassword: 'Provisoria1234',
      newPassword: 'NovaSenhaSegura1',
      confirmPassword: 'OutraCoisa12345',
    });
    component.submit();
    expect(component.error()).toBe('A confirmação não confere com a nova senha.');

    component.form.setValue({
      currentPassword: 'Provisoria1234',
      newPassword: 'Provisoria1234',
      confirmPassword: 'Provisoria1234',
    });
    component.submit();
    expect(component.error()).toBe('A nova senha deve ser diferente da senha provisória.');

    expect(auth.changePassword).not.toHaveBeenCalled();
  });

  it('changes the password and enters the system', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const component = render();
    component.form.setValue({
      currentPassword: 'Provisoria1234',
      newPassword: 'NovaSenhaSegura1',
      confirmPassword: 'NovaSenhaSegura1',
    });

    component.submit();

    expect(auth.changePassword).toHaveBeenCalledWith('Provisoria1234', 'NovaSenhaSegura1');
    expect(navigate).toHaveBeenCalledWith('/');
  });

  it('shows the API message when the temporary password is wrong', () => {
    auth.changePassword.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { code: 'INVALID_CURRENT_PASSWORD', message: 'A senha atual não confere.' },
          }),
      ),
    );
    const component = render();
    component.form.setValue({
      currentPassword: 'Errada12345678',
      newPassword: 'NovaSenhaSegura1',
      confirmPassword: 'NovaSenhaSegura1',
    });

    component.submit();

    expect(component.error()).toBe('A senha atual não confere.');
    expect(component.saving()).toBe(false);
  });
});
