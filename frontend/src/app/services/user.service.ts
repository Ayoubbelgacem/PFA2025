// src/app/services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
  photoProfil: string | null;
  dateInscription: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  /**
   * Récupère l'utilisateur connecté (GET /me)
   */
  getCurrentUser(): Observable<Utilisateur> {
    const token = localStorage.getItem('accessToken'); // 👈 utilise une seule clé
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.get<Utilisateur>(`${this.baseUrl}/me`, { headers });
  }









deleteAccount(): Observable<any> {
  const token = localStorage.getItem('accessToken');
  const headers = new HttpHeaders({
    Authorization: `Bearer ${token}`
  });
  return this.http.delete('http://localhost:8080/api/auth/delete-account', { headers });
}


  /**
   * Met à jour le profil utilisateur (PUT /update-profile)
   */
  updateUser(userData: any): Observable<any> {
    const token = localStorage.getItem('accessToken');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.put(`${this.baseUrl}/update-profile`, userData, { headers });
  }
}
