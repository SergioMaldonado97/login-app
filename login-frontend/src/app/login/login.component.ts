import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

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

  constructor(private http: HttpClient, private router: Router) {}

  onLogin() {
    this.loading = true;
    this.message = '';
    this.http.post<any>('http://localhost:8080/api/auth/login', {
      username: this.username,
      password: this.password
    }).subscribe({
      next: (res) => {
        this.success = true;
        this.message = `¡Bienvenido, ${res.user}!`;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/usuarios']), 800);
      },
      error: (err) => {
        this.success = false;
        this.message = err.error?.message || 'Error al conectar con el servidor';
        this.loading = false;
      }
    });
  }
}
