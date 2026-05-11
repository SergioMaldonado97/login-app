import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AuditoriaService } from '../services/auditoria.service';
import { AuditoriaEntry, AuditoriaFiltros, PageResponse } from '../models/auditoria.model';

interface EntradaJson {
  clave: string;
  valor: unknown;
}

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './auditoria.component.html',
  styleUrl: './auditoria.component.css'
})
export class AuditoriaComponent implements OnInit {

  registros: AuditoriaEntry[] = [];
  loading = false;
  error = '';

  // Paginación
  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;

  // Filtros
  filtroEntidad = '';
  filtroAccion = '';
  filtroUsername = '';
  filtroFechaDesde = '';
  filtroFechaHasta = '';

  // Fila expandida para comparativa JSON
  expandedId: number | null = null;

  entidades = ['', 'SESION', 'USUARIO', 'CLIENTE', 'CONTACTO', 'OPORTUNIDAD', 'ACTIVIDAD', 'PRODUCTO', 'OFERTA'];
  acciones  = ['', 'CREAR', 'MODIFICAR', 'ELIMINAR', 'CAMBIO_ESTADO', 'LOGIN_EXITOSO', 'LOGIN_FALLIDO', 'LOGOUT'];

  constructor(
    private auditoriaService: AuditoriaService,
    private auth: AuthService,
    public router: Router
  ) {}

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.loading = true;
    this.error = '';

    const filtros: AuditoriaFiltros = {};
    if (this.filtroEntidad)    filtros.entidad    = this.filtroEntidad;
    if (this.filtroAccion)     filtros.accion     = this.filtroAccion;
    if (this.filtroUsername)   filtros.username   = this.filtroUsername;
    if (this.filtroFechaDesde) filtros.fechaDesde = this.filtroFechaDesde;
    if (this.filtroFechaHasta) filtros.fechaHasta = this.filtroFechaHasta;

    this.auditoriaService.getAuditoria(filtros, this.page, this.size).subscribe({
      next: (res: PageResponse<AuditoriaEntry>) => {
        this.registros = res.content;
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los registros de auditoría';
        this.loading = false;
      }
    });
  }

  buscar() {
    this.page = 0;
    this.cargar();
  }

  limpiarFiltros() {
    this.filtroEntidad = '';
    this.filtroAccion = '';
    this.filtroUsername = '';
    this.filtroFechaDesde = '';
    this.filtroFechaHasta = '';
    this.page = 0;
    this.cargar();
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.cargar();
    }
  }

  paginaSiguiente() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.cargar();
    }
  }

  toggleExpand(id: number) {
    this.expandedId = this.expandedId === id ? null : id;
  }

  parsearJson(valor: string | null): Record<string, unknown> | null {
    if (!valor) return null;
    try {
      return JSON.parse(valor);
    } catch {
      return null;
    }
  }

  getEntradas(valor: string | null): EntradaJson[] {
    const obj = this.parsearJson(valor);
    if (!obj) return [];
    return Object.keys(obj).map(k => ({ clave: k, valor: obj[k] }));
  }

  esCambiado(r: AuditoriaEntry, campo: string): boolean {
    const a = this.parsearJson(r.valorAnterior);
    const n = this.parsearJson(r.valorNuevo);
    if (!a || !n) return false;
    return JSON.stringify(a[campo]) !== JSON.stringify(n[campo]);
  }

  formatearFecha(fechaHora: string): string {
    if (!fechaHora) return '';
    return new Date(fechaHora).toLocaleString();
  }

  cerrarSesion() {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }

  get paginasArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  irAPagina(p: number) {
    this.page = p;
    this.cargar();
  }
}
