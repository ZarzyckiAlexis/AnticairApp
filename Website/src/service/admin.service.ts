import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/users/list`);
  }

  forcePasswordReset(rawToken: string, emailid: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/force-password-reset/${emailid}`, null, {
      headers: {
        Authorization: `Bearer ${rawToken}`,
      }
    }).pipe(
      catchError(error => {
        console.error('Error forcing password reset:', error);
        return throwError(() => new Error('Error forcing password reset'));
      })
    );
  }


}
