# BA — Módulo de Auditoría CRM
Fecha: 11/05/2026
Estado: PENDIENTE DE DESARROLLO
Sprint: 1

---

## 1. Descripción general

En un CRM, la trazabilidad de las operaciones sobre los datos de negocio es un requisito crítico. Los equipos de ventas, soporte y dirección necesitan saber en todo momento **quién modificó el registro de un cliente, cuándo se cambió el estado de una oportunidad, o qué usuario eliminó un contacto**. Esta capacidad no solo responde a exigencias regulatorias o de auditoría interna, sino que es la base para resolver disputas comerciales, detectar uso indebido de los datos de clientes y mantener la integridad de la información de negocio.

El módulo de Auditoría CRM registra de forma automática, transparente e inmutable cada acción que los usuarios realizan sobre las entidades del CRM — Clientes, Contactos, Oportunidades, Actividades, Usuarios, etc. — respondiendo siempre a las preguntas: **¿quién hizo qué, sobre qué registro, cuándo, desde dónde y qué cambió exactamente?**

El módulo se diseña con visión de crecimiento: aunque en esta primera iteración la única entidad auditable es **Usuario** (entidad piloto ya existente en el sistema), la arquitectura debe soportar incorporar sin refactorización las entidades de negocio CRM que se desarrollen en sprints posteriores (Clientes, Contactos, Oportunidades, etc.).

El registro es **pasivo y no bloqueante**: nunca interrumpe el flujo principal de negocio. Si la escritura de un registro de auditoría falla, el error se logea en el sistema pero la operación original sobre el registro CRM se completa con normalidad.

Solo los usuarios con rol **ADMIN** pueden consultar el historial de auditoría. Ningún usuario REGULAR puede acceder a esta información, ni a través de la API ni desde la interfaz Angular.

---

## 2. Historias de usuario

**HU-AUD-01 — Trazabilidad de modificaciones sobre registros CRM**
> Como **director comercial**, quiero saber quién modificó los datos de un cliente y cuándo ocurrió el cambio, incluyendo el valor anterior y el nuevo, para poder resolver discrepancias comerciales y mantener la integridad del historial de negocio.

**HU-AUD-02 — Actividad de un usuario en el CRM**
> Como **administrador del CRM**, quiero consultar todas las acciones que realizó un usuario específico durante un rango de fechas (hoy, esta semana, este mes), para supervisar el uso del sistema, detectar comportamientos anómalos o verificar la productividad del equipo.

**HU-AUD-03 — Historial de cambios de un registro**
> Como **responsable de cuentas**, quiero ver el historial completo de modificaciones de un registro concreto (por ejemplo, el cliente con ID 450), con comparativa entre el estado anterior y el estado actual para cada cambio, de modo que pueda entender la evolución del registro a lo largo del tiempo.

**HU-AUD-04 — Detección de eliminaciones de datos de negocio**
> Como **administrador del CRM**, quiero recibir trazabilidad de cada eliminación de un registro (cliente, contacto, oportunidad), incluyendo quién lo eliminó y cuándo, para poder restaurar datos en caso de borrado accidental o no autorizado y para demostrar que el borrado fue intencionado.

**HU-AUD-05 — Consulta de auditoría con filtros avanzados**
> Como **ADMIN**, quiero filtrar el historial de auditoría por entidad (ej. Cliente), acción (ej. MODIFICAR), usuario y rango de fechas, con resultados paginados y ordenados por fecha descendente, para localizar rápidamente un evento específico sin necesidad de revisar miles de registros.

**HU-AUD-06 — Trazabilidad de gestión de usuarios del CRM**
> Como **ADMIN**, quiero que todas las altas, modificaciones y bajas de usuarios del CRM queden registradas automáticamente — incluyendo los cambios de rol y contraseña — para garantizar control total sobre quién tiene acceso al sistema y con qué permisos.

**HU-AUD-07 — Comparativa de un campo entre dos momentos**
> Como **jefe de ventas**, quiero poder ver qué valor tenía el campo "estado de oportunidad" ayer y qué valor tiene hoy, para entender si el pipeline comercial ha avanzado o si alguien modificó una oportunidad sin comunicarlo al equipo.

---

## 3. Criterios de aceptación

### HU-AUD-01 — Trazabilidad de modificaciones

- CA-01.1: Toda operación de **creación** sobre una entidad auditable genera un registro con `accion = CREAR`, `valor_anterior = NULL` y `valor_nuevo` con el estado inicial del registro en formato JSON.
- CA-01.2: Toda operación de **modificación** genera un registro con `accion = MODIFICAR`, `valor_anterior` con el estado previo en JSON y `valor_nuevo` con el estado posterior en JSON.
- CA-01.3: Los campos de tipo contraseña nunca aparecen en `valor_anterior` ni `valor_nuevo`. Su modificación se indica únicamente en el campo `detalle` con el texto `'password actualizado'`.
- CA-01.4: El registro contiene el `id_registro` de la entidad afectada, de modo que se puede relacionar con el registro de negocio concreto.

### HU-AUD-02 — Actividad de un usuario

- CA-02.1: El endpoint `GET /api/auditoria` acepta el parámetro opcional `username` y devuelve únicamente los registros del usuario indicado.
- CA-02.2: El endpoint acepta parámetros `fechaDesde` y `fechaHasta` en formato ISO-8601 (`yyyy-MM-dd`) para acotar el rango temporal.
- CA-02.3: Los resultados se ordenan siempre por `fecha_hora DESC`.
- CA-02.4: La pantalla Angular `/auditoria` permite al ADMIN seleccionar un usuario del sistema desde un selector y ver su actividad filtrada.

