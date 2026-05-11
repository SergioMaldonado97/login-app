import { Injectable } from '@angular/core';

export interface SesionUsuario {
  username: string;
  nombre: string;
  rol: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly KEY = 'sesion_usuario';

  guardarSesion(sesion: SesionUsuario) {
    localStorage.setItem(this.KEY, JSON.stringify(sesion));
  }

  getSesion(): SesionUsuario | null {
    const data = localStorage.getItem(this.KEY);
    return data ? JSON.parse(data) : null;
  }

  cerrarSesion() {
    localStorage.removeItem(this.KEY);
  }

  estaAutenticado(): boolean {
    return !!this.getSesion();
  }

  esAdmin(): boolean {
    return this.getSesion()?.rol === 'ADMIN';
  }
}
