import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, switchMap, catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { jwtDecode } from 'jwt-decode';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getAccessToken();

    // Si le token existe et est valide, on l'ajoute
    if (token && !this.isTokenExpired(token)) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si le token est expiré ou rejeté (401)
        if (error.status === 401 && token) {
          return this.authService.refreshToken().pipe(
            switchMap(res => {
              const newToken = res.accessToken;
              // Rejoue la requête avec le nouveau token
              const newReq = req.clone({
                setHeaders: { Authorization: `Bearer ${newToken}` }
              });
              return next.handle(newReq);
            }),
            catchError(err => {
              // Échec du refresh → déconnexion
              this.authService.logout();
              return throwError(() => err);
            })
          );
        }

        return throwError(() => error);
      })
    );
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded: any = jwtDecode(token);
      // `exp` est en secondes, donc × 1000 pour comparer en ms
      return Date.now() >= decoded.exp * 1000;
    } catch (e) {
      return true;
    }
  }
}
