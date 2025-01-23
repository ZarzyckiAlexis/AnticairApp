import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class GroupService {

  private apiUrl = 'http://localhost:8080/api/groups';


  constructor(private http: HttpClient, private authService: AuthService, private router : Router) { }

  // Method to add a group to a user
  addGroupToUser(emailId: string, groupName: string): Observable<any> {
    return new Observable((observer) => {
      // Retrieve the token
      this.authService.getToken().then((token: string) => {
        // Check if the token exists
        if (!token) {
          observer.error('No token found');
          this.router.navigate(['/']);
          alert("You are not connected");
          return;
        }
        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`,
        });

        // Set query parameters
        const params = new HttpParams()
          .set('emailId', emailId)
          .set('groupName', groupName);

        // Send POST request with params and headers
        this.http.post(`${this.apiUrl}/add`, null, { params, headers })
          .subscribe({
            next: (response) => {
              observer.next(response);  // Notify the success
              observer.complete();  // Complete the observable stream
            },
            error: (error) => {
              observer.error(error);  // Notify the error
            }
          });

      }).catch((error) => {
        observer.error(error);  // Notify the error in case of failure in token retrieval
      });
    });
  }

  // Method to remove a group from a user
  removeGroupFromUser(emailId: string, groupName: string): Observable<any> {
    return new Observable((observer) => {
      // Retrieve the token
      this.authService.getToken().then((token: string) => {
        // Check if the token exists
        if (!token) {
          observer.error('No token found');
          this.router.navigate(['/']);
          alert("You are not connected");
          return;
        }
        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`,
        });

        // Set query parameters
        const params = new HttpParams()
          .set('emailId', emailId)
          .set('groupName', groupName);

        // Send POST request with params and headers
        this.http.post(`${this.apiUrl}/remove`, null, { params, headers })
          .subscribe({
            next: (response) => {
              observer.next(response);  // Notify the success
              observer.complete();  // Complete the observable stream
            },
            error: (error) => {
              observer.error(error);  // Notify the error
            }
          });

      }).catch((error) => {
        observer.error(error);  // Notify the error in case of failure in token retrieval
      });
    });
  }

  // Method to get groups from the keycloak realm
  getGroups(): Observable<any> {
    return new Observable((observer) => {
      // Retrieve the token
      this.authService.getToken().then((token: string) => {
        // Check if the token exists
        if (!token) {
          observer.error('No token found');
          this.router.navigate(['/']);
          alert("You are not connected");
          return;
        }
        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`,
        });

        // Send GET request with headers
        this.http.get(`${this.apiUrl}/`, { headers })
          .subscribe({
            next: (response) => {
              observer.next(response);  // Notify the success
              observer.complete();  // Complete the observable stream
            },
            error: (error) => {
              observer.error(error);  // Notify the error
            }
          });

      }).catch((error) => {
        observer.error(error);  // Notify the error in case of failure in token retrieval
      });
    });
  }

  // Method to get groups from the keycloak realm for a specific user
  getGroupsFromUser(emailId: string): Observable<any> {
    return new Observable((observer) => {
      // Retrieve the token
      this.authService.getToken().then((token: string) => {
        // Check if the token exists
        if (!token) {
          observer.error('No token found');
          // Redirect to home page
          this.router.navigate(['/']);
          alert("You are not connected");
          return;
        }
        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`,
        });

        const params = new HttpParams()
          .set('emailId', emailId)

        // Send GET request with the emailId parameter and headers
        this.http.get(`${this.apiUrl}/${emailId}/groups`, { params, headers })
          .subscribe({
            next: (response) => {
              observer.next(response);  // Notify the success
              observer.complete();  // Complete the observable stream
            },
            error: (error) => {
              observer.error(error);  // Notify the error
            }
          });

      }).catch((error) => {
        observer.error(error);  // Notify the error in case of failure in token retrieval
      });
    });
  }


}