### HU-AUD-03 — Historial de cambios de un registro

- CA-03.1: El endpoint `GET /api/auditoria` acepta los parámetros opcionales `entidad` (ej. `USUARIO`, `CLIENTE`) e `idRegistro` (Long) para filtrar el historial de un registro concreto.
- CA-03.2: Cuando se filtra por `idRegistro`, la respuesta incluye todos los eventos de auditoría de ese registro ordenados por `fecha_hora ASC`, de forma que se puede reconstruir la evolución cronológica.
- CA-03.3: Los campos `valor_anterior` y `valor_nuevo` son JSON legible que representa el estado del registro en ese momento.

### HU-AUD-04 — Eliminaciones

- CA-04.1: Toda operación de **eliminación** genera un registro con `accion = ELIMINAR` y `valor_anterior` con el último estado del registro antes del borrado en JSON. `valor_nuevo` queda `NULL`.
- CA-04.2: El `username` del usuario que ejecuta la eliminación se extrae del JWT del `SecurityContext`, no del body del request.
- CA-04.3: El `id_registro` del registro eliminado se conserva en el registro de auditoría, aunque el registro ya no exista en la tabla origen.

### HU-AUD-05 — Filtros avanzados y paginación

- CA-05.1: Los parámetros de filtro son todos opcionales. Si no se proporciona ninguno, se devuelven todos los registros paginados.
- CA-05.2: El parámetro `size` tiene un valor por defecto de 20 y un máximo de 100. El parámetro `page` es 0-based.
- CA-05.3: La respuesta incluye metadatos de paginación: `totalElements`, `totalPages`, `currentPage`, `pageSize`.
- CA-05.4: El endpoint devuelve HTTP 403 si el token JWT no corresponde a un ADMIN.

### HU-AUD-06 — Gestión de usuarios

- CA-06.1: `POST /api/usuarios` exitoso → `entidad = USUARIO`, `accion = CREAR`, `id_registro` = ID del usuario creado, `valor_nuevo` = `{username, nombre, rol}`.
- CA-06.2: `PUT /api/usuarios/{id}` exitoso → `accion = MODIFICAR`, `valor_anterior` = estado antes del cambio, `valor_nuevo` = estado después. Si cambia el rol, `detalle` indica `'rol: REGULAR → ADMIN'`.
- CA-06.3: `DELETE /api/usuarios/{id}` exitoso → `accion = ELIMINAR`, `valor_anterior` = `{username, nombre, rol}` del usuario eliminado.
- CA-06.4: Las operaciones que fallan por validación (400) o entidad no encontrada (404) **no** generan registro de auditoría.

### HU-AUD-07 — Comparativa de campos

- CA-07.1: El frontend muestra columnas `Valor anterior` y `Valor nuevo` en la tabla de auditoría. Cuando ambas tienen contenido JSON, se renderiza una comparativa campo a campo en un panel expandible por fila.
- CA-07.2: Los campos sin cambio no se destacan; los campos que sí cambiaron se resaltan visualmente (ej. fondo amarillo).

---

## 4. Reglas de negocio

- **RN-01 — Inmutabilidad absoluta:** Los registros de auditoría nunca se modifican ni eliminan desde la aplicación. No existe ningún endpoint PUT, PATCH o DELETE sobre `/api/auditoria`. La tabla en base de datos no tiene permisos de DELETE ni UPDATE para el usuario de aplicación.
- **RN-02 — No bloqueo del flujo de negocio:** La escritura de auditoría se ejecuta dentro de un bloque try-catch aislado. Un fallo en la persistencia de auditoría no propaga excepción al flujo principal. La operación de negocio se completa siempre.
- **RN-03 — Captura de IP real del cliente:** Se extrae la IP del header `X-Forwarded-For` (proxy/balanceador). Si está presente y contiene múltiples valores (proxy chain), se toma el primero. Si el header no existe, se usa `HttpServletRequest.getRemoteAddr()`. Máximo 45 caracteres (soporte IPv6).
- **RN-04 — El actor siempre viene del JWT:** El `username` del usuario que ejecuta la acción se extrae del `SecurityContext` (`Authentication.getName()`), nunca del body de la request. Esto impide que un usuario se atribuya acciones de otro.
- **RN-05 — Contraseñas fuera de auditoría:** Ningún campo del registro de auditoría (`valor_anterior`, `valor_nuevo`, `detalle`) puede contener un valor de contraseña, ni en texto plano ni hasheado con BCrypt. El cambio de contraseña solo se documenta con `'password actualizado'` en `detalle`.
- **RN-06 — Acceso exclusivo ADMIN:** Únicamente los usuarios con rol `ADMIN` pueden consultar registros de auditoría. Los usuarios `REGULAR` reciben HTTP 403 en cualquier intento de acceso a `/api/auditoria/**`. La protección opera a nivel de Spring Security, antes de llegar al controller.
- **RN-07 — Timestamps en UTC:** `fecha_hora` se almacena siempre en UTC (`DATETIME2` de SQL Server con precisión de milisegundos). El frontend convierte a la zona horaria local del navegador en la capa de presentación.
- **RN-08 — Diseño extensible por entidad:** El módulo usa `entidad` (String o enum) para identificar el tipo de registro afectado. Al incorporar una nueva entidad CRM (ej. `CLIENTE`), basta con añadir su valor al enum `EntidadAuditable` y llamar al `AuditoriaService` desde el nuevo controller, sin modificar la estructura de la tabla ni el servicio de auditoría.
- **RN-09 — Estado antes de la operación destructiva:** En operaciones de eliminación o modificación, el estado `valor_anterior` se captura **antes** de ejecutar el `repository.save()` o `repository.deleteById()`, no después.
- **RN-10 — Retención de registros:** Los registros de auditoría se conservan un mínimo de **2 años** desde su fecha de creación. El parámetro de retención se configura en `BRM_CONFIG.CFG_CONFIG` con la variable `AUDIT_RETENTION_DAYS` (valor por defecto: 730) para que pueda ajustarse sin redespliegue.
- **RN-11 — Username en intentos de login fallido:** Se registra el username exactamente como lo envió el cliente, aunque no exista en la base de datos. Si supera los 100 caracteres, se trunca. Esto permite detectar ataques con usernames sintéticos.

