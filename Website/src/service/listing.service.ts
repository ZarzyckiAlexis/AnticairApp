import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Antiquity } from '../modele/DtoListing';
import { AuthService } from './auth.service';
import { Review } from '../modele/Review';

@Injectable({
  providedIn: 'root'
})
export class ListingService {
  // This is the URL to the API
  privateUrl = 'http://localhost:8080/api/listing';

  constructor(private http: HttpClient, private authService: AuthService) { }

  createListing(
    email: string,
    title: string,
    description: string,
    price: number,
    photos: File[]
  ): Observable<any> {
    // Create a new FormData object
    const formData = new FormData();

    // Append the data to the FormData object
    formData.append('email', email);
    formData.append('title', title);
    formData.append('description', description);
    formData.append('price', price.toString());

    // Append the photos to the FormData object if there are any
    if (photos && photos.length > 0) {
      photos.forEach((photo, index) => {
        formData.append('photos', photo, photo.name);
      });
    }
    // Get the token from the authentication service
    const rawToken = this.authService.getToken();

    // Configure the headers with the token
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${rawToken}`
    });

    // Send the request to API
    return this.http.post(this.privateUrl + '/create', formData, { headers });
  }

  getAntiquityById(id: string):Observable<Antiquity>{
    return this.http.get<Antiquity>(this.privateUrl + '/' + id);
  }

  updateAntiquityWithPhotos(id: number, antiquity: Antiquity, images?: File[]): Observable<any> {
    const formData = new FormData();

    // Convertir l'antiquité en JSON string
    formData.append('antiquity', JSON.stringify(antiquity));

    // Ajouter les images si présentes
    if (images && images.length > 0) {
      images.forEach((file, index) => {
        formData.append('images', file, file.name);
      });
    }

    return this.http.put<any>(`${this.privateUrl}/${id}`, formData);
  }

  getAllAntiquitiesChecked() : Observable<Antiquity[]>{
    return this.http.get<Antiquity[]>(this.privateUrl + '/checked');
  }

  buyAntiquity(id: number): Observable<string> {
    // Get the token from the authentication service
    const rawToken = this.authService.getToken();

    // Configure the headers with the token
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${rawToken}`
    });

    return this.http.post(`${this.privateUrl}/${id}/buy`, null, { headers, responseType: 'text' });
  }

  executePayment(paymentId: string, payerId: string): Observable<any> {
    // Get the token from the authentication service
    const rawToken = this.authService.getToken();

    // Configure the headers with the token
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${rawToken}`
    });

    return this.http.get<any>(`${this.privateUrl}/payment/execute`, { headers, params: { paymentId, PayerID: payerId } });
  }

  acceptAntiquity(antiquity: Antiquity): Observable<Map<string, string>> {
    const body = {
      id: antiquity.idAntiquity
    };
  
    const rawToken = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${rawToken}`,
      'Content-Type': 'application/json'
    });
  
    return this.http.put<Map<string, string>>(`${this.privateUrl}/acceptAntiquity`, body, { headers });
  }


  rejectAntiquity(antiquity: Antiquity, review : Review): Observable<Map<string, string>> {
    const body = {
      id: antiquity.idAntiquity,
      note_title: review.note_title!=="" ? review.note_title : "no comment.",
      note_description: review.note_description!=="" ? review.note_description : "no comment.",
      note_price: review.note_price!=="" ? review.note_price : "no comment.",
      note_photo:review.note_photo!=="" ? review.note_photo : "no comment."
    };

    // Get the token from the authentication service
    const rawToken = this.authService.getToken();

    // Configure the headers with the token
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${rawToken}`
    });

    // Envoi de la requête PUT
    return this.http.put<Map<string, string>>(`${this.privateUrl}/rejectAntiquity`,body,{headers});
  }

  getListingVerify(mailAntiquarian: string):Observable<Antiquity[]>{
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<Antiquity[]>(this.privateUrl + `/by-state?mailAntiquarian=${mailAntiquarian}`, {headers});
  }

  getListingSeller(mailSeller: string):Observable<Antiquity[]>{
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<Antiquity[]>(this.privateUrl + `/byMailSeller?mailSeller=${mailSeller}`, {headers});
  }

  deleteById(id: number): Observable<Antiquity> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<Antiquity>(`${this.privateUrl}/isDisplay/${id}`, null, { headers});
  }
}
