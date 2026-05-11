import { Injectable } from '@angular/core';

export interface SesionUsuario {
  username: string;
  nombre: string;
  rol: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'jwt_token';
  private readonly SESION_KEY = 'sesion_usuario';

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

  estaAutenticado(): boolean {
    return !!this.getToken();
  }

  esAdmin(): boolean {
    return this.getSesion()?.rol === 'ADMIN';
  }
}