---

## 5. Entidades auditables

El módulo se diseña para auditar cualquier entidad del CRM. La tabla de auditoría es genérica: el campo `entidad` identifica el tipo de objeto afectado y `id_registro` identifica el registro concreto dentro de esa entidad. La entidad **Usuario** funciona como piloto en Sprint 1.

| Entidad | Valor del campo `entidad` | Sprint | Estado | Acciones auditadas |
|---------|--------------------------|--------|--------|-------------------|
| Usuario del sistema | `USUARIO` | Sprint 1 | **Piloto — activo** | CREAR, MODIFICAR, ELIMINAR |
| Cliente | `CLIENTE` | Sprint 2 | Pendiente | CREAR, MODIFICAR, ELIMINAR |
| Contacto | `CONTACTO` | Sprint 2 | Pendiente | CREAR, MODIFICAR, ELIMINAR |
| Oportunidad | `OPORTUNIDAD` | Sprint 3 | Pendiente | CREAR, MODIFICAR, ELIMINAR, CAMBIO_ESTADO |
| Actividad / Tarea | `ACTIVIDAD` | Sprint 3 | Pendiente | CREAR, COMPLETAR, ELIMINAR |
| Producto / Servicio | `PRODUCTO` | Sprint 4 | Pendiente | CREAR, MODIFICAR, ELIMINAR |
| Oferta / Presupuesto | `OFERTA` | Sprint 4 | Pendiente | CREAR, MODIFICAR, ENVIADA, ACEPTADA, RECHAZADA |

> Nota: las acciones `LOGIN_EXITOSO` y `LOGIN_FALLIDO` no tienen `entidad` ni `id_registro` asociado ya que son eventos de sesión, no de negocio. Se registran con `entidad = SESION`.

---

## 6. Impacto en el sistema

### Base de datos

Nueva tabla en el esquema `BRM_CONFIG` de la base de datos `logindb`, siguiendo la convención de configuración centralizada del proyecto:

```sql
-- Crear tabla principal de auditoría
CREATE TABLE BRM_CONFIG.AUD_AUDITORIA (
    id                BIGINT         IDENTITY(1,1) NOT NULL,
    entidad           NVARCHAR(50)   NOT NULL,         -- USUARIO, CLIENTE, OPORTUNIDAD, SESION, etc.
    id_registro       BIGINT         NULL,             -- ID del registro afectado (NULL para eventos de sesión)
    accion            NVARCHAR(50)   NOT NULL,         -- CREAR | MODIFICAR | ELIMINAR | LOGIN_EXITOSO | LOGIN_FALLIDO | LOGOUT | CAMBIO_ESTADO
    valor_anterior    NVARCHAR(MAX)  NULL,             -- JSON con estado previo del registro (NULL en CREAR)
    valor_nuevo       NVARCHAR(MAX)  NULL,             -- JSON con estado nuevo del registro (NULL en ELIMINAR)
    username          NVARCHAR(100)  NOT NULL,         -- usuario que ejecuta la acción (del JWT / SecurityContext)
    fecha_hora        DATETIME2(3)   NOT NULL,         -- UTC, precisión de milisegundos
    ip_origen         NVARCHAR(45)   NOT NULL,         -- IPv4 o IPv6 del cliente
    detalle           NVARCHAR(500)  NULL,             -- info adicional: cambio de rol, password actualizado, motivo de fallo, etc.
    CONSTRAINT PK_AUD_AUDITORIA PRIMARY KEY CLUSTERED (id ASC)
);

-- Índices para las consultas más frecuentes de negocio
CREATE INDEX IX_AUD_fecha_hora    ON BRM_CONFIG.AUD_AUDITORIA (fecha_hora DESC);
CREATE INDEX IX_AUD_username      ON BRM_CONFIG.AUD_AUDITORIA (username);
CREATE INDEX IX_AUD_entidad       ON BRM_CONFIG.AUD_AUDITORIA (entidad);
CREATE INDEX IX_AUD_id_registro   ON BRM_CONFIG.AUD_AUDITORIA (id_registro) WHERE id_registro IS NOT NULL;
CREATE INDEX IX_AUD_accion        ON BRM_CONFIG.AUD_AUDITORIA (accion);

-- Parámetros de configuración del módulo de auditoría
INSERT INTO BRM_CONFIG.CFG_CONFIG (CFG_CONFIG_VAR, CFG_CONFIG_VALOR, CFG_CONFIG_GRUPO)
VALUES
    ('AUDIT_ENABLED',        'true', 'AUDITORIA'),
    ('AUDIT_RETENTION_DAYS', '730',  'AUDITORIA');
```

Valores posibles del campo `accion`:

