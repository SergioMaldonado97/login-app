import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface Usuario {
  id: number;
  username: string;
  nombre: string;
}

interface UsuarioForm {
  username: string;
  password: string;
  nombre: string;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  private api = 'http://localhost:8080/api/usuarios';

  usuarios: Usuario[] = [];
  loading = false;
  error = '';

  showModal = false;
  editMode = false;
  editId: number | null = null;
  form: UsuarioForm = { username: '', password: '', nombre: '' };
  formError = '';
  formLoading = false;

  confirmDelete: number | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.loading = true;
    this.http.get<Usuario[]>(this.api).subscribe({
      next: (data) => { this.usuarios = data; this.loading = false; },
      error: () => { this.error = 'Error al cargar usuarios'; this.loading = false; }
    });
  }

  abrirCrear() {
    this.editMode = false;
    this.editId = null;
    this.form = { username: '', password: '', nombre: '' };
    this.formError = '';
    this.showModal = true;
  }

  abrirEditar(u: Usuario) {
    this.editMode = true;
    this.editId = u.id;
    this.form = { username: u.username, password: '', nombre: u.nombre };
    this.formError = '';
    this.showModal = true;
  }

  cerrarModal() {
    this.showModal = false;
    this.formError = '';
  }

  guardar() {
    this.formLoading = true;
    this.formError = '';

    if (this.editMode && this.editId !== null) {
      this.http.put<Usuario>(`${this.api}/${this.editId}`, this.form).subscribe({
        next: () => { this.formLoading = false; this.cerrarModal(); this.cargar(); },
        error: (err) => { this.formError = err.error?.message || 'Error al actualizar'; this.formLoading = false; }
      });
    } else {
      this.http.post<Usuario>(this.api, this.form).subscribe({
        next: () => { this.formLoading = false; this.cerrarModal(); this.cargar(); },
        error: (err) => { this.formError = err.error?.message || 'Error al crear'; this.formLoading = false; }
      });
    }
  }

  pedirConfirmacion(id: number) {
    this.confirmDelete = id;
  }

  cancelarEliminar() {
    this.confirmDelete = null;
  }

  eliminar(id: number) {
    this.http.delete(`${this.api}/${id}`).subscribe({
      next: () => { this.confirmDelete = null; this.cargar(); },
      error: () => { this.error = 'Error al eliminar'; this.confirmDelete = null; }
    });
  }

  cerrarSesion() {
    this.router.navigate(['/login']);
  }
}
