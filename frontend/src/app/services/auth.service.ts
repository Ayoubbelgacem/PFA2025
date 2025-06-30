import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

const API_URL = 'http://localhost:8080/api/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) {}

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post(`${API_URL}/login`, credentials).pipe(
      tap((res: any) => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
      })
    );
  }

  register(data: { nom: string; prenom: string; email: string; password: string; role: string }): Observable<any> {
    return this.http.post(`${API_URL}/register`, data).pipe(
      tap((res: any) => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
      })
    );
  }

  refreshToken(): Observable<any> {
    const refreshToken = localStorage.getItem('refresh_token');
    return this.http.post(`${API_URL}/refresh-token`, { refreshToken }).pipe(
      tap((res: any) => {
        localStorage.setItem('access_token', res.accessToken);
      })
    );
  }

  getAccessToken(): string | null {
    return localStorage.getItem('access_token');
  }

  logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }
  /*forgotPassword(email: string): Observable<any> {
   // return this.http.post(`${API_URL}/forgot-password`, { email });
  }
*/
forgotPassword(email: string): Observable<any> {
  return this.http.post(`http://localhost:8080/api/auth/forgot-password`, null, {
    params: { email }
  });
}

resetPassword(token: string, newPassword: string): Observable<any> {
  return this.http.post(`http://localhost:8080/api/auth/reset-password`, null, {
    params: { token, newPassword }
  });
}

}
