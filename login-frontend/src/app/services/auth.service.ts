import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

export interface SesionUsuario {
  username: string;
  nombre: string;
  rol: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'jwt_token';
  private readonly SESION_KEY = 'sesion_usuario';
  private readonly LOGOUT_URL = 'http://localhost:8080/api/auth/logout';

  constructor(private http: HttpClient) {}

  guardarSesion(sesion: SesionUsuario, token: string) {
    localStorage.setItem(this.SESION_KEY, JSON.stringify(sesion));
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getSesion(): SesionUsuario | null {
    const data = localStorage.getItem(this.SESION_KEY);
    return data ? JSON.parse(data) : null;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  cerrarSesion() {
    localStorage.removeItem(this.SESION_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
  }

  logout(detalle?: string): Observable<unknown> {
    const params = detalle ? `?detalle=${encodeURIComponent(detalle)}` : '';
    return this.http.post(`${this.LOGOUT_URL}${params}`, {}).pipe(
      catchError(() => of(null)),
      finalize(() => this.cerrarSesion())
    );
  }

  estaAutenticado(): boolean {
    return !!this.getToken();
  }

  esAdmin(): boolean {
    return this.getSesion()?.rol === 'ADMIN';
  }
}