| Valor | Tipo de evento | Entidad típica |
|-------|---------------|----------------|
| `CREAR` | Alta de un nuevo registro | Todas las entidades CRM |
| `MODIFICAR` | Edición de un registro existente | Todas las entidades CRM |
| `ELIMINAR` | Baja de un registro | Todas las entidades CRM |
| `CAMBIO_ESTADO` | Cambio de estado de negocio | Oportunidad, Oferta |
| `LOGIN_EXITOSO` | Autenticación correcta | SESION |
| `LOGIN_FALLIDO` | Credenciales incorrectas | SESION |
| `LOGOUT` | Cierre de sesión explícito o por timeout | SESION |

---

### Backend (Spring Boot)

**Clases nuevas a crear en `com.sergio.login`:**

| Clase | Tipo | Responsabilidad |
|-------|------|-----------------|
| `Auditoria.java` | `@Entity` JPA | Mapea `BRM_CONFIG.AUD_AUDITORIA`. Campos: `id`, `entidad`, `idRegistro`, `accion`, `valorAnterior`, `valorNuevo`, `username`, `fechaHora` (`LocalDateTime` UTC), `ipOrigen`, `detalle`. |
| `EntidadAuditable.java` | `enum` | Centraliza los valores de `entidad`: `USUARIO`, `CLIENTE`, `CONTACTO`, `OPORTUNIDAD`, `SESION`, etc. Único punto de cambio al incorporar nuevas entidades. |
| `AccionAuditoria.java` | `enum` | Centraliza los valores de `accion`: `CREAR`, `MODIFICAR`, `ELIMINAR`, `CAMBIO_ESTADO`, `LOGIN_EXITOSO`, `LOGIN_FALLIDO`, `LOGOUT`. |
| `AuditoriaRepository.java` | `JpaRepository<Auditoria, Long>` + `JpaSpecificationExecutor<Auditoria>` | Acceso a datos con soporte para `Specification` dinámica (filtros combinados). |
| `AuditoriaService.java` | `@Service` | Método `registrar(...)` con try-catch aislado. Extracción de IP con fallback `X-Forwarded-For` / `getRemoteAddr()`. Consulta del flag `AUDIT_ENABLED` desde `ConfigService`. Método `buscar(filtros, pageable)` que construye la `Specification` y delega en el repositorio. |
| `AuditoriaController.java` | `@RestController @RequestMapping("/api/auditoria")` | `GET /api/auditoria` paginado con filtros opcionales. Devuelve `Page<AuditoriaDTO>` (no la entidad directamente). |
| `AuditoriaDTO.java` | Record / POJO | Proyección de los campos de `Auditoria` para la API: `id`, `entidad`, `idRegistro`, `accion`, `valorAnterior`, `valorNuevo`, `username`, `fechaHora` (ISO-8601), `ipOrigen`, `detalle`. |
| `AuditoriaSpec.java` | `Specification<Auditoria>` | Predicados dinámicos reutilizables: `byEntidad`, `byAccion`, `byUsername`, `byIdRegistro`, `byFechaDesde`, `byFechaHasta`. |

**Clases existentes a modificar:**

| Clase | Modificación necesaria |
|-------|----------------------|
| `AuthController.java` | Inyectar `AuditoriaService` y `HttpServletRequest` por constructor. Tras evaluar credenciales, llamar `registrar(SESION, null, LOGIN_EXITOSO/LOGIN_FALLIDO, ...)` en ambas ramas. Agregar `@PostMapping("/logout")` que extrae el username del JWT manualmente para registrar el `LOGOUT`. |
| `UsuarioController.java` | Inyectar `AuditoriaService` y `HttpServletRequest`. En `create`: capturar `valor_nuevo` del usuario creado. En `update`: capturar `valor_anterior` antes de modificar y `valor_nuevo` tras guardar. En `delete`: capturar `valor_anterior` **antes** de `deleteById`. El actor se obtiene siempre de `SecurityContextHolder.getContext().getAuthentication().getName()`. |
| `SecurityConfig.java` | Añadir, **antes de** `.anyRequest().authenticated()`: (1) `requestMatchers(POST, "/api/auth/logout").permitAll()` — el endpoint valida el JWT manualmente. (2) `requestMatchers("/api/auditoria/**").hasAuthority("ADMIN")`. |

---

### Frontend (Angular)

**Archivos nuevos:**

| Archivo | Responsabilidad |
|---------|-----------------|
| `src/app/auditoria/auditoria.component.ts` | Componente tabla con paginación del lado del servidor, barra de filtros (entidad, acción, username, fechaDesde, fechaHasta) y panel expandible de comparativa `valor_anterior` vs `valor_nuevo`. |
| `src/app/auditoria/auditoria.component.html` | Template con tabla responsiva, filtros en formulario reactivo, paginador y resaltado de campos modificados. |
| `src/app/services/auditoria.service.ts` | `getAuditoria(filtros: AuditoriaFiltros, page: number, size: number): Observable<PageResponse<AuditoriaEntry>>`. Adjunta automáticamente el Bearer token vía `AuthInterceptor` ya existente. |
| `src/app/models/auditoria.model.ts` | Interfaces TypeScript: `AuditoriaEntry`, `AuditoriaFiltros`, `PageResponse<T>`. |

**Archivos existentes a modificar:**

| Archivo | Modificación |
|---------|-------------|
| `app.routes.ts` | Nueva ruta: `{ path: 'auditoria', component: AuditoriaComponent, canActivate: [authGuard, adminGuard] }` |
| `auth.service.ts` | Agregar método `logout(): Observable<any>` que llama `POST /api/auth/logout` con el token actual, luego limpia el `localStorage` y emite el evento de sesión terminada. |
| `session.service.ts` | En el handler de expiración por timeout, invocar `this.authService.logout().subscribe({ complete: () => this.router.navigate(['/login']) })` antes de redirigir, pasando `detalle = 'Timeout de sesión'` al backend. |
| Componente de navegación | Agregar enlace "Auditoría" visible condicionalmente solo si `rol === 'ADMIN'`. |

