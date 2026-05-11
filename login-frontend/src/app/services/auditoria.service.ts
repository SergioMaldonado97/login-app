import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditoriaEntry, AuditoriaFiltros, PageResponse } from '../models/auditoria.model';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly API = 'http://localhost:8080/api/auditoria';

  constructor(private http: HttpClient) {}

  getAuditoria(
    filtros: AuditoriaFiltros,
    page = 0,
    size = 20
  ): Observable<PageResponse<AuditoriaEntry>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', Math.min(size, 100).toString());

    if (filtros.entidad)              params = params.set('entidad',    filtros.entidad);
    if (filtros.accion)               params = params.set('accion',     filtros.accion);
    if (filtros.username)             params = params.set('username',   filtros.username);
    if (filtros.idRegistro != null)   params = params.set('idRegistro', filtros.idRegistro.toString());
    if (filtros.fechaDesde)           params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta)           params = params.set('fechaHasta', filtros.fechaHasta);

    return this.http.get<PageResponse<AuditoriaEntry>>(this.API, { params });
  }
}
