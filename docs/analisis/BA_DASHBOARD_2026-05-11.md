# BA — Dashboard Mejorado CRM
Fecha: 11/05/2026
Estado: PENDIENTE DE DESARROLLO
Sprint: 2

---

## 1. Descripción general

El dashboard actual (`DashboardComponent`) es una pantalla de bienvenida básica que únicamente muestra el nombre, username y rol del usuario autenticado, junto con un mensaje de acceso restringido para usuarios REGULAR. No expone ninguna métrica operativa ni facilita navegación hacia los módulos del sistema.

El objetivo de este Sprint 2 es transformar el dashboard en el **hub central del CRM**, diferenciando la experiencia según el rol del usuario autenticado:

- **ADMIN:** visión global del negocio con métricas agregadas, acceso a los registros de auditoría más recientes y navegación completa a todos los módulos.
- **REGULAR:** visión personal de su cartera de trabajo, mostrando los clientes y contactos que tiene asignados, con acceso únicamente a los módulos habilitados para su rol.

El rediseño debe ser coherente con el stack existente (Angular 21 standalone + Spring Boot 3.4.5 + JWT), reutilizando los mecanismos de autenticación y autorización ya implementados.

---

## 2. Historias de usuario

| ID | Rol | Historia |
|----|-----|----------|
| HU-D01 | ADMIN | Como administrador quiero ver en el dashboard el total de clientes activos del sistema para tener una visión rápida del estado de la cartera. |
| HU-D02 | ADMIN | Como administrador quiero ver el total de contactos registrados para evaluar el alcance de las relaciones comerciales. |
| HU-D03 | ADMIN | Como administrador quiero ver los últimos 5 registros de auditoría para monitorear la actividad reciente del sistema sin entrar al módulo completo. |
| HU-D04 | ADMIN | Como administrador quiero accesos directos a todos los módulos (Usuarios, Clientes, Contactos, Auditoría) desde el dashboard para navegar eficientemente. |
| HU-D05 | REGULAR | Como usuario regular quiero ver cuántos clientes tengo asignados para conocer mi carga de trabajo actual. |
| HU-D06 | REGULAR | Como usuario regular quiero ver mis contactos más recientes para retomar conversaciones sin buscar en el módulo completo. |
| HU-D07 | REGULAR | Como usuario regular quiero accesos directos a los módulos que tengo permitidos (Clientes, Contactos) para navegar rápidamente. |
| HU-D08 | Ambos | Como usuario autenticado quiero que el dashboard se cargue en menos de 2 segundos para no perder tiempo al iniciar mi jornada. |
| HU-D09 | Ambos | Como usuario autenticado quiero ver mi nombre, rol y la fecha/hora actual en el dashboard para confirmar que mi sesión es correcta. |

---

## 3. Criterios de aceptación

### HU-D01 — Total de clientes activos (ADMIN)
- La tarjeta muestra un número entero correspondiente al `COUNT` de registros en `dbo.CRM_CLIENTES` con estado activo.
- Si no hay clientes, muestra `0`.
- El número se actualiza en cada carga del componente (no hay refresco automático en tiempo real en este sprint).

### HU-D02 — Total de contactos (ADMIN)
- La tarjeta muestra el `COUNT` total de `dbo.CRM_CONTACTOS`.
- Si no hay contactos, muestra `0`.

### HU-D03 — Últimos 5 registros de auditoría (ADMIN)
- Se muestra una tabla resumen con columnas: Fecha/Hora, Usuario, Acción, Módulo.
- Los registros provienen de `BRM_CONFIG.AUD_AUDITORIA`, ordenados por fecha descendente, limitados a 5 filas.
- Si no hay registros, la tabla muestra el mensaje "Sin actividad reciente".
- El enlace "Ver todo" navega a la ruta `/auditoria`.

### HU-D04 — Accesos rápidos ADMIN
- Se muestran tarjetas de acceso rápido para: Usuarios (`/usuarios`), Clientes (`/clientes`), Contactos (`/contactos`), Auditoría (`/auditoria`).
- Cada tarjeta incluye un ícono representativo, el nombre del módulo y una descripción breve de una línea.