---

### Seguridad

- El endpoint `GET /api/auditoria/**` se protege con `.hasAuthority("ADMIN")` en `SecurityConfig`, que verifica el rol antes de que la request llegue al controller. Un usuario `REGULAR` recibe HTTP 403 inmediatamente.
- El endpoint `POST /api/auth/logout` se declara como `permitAll` únicamente porque el token puede estar expirado en el momento del logout. El backend extrae y valida el JWT manualmente: si es inválido o está ausente, retorna HTTP 400 sin crear registro de auditoría.
- El usuario de base de datos de la aplicación debe tener **solo `INSERT` y `SELECT`** sobre `BRM_CONFIG.AUD_AUDITORIA`. Nunca `DELETE` ni `UPDATE`. En el entorno de desarrollo con usuario `SA` esto se gestiona por convención; en producción debe aplicarse mediante permisos granulares.
- Los campos `valor_anterior` y `valor_nuevo` se serializan a JSON en el backend (con Jackson) antes de persistir. El backend nunca serializa campos de contraseña: el método de serialización debe incluir una lista negra de nombres de campo (`password`, `hash`, `bcrypt`).
- Nunca añadir `/api/auditoria/**` a `permitAll`.

---

## 7. Flujo principal

**Flujo: Modificación de un usuario CRM con registro de auditoría**

```
ADMIN en /usuarios — edita nombre y rol de un usuario
    │
    ├─► PUT /api/usuarios/{id}  [Authorization: Bearer <token>]
    │         body: { nombre: "Juan López", rol: "ADMIN" }
    │
    ▼
JwtFilter
    └── Valida token → extrae username="admin" y rol="ADMIN"
        → pone Authentication en SecurityContext
    │
    ▼
UsuarioController.update(id, body)
    ├── repo.findById(id)                  → Usuario encontrado
    ├── valorAnterior = serialize(usuario) → '{"username":"jlopez","nombre":"Juan","rol":"REGULAR"}'
    ├── Aplica cambios al objeto
    ├── repo.save(usuarioModificado)       → persiste cambios en dbo.usuarios
    ├── valorNuevo = serialize(updated)    → '{"username":"jlopez","nombre":"Juan López","rol":"ADMIN"}'
    ├── actor = SecurityContextHolder.getAuthentication().getName()  → "admin"
    ├── auditoriaService.registrar(
    │       entidad:       USUARIO,
    │       idRegistro:    id,
    │       accion:        MODIFICAR,
    │       valorAnterior: '{"username":"jlopez","nombre":"Juan","rol":"REGULAR"}',
    │       valorNuevo:    '{"username":"jlopez","nombre":"Juan López","rol":"ADMIN"}',
    │       username:      "admin",
    │       ipOrigen:      "192.168.1.10",
    │       detalle:       "rol: REGULAR → ADMIN",
    │       request:       HttpServletRequest
    │   )
    │   └── [try-catch aislado — error no propaga]
    └── ResponseEntity.ok(UsuarioDTO)
    │
    ▼
Angular recibe HTTP 200 → actualiza tabla de usuarios
```

**Flujo: Consulta de auditoría por el ADMIN**

```
ADMIN en /auditoria — filtra por entidad=USUARIO, fechaDesde=2026-05-01
    │
    ├─► GET /api/auditoria?entidad=USUARIO&fechaDesde=2026-05-01&page=0&size=20
    │         [Authorization: Bearer <token ADMIN>]
    │
    ▼
SecurityConfig → .hasAuthority("ADMIN") → AUTORIZADO
    │
    ▼
AuditoriaController.buscar(filtros, pageable)
    ├── AuditoriaService.buscar(filtros, pageable)
    │       └── AuditoriaSpec.build(filtros) → Specification dinámica
    │           └── AuditoriaRepository.findAll(spec, pageable)
    │               → Page<Auditoria> con 20 registros más recientes
    ├── Mapea a Page<AuditoriaDTO>
    └── ResponseEntity.ok(pageDto)
    │
    ▼
Angular — AuditoriaComponent
    └── Renderiza tabla con fechas en zona horaria local del navegador
        Columnas: Fecha/Hora | Usuario | Entidad | ID Registro | Acción | Afectado | Detalle
        Fila expandible: comparativa JSON valor_anterior vs valor_nuevo con campos resaltados
```

---

## 8. Flujos alternativos / errores

