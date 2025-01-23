import {inject, Injectable} from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router, CanActivateFn
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { AuthService } from '../service/auth.service';


export const AdminGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);

  if(authService.isLoggedIn().value && authService.isAdmin()){
    return of(true);
  }else {
    router.navigate(['/home']);
    return of(false);
  }







  }