### HU-D05 — Clientes asignados (REGULAR)
- La tarjeta muestra el `COUNT` de clientes en `dbo.CRM_CLIENTES` donde el campo `usuario_asignado` (o equivalente FK al usuario) coincide con el ID del usuario autenticado.
- Si no tiene clientes asignados, muestra `0`.

### HU-D06 — Contactos recientes (REGULAR)
- Se muestra una lista de los últimos 3 contactos asociados a los clientes del usuario, con: nombre del contacto, empresa/cliente, y fecha de creación.
- Si no hay contactos, muestra el mensaje "No tienes contactos recientes".

### HU-D07 — Accesos rápidos REGULAR
- Se muestran únicamente las tarjetas de módulos habilitados: Clientes (`/clientes`) y Contactos (`/contactos`).
- No se muestra ni el módulo de Usuarios ni el de Auditoría.

### HU-D08 — Rendimiento
- El endpoint `/api/dashboard/resumen` responde en menos de 500 ms bajo carga normal (1 usuario concurrente en entorno de desarrollo).
- El componente Angular muestra un spinner de carga mientras espera la respuesta del backend.

### HU-D09 — Cabecera de sesión
- Se muestra: nombre completo del usuario, badge con el rol (`ADMIN` en azul / `REGULAR` en verde), y la fecha y hora actuales en formato `dd/MM/yyyy HH:mm`.

---

## 4. Reglas de negocio

| ID | Regla |
|----|-------|
| RN-D01 | El endpoint `/api/dashboard/resumen` extrae el rol del JWT para decidir qué datos devolver; nunca confía en parámetros del cliente. |
| RN-D02 | Un usuario REGULAR solo puede ver métricas de los recursos que le pertenecen (clientes asignados, sus contactos); nunca datos agregados globales. |
| RN-D03 | Los accesos rápidos en el frontend se renderizan condicionalmente con `*ngIf` basado en el rol almacenado en `SesionUsuario`; esta es una restricción de UX. La restricción real de seguridad está en el backend. |
| RN-D04 | Si el token JWT ha expirado cuando el dashboard intenta cargar las métricas, el interceptor redirige al login (comportamiento heredado del `AuthInterceptor` existente). |
| RN-D05 | Los números de las tarjetas de métricas son de solo lectura; no son enlaces ni abren modales. |
| RN-D06 | La tabla de auditoría en el dashboard es un resumen de solo lectura; el detalle y la exportación permanecen en el módulo `/auditoria`. |
| RN-D07 | Un cliente se considera "activo" cuando su campo de estado en `dbo.CRM_CLIENTES` tiene el valor definido como activo (a confirmar con el diseño del módulo Clientes en el mismo Sprint 2). |
| RN-D08 | El dashboard no realiza ninguna operación de escritura; es una pantalla 100 % de lectura. |

---

## 5. Métricas y componentes visuales

### Vista ADMIN

```
┌────────────────────────────────────────────────────────────────┐
│  CRM Dashboard           Hola, [Nombre]   [ADMIN]  10/05/2026  │
├──────────────┬───────────────────┬──────────────────────────────┤
│  TARJETA 1   │    TARJETA 2      │         TARJETA 3            │
│  Clientes    │    Contactos      │      Usuarios registrados    │
│  activos     │    totales        │        (de dbo.usuarios)     │
│    [N]       │       [N]         │             [N]              │
├──────────────┴───────────────────┴──────────────────────────────┤
│  ÚLTIMOS REGISTROS DE AUDITORÍA                    [Ver todo →] │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Fecha/Hora        │ Usuario   │ Acción    │ Módulo       │   │
│  │ 11/05/2026 09:14  │ admin     │ LOGIN     │ AUTH         │   │
│  │ 11/05/2026 08:52  │ jperez    │ CREATE    │ CLIENTES     │   │
│  │ ...               │ ...       │ ...       │ ...          │   │
│  └─────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│  ACCESOS RÁPIDOS                                                 │
│  [Usuarios]   [Clientes]   [Contactos]   [Auditoría]            │
└─────────────────────────────────────────────────────────────────┘
```