| Escenario | Comportamiento esperado |
|-----------|------------------------|
| Login fallido (usuario inexistente) | `LOGIN_FALLIDO` con el username tal como lo envió el cliente (truncado a 100 chars si excede). La respuesta HTTP 401 no se altera. |
| Login fallido (contraseña incorrecta) | `LOGIN_FALLIDO` con el username existente. Igual al caso anterior. |
| Error al persistir auditoría (BD caída o timeout) | `AuditoriaService` captura la excepción, logea con `log.error(...)` y retorna sin relanzar. El flujo de negocio devuelve su respuesta normal. |
| `PUT /api/usuarios/{id}` con rol inválido (400) | La validación ocurre antes de la escritura; no se genera registro de auditoría. |
| `DELETE /api/usuarios/{id}` para ID inexistente (404) | El controller retorna 404 antes de llegar a `deleteById`; no se genera registro. |
| Usuario REGULAR intenta `GET /api/auditoria` | Spring Security devuelve HTTP 403 antes de entrar al controller. No se genera registro de auditoría por el intento. |
| Token JWT expirado en `GET /api/auditoria` | `JwtFilter` rechaza con HTTP 401. No hay acceso al controller. |
| `POST /api/auth/logout` sin token o con token inválido | `AuthController.logout()` retorna HTTP 400. No se crea registro de auditoría. |
| `X-Forwarded-For` con múltiples IPs | Se toma la primera IP de la lista (`ip.split(",")[0].trim()`), que corresponde al cliente original. |
| `AUDIT_ENABLED = false` en `BRM_CONFIG` | `AuditoriaService.registrar()` verifica el flag al inicio del método y retorna inmediatamente sin persistir. El resto del sistema funciona con normalidad. |
| Timeout de sesión en frontend | `SessionService` llama `authService.logout()` con `detalle = 'Timeout de sesión'` antes de redirigir a `/login`. Se registra `LOGOUT` en auditoría. |
| Intento de `DELETE /api/auditoria/{id}` | El endpoint no existe. Spring devuelve HTTP 404. `SecurityConfig` no registra ninguna ruta DELETE para auditoría. |

---

## 9. Dependencias

| Dependencia | Tipo | Detalle |
|-------------|------|---------|
| `BRM_CONFIG.AUD_AUDITORIA` | Nueva tabla SQL Server | Debe crearse con el DDL del paso 6 antes de arrancar el módulo. |
| `BRM_CONFIG.CFG_CONFIG` | Tabla existente | Insertar las variables `AUDIT_ENABLED` y `AUDIT_RETENTION_DAYS` antes del primer arranque. |
| `Spring Data JPA` + `JpaSpecificationExecutor` | Ya presente en el proyecto | No requiere nueva dependencia Maven. Solo activar `JpaSpecificationExecutor` en el repositorio. |
| `Jackson ObjectMapper` | Ya presente en Spring Boot | Usado para serializar `valor_anterior` y `valor_nuevo` a JSON en `AuditoriaService`. |
| `HttpServletRequest` | Spring MVC — ya disponible | Inyectado en los controllers para extraer la IP de origen. |
| `SecurityContextHolder` | Spring Security — ya presente | Para extraer el actor (`Authentication.getName()`) en operaciones CRUD. |
| `authGuard` + `adminGuard` | Ya presentes en Angular | Reutilizados para proteger la ruta `/auditoria`. |
| `auth.service.ts` | Ya presente en Angular | Requiere nuevo método `logout()` que llame al endpoint. |
| `session.service.ts` | Ya presente en Angular | Requiere integración con `logout()` en el handler de timeout. |
| `ConfigService.java` | Ya presente en el proyecto | Consultado en `AuditoriaService` para leer `AUDIT_ENABLED` sin hardcodear. |

---

## 10. Estimación de complejidad

| Componente | Complejidad | Story Points | Notas |
|------------|-------------|-------------|-------|
| DDL (`BRM_CONFIG.AUD_AUDITORIA`) + INSERT config | Baja | 1 | Script directo, sin FK. Requiere schema `BRM_CONFIG` ya existente. |
| `Auditoria.java` (entidad) + enums + `AuditoriaRepository` | Baja | 1 | Patrón ya establecido con `Usuario.java`. |
| `AuditoriaService.java` | Media | 3 | Extracción de IP, serialización JSON sin contraseñas, flag `AUDIT_ENABLED`, try-catch. |
| Modificar `AuthController.java` (login + logout) | Media | 2 | Nuevo endpoint logout con validación manual de JWT. |
| Modificar `UsuarioController.java` | Baja-Media | 2 | Captura de `valor_anterior` y `valor_nuevo`, extracción del actor. |
| Modificar `SecurityConfig.java` | Baja | 1 | Dos nuevas reglas de autorización. |
| `AuditoriaController.java` (GET paginado + filtros) | Media | 3 | `Specification` dinámica, DTO mapping, paginación. |
| `AuditoriaSpec.java` (predicados dinámicos) | Media | 2 | Predicados combinables para 5+ parámetros de filtro. |
| `auditoria.service.ts` + `auditoria.model.ts` (Angular) | Baja | 1 | Llamada HTTP tipada con los filtros. |
| `AuditoriaComponent` (tabla, filtros, comparativa JSON) | Media-Alta | 4 | Paginación server-side, renderizado JSON diff, filtros reactivos. |
| Integración logout en `session.service.ts` | Baja | 1 | Una llamada al método antes del redirect. |
| Tests unitarios backend (`AuditoriaService`, `AuthController`) | Media | 3 | Mocks de repositorio, request, SecurityContext. |
| **Total Sprint 1** | | **24 SP** | Incluye entidad piloto (Usuario). Entidades CRM futuras: ~3 SP por entidad adicional. |

---

## 11. Instrucciones para el desarrollador

### Orden de implementación recomendado

**Paso 1 — DDL y configuración en base de datos (prerequisito)**

Ejecutar el siguiente script sobre `logindb` antes de tocar ningún archivo Java o TypeScript:

