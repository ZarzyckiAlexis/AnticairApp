import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateFn,
  GuardResult,
  MaybeAsync, Router,
  RouterStateSnapshot
} from '@angular/router';
import {AuthService} from '../service/auth.service';

import {inject} from '@angular/core';
import {of} from 'rxjs';

export const isLoginGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);


  if(authService.isLoggedIn().value){
    return of(true);
  }else {
    router.navigate(['/home']);
    return of(false);
  }
}