**Tarjetas de métricas ADMIN:**

| Tarjeta | Fuente de datos | Ícono sugerido |
|---------|-----------------|----------------|
| Clientes activos | `SELECT COUNT(*) FROM dbo.CRM_CLIENTES WHERE estado = 'ACTIVO'` | building / briefcase |
| Contactos totales | `SELECT COUNT(*) FROM dbo.CRM_CONTACTOS` | person-lines-fill |
| Usuarios registrados | `SELECT COUNT(*) FROM dbo.usuarios` | people-fill |

**Tabla de auditoría resumida:**

| Columna | Campo fuente | Formato |
|---------|-------------|---------|
| Fecha/Hora | `AUD_FECHA` | `dd/MM/yyyy HH:mm` |
| Usuario | `AUD_USUARIO` | texto plano |
| Acción | `AUD_ACCION` | badge de color (LOGIN=azul, CREATE=verde, UPDATE=naranja, DELETE=rojo) |
| Módulo | `AUD_MODULO` | texto plano |

**Tarjetas de acceso rápido ADMIN:**

| Módulo | Ruta | Descripción breve |
|--------|------|-------------------|
| Usuarios | `/usuarios` | Gestión de cuentas y roles |
| Clientes | `/clientes` | Cartera de clientes CRM |
| Contactos | `/contactos` | Directorio de contactos |
| Auditoría | `/auditoria` | Registro de actividad del sistema |

---

### Vista REGULAR

```
┌────────────────────────────────────────────────────────────────┐
│  CRM Dashboard        Hola, [Nombre]   [REGULAR]  10/05/2026   │
├───────────────────────────┬────────────────────────────────────┤
│  TARJETA 1                │         TARJETA 2                  │
│  Mis clientes asignados   │   Mis contactos recientes          │
│          [N]              │           [N]                      │
├───────────────────────────┴────────────────────────────────────┤
│  MIS CONTACTOS RECIENTES                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Nombre            │ Cliente       │ Fecha creación       │   │
│  │ Juan García       │ Empresa ABC   │ 10/05/2026           │   │
│  │ María López       │ Empresa XYZ   │ 09/05/2026           │   │
│  │ ...               │ ...           │ ...                  │   │
│  └─────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│  ACCESOS RÁPIDOS                                                 │
│  [Clientes]   [Contactos]                                       │
└─────────────────────────────────────────────────────────────────┘
```

**Tarjetas de métricas REGULAR:**

| Tarjeta | Fuente de datos | Ícono sugerido |
|---------|-----------------|----------------|
| Mis clientes asignados | `SELECT COUNT(*) FROM dbo.CRM_CLIENTES WHERE usuario_asignado = :userId AND estado = 'ACTIVO'` | briefcase |
| Mis contactos recientes | `SELECT COUNT(*) FROM dbo.CRM_CONTACTOS WHERE cliente_id IN (SELECT id FROM dbo.CRM_CLIENTES WHERE usuario_asignado = :userId)` | person-lines-fill |

**Lista de contactos recientes REGULAR:**

| Columna | Campo fuente | Formato |
|---------|-------------|---------|
| Nombre | `CRM_CONTACTOS.nombre` + `CRM_CONTACTOS.apellido` | texto plano |
| Cliente | `CRM_CLIENTES.nombre` (JOIN) | texto plano |
| Fecha creación | `CRM_CONTACTOS.fecha_creacion` | `dd/MM/yyyy` |

**Tarjetas de acceso rápido REGULAR:**

| Módulo | Ruta | Descripción breve |
|--------|------|-------------------|
| Clientes | `/clientes` | Ver mis clientes asignados |
| Contactos | `/contactos` | Ver mis contactos |

---

## 6. Impacto en el sistema

### Backend (endpoints de resumen/métricas)

Se requiere crear un nuevo controller dedicado al dashboard:

**Nuevo endpoint:**