```sql
-- Verificar que el schema BRM_CONFIG existe (ya existe en logindb)
-- Crear la tabla de auditoría
CREATE TABLE BRM_CONFIG.AUD_AUDITORIA (
    id             BIGINT        IDENTITY(1,1) NOT NULL,
    entidad        NVARCHAR(50)  NOT NULL,
    id_registro    BIGINT        NULL,
    accion         NVARCHAR(50)  NOT NULL,
    valor_anterior NVARCHAR(MAX) NULL,
    valor_nuevo    NVARCHAR(MAX) NULL,
    username       NVARCHAR(100) NOT NULL,
    fecha_hora     DATETIME2(3)  NOT NULL,
    ip_origen      NVARCHAR(45)  NOT NULL,
    detalle        NVARCHAR(500) NULL,
    CONSTRAINT PK_AUD_AUDITORIA PRIMARY KEY CLUSTERED (id ASC)
);

CREATE INDEX IX_AUD_fecha_hora  ON BRM_CONFIG.AUD_AUDITORIA (fecha_hora DESC);
CREATE INDEX IX_AUD_username    ON BRM_CONFIG.AUD_AUDITORIA (username);
CREATE INDEX IX_AUD_entidad     ON BRM_CONFIG.AUD_AUDITORIA (entidad);
CREATE INDEX IX_AUD_id_registro ON BRM_CONFIG.AUD_AUDITORIA (id_registro) WHERE id_registro IS NOT NULL;
CREATE INDEX IX_AUD_accion      ON BRM_CONFIG.AUD_AUDITORIA (accion);

INSERT INTO BRM_CONFIG.CFG_CONFIG (CFG_CONFIG_VAR, CFG_CONFIG_VALOR, CFG_CONFIG_GRUPO)
VALUES
    ('AUDIT_ENABLED',        'true', 'AUDITORIA'),
    ('AUDIT_RETENTION_DAYS', '730',  'AUDITORIA');
```

---

**Paso 2 — Capa de dominio backend**

Crear `EntidadAuditable.java`:
```java
public enum EntidadAuditable {
    SESION, USUARIO, CLIENTE, CONTACTO, OPORTUNIDAD, ACTIVIDAD, PRODUCTO, OFERTA
}
```

Crear `AccionAuditoria.java`:
```java
public enum AccionAuditoria {
    CREAR, MODIFICAR, ELIMINAR, CAMBIO_ESTADO,
    LOGIN_EXITOSO, LOGIN_FALLIDO, LOGOUT
}
```

Crear `Auditoria.java`:
```java
@Entity
@Table(name = "AUD_AUDITORIA", schema = "BRM_CONFIG")
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EntidadAuditable entidad;

    private Long idRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccionAuditoria accion;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String valorAnterior;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String valorNuevo;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private LocalDateTime fechaHora;   // siempre UTC: LocalDateTime.now(ZoneOffset.UTC)

    @Column(nullable = false, length = 45)
    private String ipOrigen;

    @Column(length = 500)
    private String detalle;

    // constructor, getters (sin setters para favorecer inmutabilidad en código)
}
```

Crear `AuditoriaRepository.java`:
```java
public interface AuditoriaRepository
        extends JpaRepository<Auditoria, Long>, JpaSpecificationExecutor<Auditoria> {
}
```

---

**Paso 3 — AuditoriaService**

```java
@Service
@Slf4j
public class AuditoriaService {

    private final AuditoriaRepository repo;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    // Inyección por constructor

    public void registrar(EntidadAuditable entidad, Long idRegistro,
                          AccionAuditoria accion,
                          Object estadoAnterior, Object estadoNuevo,
                          String username, HttpServletRequest request,
                          String detalle) {
        try {
            if (!"true".equalsIgnoreCase(configService.get("AUDIT_ENABLED"))) return;

            Auditoria reg = new Auditoria(
                entidad, idRegistro, accion,
                toJson(estadoAnterior),
                toJson(estadoNuevo),
                username,
                LocalDateTime.now(ZoneOffset.UTC),
                extractIp(request),
                detalle
            );
            repo.save(reg);
        } catch (Exception e) {
            log.error("[AUDITORIA] Error al registrar evento {}/{}: {}", entidad, accion, e.getMessage());
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            // Filtrar campos sensibles antes de serializar
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public Page<Auditoria> buscar(AuditoriaFiltrosDTO filtros, Pageable pageable) {
        Specification<Auditoria> spec = AuditoriaSpec.build(filtros);
        return repo.findAll(spec, pageable);
    }
}
```

> **Importante:** Para serializar `UsuarioDTO` sin contraseña, asegurarse de que el objeto pasado como `estadoAnterior`/`estadoNuevo` NO incluya el campo `password`. Usar el record `UsuarioDTO` ya existente (que no tiene password) en lugar de la entidad `Usuario`.

---

**Paso 4 — Modificar AuthController**

```java
// Agregar al constructor:
private final AuditoriaService auditoriaService;

// En el método login(), reemplazar el return existente:
if (usuario.isPresent() && passwordEncoder.matches(password, hash)) {
    String token = jwtUtil.generarToken(username, rol);
    auditoriaService.registrar(SESION, null, LOGIN_EXITOSO,
        null, null, username, request, null);
    return ResponseEntity.ok(...);
}
auditoriaService.registrar(SESION, null, LOGIN_FALLIDO,
    null, null,
    username != null ? username.substring(0, Math.min(username.length(), 100)) : "DESCONOCIDO",
    request, "Credenciales inválidas");
return ResponseEntity.status(401).body(...);

// Nuevo endpoint logout:
@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.badRequest().body(Map.of("message", "Token requerido"));
    }
    try {
        String token = authHeader.substring(7);
        String username = jwtUtil.extraerUsername(token);
        String detalle = request.getParameter("detalle"); // nullable
        auditoriaService.registrar(SESION, null, LOGOUT,
            null, null, username, request, detalle);
        return ResponseEntity.ok(Map.of("message", "Logout registrado"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Token inválido"));
    }
}
```

---

**Paso 5 — Modificar UsuarioController**

