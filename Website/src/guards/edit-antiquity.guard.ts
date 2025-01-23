import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanActivateFn, GuardResult, MaybeAsync, Router, RouterStateSnapshot } from '@angular/router';
import { ListingService } from '../service/listing.service';
import { catchError, Observable, of, switchMap } from 'rxjs';
import { AuthService } from '../service/auth.service';


@Injectable({
  providedIn: 'root'
})
export class editAntiquityGuard implements CanActivate{

  
  constructor(private authservice: AuthService, private antiquityservice: ListingService,private route: Router){}

  
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean>{
    const idAntiquity = route.paramMap.get("id");
    if(!idAntiquity){
      return of(false);
    }
    if(this.authservice.isAdmin()){
      return of(true);
    }
    
    const user = this.authservice.getUserDetails();
    const mail = user.email;

    return this.antiquityservice.getAntiquityById(idAntiquity).pipe(
      switchMap((antiquity) => {
        if (antiquity.mailSeller == mail) {
          return of(true);  // Access granted
        } else {
          this.route.navigate(['/']);
          return of(false);  // Access denied
        }
      }),
      catchError((err) => {
        console.error('Antiquity not found or error', err);
        this.route.navigate(['/']);
        return of(false);
      })
    );
    
  
  }
    
  }