```
GET /api/dashboard/resumen
Authorization: Bearer <token>
```

**Lógica del controller:**
- Extraer el rol y el `userId` del JWT en el filtro existente (`JwtFilter`).
- Si el rol es `ADMIN`: ejecutar las tres consultas de conteo global + consulta de últimos 5 registros de auditoría.
- Si el rol es `REGULAR`: ejecutar las dos consultas de conteo filtradas por `userId` + consulta de últimos 3 contactos del usuario.
- Retornar un `DashboardResumenDTO` serializado como JSON.

**Clases nuevas a crear:**

| Clase | Paquete sugerido | Tipo |
|-------|-----------------|------|
| `DashboardController` | `com.sergio.login` | `@RestController` |
| `DashboardService` | `com.sergio.login` | `@Service` |
| `DashboardResumenDTO` | `com.sergio.login` | Record / POJO |
| `AuditoriaResumenDTO` | `com.sergio.login` | Record / POJO |
| `ContactoResumenDTO` | `com.sergio.login` | Record / POJO |
| `DashboardRepository` | `com.sergio.login` | `@Repository` con `@Query` nativos |

**Estructura del DTO de respuesta:**

```json
{
  "rol": "ADMIN",
  "nombreUsuario": "Sergio Maldonado",
  "totalClientes": 42,
  "totalContactos": 128,
  "totalUsuarios": 5,
  "ultimaAuditoria": [
    {
      "fecha": "2026-05-11T09:14:00",
      "usuario": "admin",
      "accion": "LOGIN",
      "modulo": "AUTH"
    }
  ],
  "contactosRecientes": []
}
```

Para REGULAR, `totalUsuarios` y `ultimaAuditoria` serán `null` o ausentes del DTO; `contactosRecientes` contendrá la lista de hasta 3 contactos.

**Registro en auditoría:** El acceso al dashboard NO se auditará para evitar saturar la tabla `AUD_AUDITORIA` con entradas de solo lectura.

---

### Frontend (rediseño del componente)

**Archivos a modificar:**

| Archivo | Tipo de cambio |
|---------|---------------|
| `dashboard.component.ts` | Reescritura completa: añadir `DashboardService`, `HttpClient`, variables de estado, lógica condicional por rol. |
| `dashboard.component.html` | Reescritura completa: nueva estructura con tarjetas de métricas, tabla de auditoría/contactos y accesos rápidos. |
| `dashboard.component.css` | Extensión: estilos para tarjetas de métricas, tabla resumen, badges de acción, spinner de carga. |

**Archivos a crear:**

| Archivo | Descripción |
|---------|-------------|
| `services/dashboard.service.ts` | Servicio Angular que consume `GET /api/dashboard/resumen` con el `AuthInterceptor` existente. |

**Estructura del componente TypeScript:**

```typescript
export class DashboardComponent implements OnInit {
  sesion: SesionUsuario | null = null;
  resumen: DashboardResumen | null = null;
  cargando = true;
  error: string | null = null;

  get esAdmin(): boolean {
    return this.sesion?.rol === 'ADMIN';
  }
}
```

**Interfaz TypeScript a definir:**

```typescript
export interface DashboardResumen {
  rol: string;
  nombreUsuario: string;
  totalClientes: number;
  totalContactos: number;
  totalUsuarios?: number;
  ultimaAuditoria?: AuditoriaResumen[];
  contactosRecientes?: ContactoResumen[];
}
```

---

### Seguridad

| Punto | Detalle |
|-------|---------|
| Autorización en backend | El endpoint `/api/dashboard/resumen` requiere JWT válido. Se añade a la lista de rutas protegidas en `SecurityConfig.java`. |
| Filtrado por userId | El `userId` para filtrar datos de REGULAR se obtiene SIEMPRE del token JWT, nunca de un parámetro de query o body. |
| No exposición cruzada | Un usuario REGULAR no puede obtener métricas globales aunque manipule el request; el backend valida el rol antes de ejecutar las queries. |
| Sin nuevas rutas públicas | El endpoint de dashboard no se añade a las rutas públicas de `SecurityConfig`. |
| CORS | No se requieren cambios; el CORS ya está configurado para `http://localhost:4200`. |

