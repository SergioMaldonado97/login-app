export interface AuditoriaEntry {
  id: number;
  entidad: string;
  idRegistro: number | null;
  accion: string;
  valorAnterior: string | null;
  valorNuevo: string | null;
  username: string;
  fechaHora: string;
  ipOrigen: string;
  detalle: string | null;
}

export interface AuditoriaFiltros {
  entidad?: string;
  accion?: string;
  username?: string;
  idRegistro?: number;
  fechaDesde?: string;
  fechaHasta?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
