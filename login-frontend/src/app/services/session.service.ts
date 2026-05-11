import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class SessionService implements OnDestroy {

  private timeoutMs = 20 * 60 * 1000;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private routerSub: Subscription | null = null;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router
  ) {}

  init() {
    this.http.get<Record<string, string>>('http://localhost:8080/api/config/sesion').subscribe({
      next: (config) => {
        const ms = parseInt(config['SESSION_TIMEOUT_MS'] ?? '1200000', 10);
        this.timeoutMs = isNaN(ms) ? 1200000 : ms;
      },
      error: () => {},
      complete: () => this.iniciarEscucha()
    });

    this.iniciarEscucha();
  }

  private iniciarEscucha() {
    this.routerSub?.unsubscribe();
    this.routerSub = this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => this.reiniciar());
  }

  reiniciar() {
    if (!this.auth.estaAutenticado()) return;
    if (this.timer) clearTimeout(this.timer);
    this.timer = setTimeout(() => this.expirar(), this.timeoutMs);
  }

  detener() {
    if (this.timer) { clearTimeout(this.timer); this.timer = null; }
    this.routerSub?.unsubscribe();
    this.routerSub = null;
  }

  private expirar() {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }

  ngOnDestroy() {
    this.detener();
  }
}