---

## 7. Flujo principal

```
1. Usuario autenticado navega a /dashboard (o es redirigido tras login).
2. DashboardComponent.ngOnInit() se ejecuta:
   a. Recupera la sesión del AuthService (localStorage / JWT decode).
   b. Llama a DashboardService.obtenerResumen().
   c. Muestra spinner de carga mientras espera la respuesta.
3. DashboardService envía GET /api/dashboard/resumen con Bearer token.
4. AuthInterceptor agrega automáticamente el header Authorization.
5. JwtFilter valida el token y coloca el usuario en el SecurityContext.
6. DashboardController recibe la request:
   a. Extrae el rol y userId del SecurityContext.
   b. Llama a DashboardService (backend) con el rol y userId.
   c. DashboardService ejecuta las queries correspondientes.
   d. Retorna el DashboardResumenDTO serializado.
7. DashboardComponent recibe la respuesta:
   a. Oculta el spinner.
   b. Rellena las variables de estado (resumen).
   c. El template renderiza condicionalmente según el rol.
8. Si el token ha expirado (401):
   a. AuthInterceptor redirige al login.
   b. SessionService limpia el localStorage.
9. Si hay error de servidor (500):
   a. Se muestra un mensaje de error en pantalla.
   b. Las tarjetas muestran "--" en lugar de números.
```

---

## 8. Dependencias

Este módulo depende de los siguientes elementos que se desarrollan en el mismo Sprint 2:

| Dependencia | Motivo | Estado |
|-------------|--------|--------|
| Módulo Clientes (`dbo.CRM_CLIENTES`) | Las tarjetas de métricas de clientes y las queries de conteo requieren que la tabla y su entidad JPA existan. | Pendiente de desarrollo (Sprint 2) |
| Módulo Contactos (`dbo.CRM_CONTACTOS`) | Las tarjetas de métricas de contactos y la lista de contactos recientes dependen de esta tabla. | Pendiente de desarrollo (Sprint 2) |
| Módulo Auditoría (`BRM_CONFIG.AUD_AUDITORIA`) | La tabla ya existe. El BA de Auditoría (`BA_AUDITORIA_2026-05-11.md`) define su estructura y el endpoint de lectura. | Tabla existente — endpoint pendiente |
| Campo `usuario_asignado` en `CRM_CLIENTES` | Necesario para filtrar los clientes del usuario REGULAR. El diseño de este campo debe quedar acordado en el BA del módulo Clientes. | Pendiente de definición |

**Orden de implementación recomendado dentro del Sprint 2:**
1. Módulo Clientes (tabla + entidad + CRUD básico).
2. Módulo Contactos (tabla + entidad + CRUD básico).
3. Dashboard mejorado (depende de los dos anteriores).
4. Módulo Auditoría completo (puede ir en paralelo con el dashboard).

---

## 9. Estimación de complejidad

| Componente | Complejidad | Justificación |
|------------|-------------|---------------|
| Backend — `DashboardController` | Baja | Un único endpoint GET, sin escritura. |
| Backend — `DashboardService` | Media | Lógica condicional por rol, 4-5 queries distintas. |
| Backend — `DashboardRepository` | Media | Queries nativas con JOIN entre tablas de schemas distintos (`dbo` y `BRM_CONFIG`). |
| Frontend — `DashboardComponent` rediseño | Media | Renderizado condicional por rol, manejo de estado de carga y error. |
| Frontend — `DashboardService` | Baja | Una sola llamada HTTP GET. |
| CSS / diseño de tarjetas | Baja-Media | Nuevo layout de tarjetas; sin dependencias de librerías externas. |
| **Total estimado** | **Media** | **~8-12 horas de desarrollo + pruebas.** |

**Puntos de historia sugeridos (Planning Poker):** 5

---

## 10. Instrucciones para el desarrollador

