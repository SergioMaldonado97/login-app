import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  message = '';
  success = false;
  loading = false;

  constructor(private http: HttpClient, private router: Router, private auth: AuthService) {}

  onLogin() {
    this.loading = true;
    this.message = '';
    this.http.post<any>('http://localhost:8080/api/auth/login', {
      username: this.username,
      password: this.password
    }).subscribe({
      next: (res) => {
        this.auth.guardarSesion({ username: res.username, nombre: res.user, rol: res.rol }, res.token);
        this.success = true;
        this.message = `¡Bienvenido, ${res.user}!`;
        this.loading = false;
        const destino = res.rol === 'ADMIN' ? '/usuarios' : '/dashboard';
        setTimeout(() => this.router.navigate([destino]), 800);
      },
      error: (err) => {
        this.success = false;
        this.message = err.error?.message || 'Error al conectar con el servidor';
        this.loading = false;
      }
    });
  }
}