```java
// Agregar al constructor:
private final AuditoriaService auditoriaService;

// En create(), tras repo.save():
UsuarioDTO dto = new UsuarioDTO(saved.getId(), saved.getUsername(), saved.getNombre(), saved.getRol());
String actor = SecurityContextHolder.getContext().getAuthentication().getName();
auditoriaService.registrar(USUARIO, saved.getId(), CREAR,
    null, dto, actor, request, null);

// En update(), ANTES de repo.save():
UsuarioDTO anterior = new UsuarioDTO(u.getId(), u.getUsername(), u.getNombre(), u.getRol());
// ...aplicar cambios y guardar...
UsuarioDTO posterior = new UsuarioDTO(id, updated.getUsername(), updated.getNombre(), updated.getRol());
String detalle = construirDetalleEdicion(anterior, posterior, body);
auditoriaService.registrar(USUARIO, id, MODIFICAR,
    anterior, posterior, actor, request, detalle);

// En delete(), ANTES de repo.deleteById():
Usuario aEliminar = repo.findById(id).get();
UsuarioDTO anteriorDto = new UsuarioDTO(id, aEliminar.getUsername(), aEliminar.getNombre(), aEliminar.getRol());
repo.deleteById(id);
auditoriaService.registrar(USUARIO, id, ELIMINAR,
    anteriorDto, null, actor, request, null);

// Método auxiliar para detalle de edición:
private String construirDetalleEdicion(UsuarioDTO ant, UsuarioDTO nvo, Map<String,String> body) {
    List<String> cambios = new ArrayList<>();
    if (!ant.rol().equals(nvo.rol()))
        cambios.add("rol: " + ant.rol() + " → " + nvo.rol());
    if (body.containsKey("password") && !body.get("password").isBlank())
        cambios.add("password actualizado");
    return cambios.isEmpty() ? null : String.join(", ", cambios);
}
```

---

**Paso 6 — Modificar SecurityConfig**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**", "/api/config/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()  // NUEVO
    .requestMatchers("/api/auditoria/**").hasAuthority("ADMIN")        // NUEVO
    .anyRequest().authenticated()
)
```

---

**Paso 7 — AuditoriaController**

```java
@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200")
public class AuditoriaController {

    private final AuditoriaService service;

    @GetMapping
    public ResponseEntity<Page<AuditoriaDTO>> buscar(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long idRegistro,
            @RequestParam(required = false) @DateTimeFormat(iso=DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso=DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaHora").descending());
        AuditoriaFiltrosDTO filtros = new AuditoriaFiltrosDTO(
            entidad, accion, username, idRegistro,
            fechaDesde != null ? fechaDesde.atStartOfDay() : null,
            fechaHasta != null ? fechaHasta.atTime(23, 59, 59) : null
        );
        Page<AuditoriaDTO> resultado = service.buscar(filtros, pageable)
            .map(AuditoriaDTO::from);
        return ResponseEntity.ok(resultado);
    }
}
```

---

**Paso 8 — Frontend: auth.service.ts**

```typescript
logout(): Observable<any> {
  return this.http.post('/api/auth/logout', {}).pipe(
    finalize(() => {
      localStorage.clear();
      // emitir evento de sesión terminada si existe un Subject de autenticación
    })
  );
}
```

---

**Paso 9 — Frontend: auditoria.service.ts**

```typescript
@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly API = '/api/auditoria';

  constructor(private http: HttpClient) {}

  getAuditoria(filtros: AuditoriaFiltros, page = 0, size = 20): Observable<PageResponse<AuditoriaEntry>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', Math.min(size, 100));
    if (filtros.entidad)    params = params.set('entidad',    filtros.entidad);
    if (filtros.accion)     params = params.set('accion',     filtros.accion);
    if (filtros.username)   params = params.set('username',   filtros.username);
    if (filtros.idRegistro) params = params.set('idRegistro', filtros.idRegistro);
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    return this.http.get<PageResponse<AuditoriaEntry>>(this.API, { params });
  }
}
```

---

**Paso 10 — Frontend: session.service.ts**

Localizar el handler de expiración de sesión por timeout y reemplazar la navegación directa:

```typescript
// ANTES (simplificado):
this.router.navigate(['/login']);

// DESPUÉS:
this.authService.logout().subscribe({
  complete: () => this.router.navigate(['/login'])
});
```

---

### Notas de calidad y buenas practicas

- Nunca hardcodear los literales de `entidad` o `accion` como strings en los controllers. Usar siempre los enums `EntidadAuditable` y `AccionAuditoria`.
- La serialización a JSON de las entidades para `valor_anterior`/`valor_nuevo` debe usar los **DTOs**, no las entidades JPA directamente, para evitar serializar relaciones lazy no deseadas o campos sensibles.
- Al incorporar una nueva entidad CRM en sprints futuros (ej. `Cliente`): (1) añadir `CLIENTE` al enum `EntidadAuditable`, (2) inyectar `AuditoriaService` en `ClienteController`, (3) llamar `registrar(...)` tras cada operación exitosa. No es necesario modificar ninguna otra clase del módulo de auditoría.
- En producción: revocar permisos `DELETE` y `UPDATE` sobre `BRM_CONFIG.AUD_AUDITORIA` al usuario de aplicación a nivel de SQL Server.
- Los tests unitarios de `AuditoriaService` deben verificar que: (a) cuando `AUDIT_ENABLED=false` no se llama a `repo.save()`, (b) cuando `repo.save()` lanza excepción el método retorna sin propagarla, (c) la IP se extrae correctamente con y sin `X-Forwarded-For`.