### Prerrequisitos
- Las tablas `dbo.CRM_CLIENTES` y `dbo.CRM_CONTACTOS` deben existir en `logindb` antes de iniciar el desarrollo del dashboard.
- Verificar que `BRM_CONFIG.AUD_AUDITORIA` tiene datos de prueba para validar la tabla resumen.

### Pasos de implementación

**1. Backend — Crear el DTO de respuesta**
```java
// DashboardResumenDTO.java
public record DashboardResumenDTO(
    String rol,
    String nombreUsuario,
    long totalClientes,
    long totalContactos,
    Long totalUsuarios,                         // null para REGULAR
    List<AuditoriaResumenDTO> ultimaAuditoria,  // null para REGULAR
    List<ContactoResumenDTO> contactosRecientes // null para ADMIN
) {}
```

**2. Backend — Crear el Repository con queries nativas**
- Anotar la interfaz con `@Repository`.
- Usar `@Query(value = "...", nativeQuery = true)` para las consultas a `BRM_CONFIG.AUD_AUDITORIA` (schema diferente al default).
- Pasar el `userId` como `@Param("userId")`.

**3. Backend — Crear el Service**
- Inyectar el repository y la lógica de rol.
- Método principal: `DashboardResumenDTO obtenerResumen(String username, String rol)`.

**4. Backend — Crear el Controller**
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumenDTO> resumen(Principal principal) { ... }
}
```
- Usar `Principal principal` (inyectado por Spring Security) para obtener el username.
- Recuperar el rol del `UserDetails` o del `Authentication` del `SecurityContext`.

**5. Backend — Registrar la ruta en SecurityConfig**
```java
.requestMatchers("/api/dashboard/**").authenticated()
```
Asegurarse de que esta línea aparece en el bloque de rutas protegidas, NO en las públicas.

**6. Frontend — Crear `dashboard.service.ts`**
```typescript
@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  obtenerResumen(): Observable<DashboardResumen> {
    return this.http.get<DashboardResumen>('/api/dashboard/resumen');
  }
}
```

**7. Frontend — Reescribir `dashboard.component.ts`**
- Inyectar `DashboardService` y `AuthService`.
- En `ngOnInit`: llamar a `obtenerResumen()`, gestionar `cargando` y `error`.
- Propiedad computada `get esAdmin()` para simplificar el template.

**8. Frontend — Reescribir `dashboard.component.html`**
- Sección 1: cabecera con nombre, rol y fecha actual.
- Sección 2: grid de tarjetas de métricas (condicional por rol).
- Sección 3: tabla resumen (auditoría para ADMIN, contactos recientes para REGULAR).
- Sección 4: grid de accesos rápidos (condicional por rol).
- Usar `@if` (nueva sintaxis Angular 17+) en lugar de `*ngIf` dado que el proyecto usa Angular 21.

**9. Pruebas mínimas requeridas**
- [ ] Login como ADMIN: verificar que las 3 tarjetas muestran números, la tabla muestra hasta 5 registros de auditoría y se ven los 4 accesos rápidos.
- [ ] Login como REGULAR: verificar que solo se ven las 2 tarjetas propias, la lista de contactos recientes y los 2 accesos rápidos.
- [ ] Verificar que un REGULAR no puede llamar directamente a `/api/dashboard/resumen` y obtener datos globales (probar con Postman usando el token de un REGULAR).
- [ ] Verificar comportamiento con tablas vacías (0 clientes, 0 contactos, 0 registros de auditoría).
- [ ] Verificar redirección al login si el token ha expirado al cargar el dashboard.

### Notas adicionales
- No modificar `auth.service.ts` ni `auth.interceptor.ts`; deben funcionar sin cambios para este módulo.
- El campo que relaciona clientes con usuarios en `dbo.CRM_CLIENTES` debe consensuarse con el desarrollador del módulo Clientes antes de escribir las queries del dashboard. Usar `usuario_asignado` como nombre provisional.
- Si durante el desarrollo se detecta que las queries son lentas, añadir los índices necesarios sobre `usuario_asignado` y `fecha_creacion` y documentarlo en el script de migración de base de datos.
