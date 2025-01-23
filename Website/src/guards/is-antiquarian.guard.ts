import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {AuthService} from '../service/auth.service';
import {of} from 'rxjs';

export const isAntiquarianGuard: CanActivateFn = (route, state) => {


  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.getUserDetails();
  const mail: string = user['groups'];
  if(mail.includes('Antiquarian')){
    return of(true);
  }else {
    router.navigate(['/home']);
    return of(false);
  }
};
