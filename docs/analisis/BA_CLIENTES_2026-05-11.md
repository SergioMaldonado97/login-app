# BA — Módulo de Clientes CRM

**Fecha:** 11/05/2026
**Estado:** PENDIENTE DE DESARROLLO
**Sprint:** 2
**Autor:** Analista de Negocio
**Revisado por:** Pendiente

---

## 1. Descripción general

El Módulo de Clientes es el núcleo del CRM. Permite gestionar el catálogo de empresas u organizaciones que son clientes de la compañía. Cada registro representa a una empresa/organización con sus datos de identificación fiscal, de contacto y operativos.

**Objetivo principal:** centralizar la información de clientes para que el equipo comercial pueda consultarla, crearla y mantenerla actualizada desde la aplicación, con trazabilidad completa de los cambios.

**Principio de eliminación lógica:** ningún cliente se elimina físicamente de la base de datos. Los registros se desactivan (estado `INACTIVO`) para conservar el historial y la integridad referencial con futuras entidades (contratos, oportunidades, contactos).

**Alcance del sprint 2:**
- CRUD completo de clientes (sin eliminación física).
- Búsqueda y filtrado por nombre, RFC/NIT, industria y estado.
- Control de acceso por rol: ADMIN puede operar, REGULAR solo puede consultar.
- Registro automático en la tabla de auditoría `BRM_CONFIG.AUD_AUDITORIA` en cada operación de escritura.

---

## 2. Historias de usuario

### HU-CLI-01 — Crear cliente (ADMIN)
**Como** usuario ADMIN,
**quiero** registrar una nueva empresa cliente en el CRM,
**para** tener su información disponible para el equipo comercial.

**Puntos de historia:** 3

---

### HU-CLI-02 — Listar clientes (ADMIN / REGULAR)
**Como** usuario autenticado (ADMIN o REGULAR),
**quiero** ver la lista paginada de todos los clientes activos e inactivos,
**para** conocer el catálogo de empresas gestionadas por la compañía.

**Puntos de historia:** 2

---

### HU-CLI-03 — Buscar y filtrar clientes (ADMIN / REGULAR)
**Como** usuario autenticado,
**quiero** buscar clientes por razón social, RFC/NIT, industria o estado,
**para** localizar rápidamente un cliente concreto sin recorrer toda la lista.

**Puntos de historia:** 2

---

### HU-CLI-04 — Ver detalle de cliente (ADMIN / REGULAR)
**Como** usuario autenticado,
**quiero** ver la ficha completa de un cliente,
**para** consultar todos sus datos (fiscales, de contacto, estado, auditoría).

**Puntos de historia:** 1

---

### HU-CLI-05 — Editar cliente (ADMIN)
**Como** usuario ADMIN,
**quiero** modificar los datos de un cliente existente,
**para** mantener la información actualizada ante cambios fiscales o de contacto.

**Puntos de historia:** 3

---

### HU-CLI-06 — Desactivar cliente (ADMIN)
**Como** usuario ADMIN,
**quiero** desactivar un cliente sin eliminarlo de la base de datos,
**para** conservar el historial y evitar que aparezca en operaciones activas.

**Puntos de historia:** 2

---

### HU-CLI-07 — Reactivar cliente (ADMIN)
**Como** usuario ADMIN,
**quiero** reactivar un cliente previamente desactivado,
**para** retomar la relación comercial sin perder los datos históricos.

**Puntos de historia:** 1

---

## 3. Criterios de aceptación

### HU-CLI-01 — Crear cliente
- [ ] El formulario valida todos los campos obligatorios antes de enviar.
- [ ] El RFC/NIT es único en la base de datos; si ya existe se rechaza con mensaje claro.
- [ ] Al crear exitosamente, el cliente queda en estado `ACTIVO`.
- [ ] Se registra un evento `CREAR` en `BRM_CONFIG.AUD_AUDITORIA` con el actor (usuario ADMIN autenticado).
- [ ] La respuesta HTTP es `201 Created` con el objeto cliente creado.
- [ ] Un usuario REGULAR recibe `403 Forbidden` si intenta usar este endpoint.

### HU-CLI-02 — Listar clientes
- [ ] La lista es paginada (tamaño de página configurable; valor por defecto en `BRM_CONFIG.CFG_CONFIG`).
- [ ] Por defecto se muestran clientes `ACTIVO` primero, ordenados por `RAZON_SOCIAL` ascendente.
- [ ] Un usuario REGULAR puede acceder a este listado.
- [ ] La respuesta incluye metadatos de paginación (página actual, total de páginas, total de registros).

### HU-CLI-03 — Buscar y filtrar
- [ ] El filtro por `RAZON_SOCIAL` es insensible a mayúsculas/minúsculas y acepta búsqueda parcial.
- [ ] Se puede combinar más de un filtro a la vez.
- [ ] Si no hay resultados, la API devuelve `200 OK` con lista vacía (no `404`).

### HU-CLI-04 — Ver detalle
- [ ] Si el ID no existe, la API devuelve `404 Not Found`.
- [ ] Se muestran todos los campos incluyendo `FECHA_CREACION`, `FECHA_MODIFICACION` y `USUARIO_CREACION`.

### HU-CLI-05 — Editar cliente
- [ ] No se puede editar el RFC/NIT a uno que ya existe en otro registro.
- [ ] El campo `FECHA_MODIFICACION` se actualiza automáticamente.
- [ ] Se registra un evento `MODIFICAR` en auditoría con los valores anteriores y posteriores.
- [ ] La respuesta HTTP es `200 OK` con el objeto actualizado.

### HU-CLI-06 — Desactivar cliente
- [ ] Solo cambia `ESTADO` de `ACTIVO` a `INACTIVO`; no borra el registro.
- [ ] Un cliente ya `INACTIVO` no puede volver a desactivarse (devuelve `409 Conflict`).
- [ ] Se registra un evento `DESACTIVAR` en auditoría.

### HU-CLI-07 — Reactivar cliente
- [ ] Solo cambia `ESTADO` de `INACTIVO` a `ACTIVO`.
- [ ] Se registra un evento `REACTIVAR` en auditoría.

---

## 4. Reglas de negocio

| ID | Regla |
|----|-------|
| RN-CLI-01 | El RFC/NIT es el identificador fiscal único del cliente; no puede repetirse en la tabla. |
| RN-CLI-02 | Los clientes nunca se eliminan físicamente; solo se desactivan (`ESTADO = 'INACTIVO'`). |
| RN-CLI-03 | Solo usuarios con rol `ADMIN` pueden crear, editar, desactivar o reactivar clientes. |
| RN-CLI-04 | Usuarios con rol `REGULAR` solo pueden leer (listar, buscar, ver detalle). |
| RN-CLI-05 | Toda operación de escritura (crear, editar, desactivar, reactivar) debe registrarse en `BRM_CONFIG.AUD_AUDITORIA`. |
| RN-CLI-06 | El campo `ESTADO` solo admite los valores `'ACTIVO'` o `'INACTIVO'`. |
| RN-CLI-07 | El campo `INDUSTRIA` debe validarse contra un catálogo cerrado (ver sección 5). |
| RN-CLI-08 | El correo electrónico del cliente debe tener formato válido (RFC 5321). |
| RN-CLI-09 | El teléfono solo admite dígitos, espacios, guiones y el prefijo `+`; longitud mínima 7 dígitos. |
| RN-CLI-10 | `FECHA_CREACION` y `USUARIO_CREACION` son inmutables una vez insertados. |
| RN-CLI-11 | El tamaño de página por defecto se lee de `BRM_CONFIG.CFG_CONFIG` (clave `CLIENTES_PAGE_SIZE`, grupo `CLIENTES`). |
| RN-CLI-12 | Los clientes inactivos son visibles en búsquedas pero se distinguen visualmente del resto. |

---

## 5. Modelo de datos

### Tabla `dbo.CRM_CLIENTES`

```sql
CREATE TABLE dbo.CRM_CLIENTES (
    ID               BIGINT          IDENTITY(1,1)   NOT NULL,
    RAZON_SOCIAL     NVARCHAR(200)                   NOT NULL,
    RFC_NIT          NVARCHAR(20)                    NOT NULL,
    INDUSTRIA        NVARCHAR(60)                    NOT NULL,
    TELEFONO         NVARCHAR(30)                        NULL,
    EMAIL            NVARCHAR(150)                       NULL,
    DIRECCION        NVARCHAR(300)                       NULL,
    CIUDAD           NVARCHAR(100)                       NULL,
    PAIS             NVARCHAR(100)   DEFAULT 'Mexico'    NULL,
    SITIO_WEB        NVARCHAR(200)                       NULL,
    ESTADO           NVARCHAR(10)    DEFAULT 'ACTIVO' NOT NULL,
    FECHA_CREACION   DATETIME2(0)    DEFAULT GETDATE() NOT NULL,
    FECHA_MODIFICACION DATETIME2(0)                     NULL,
    USUARIO_CREACION NVARCHAR(100)                   NOT NULL,

    CONSTRAINT PK_CRM_CLIENTES         PRIMARY KEY (ID),
    CONSTRAINT UQ_CRM_CLIENTES_RFC_NIT UNIQUE      (RFC_NIT),
    CONSTRAINT CK_CRM_CLIENTES_ESTADO  CHECK       (ESTADO IN ('ACTIVO','INACTIVO')),
    CONSTRAINT CK_CRM_CLIENTES_EMAIL   CHECK       (EMAIL IS NULL OR EMAIL LIKE '%_@__%.__%')
);
```

#### Descripción de columnas

| Columna | Tipo | Nulo | Descripción |
|---------|------|------|-------------|
| `ID` | BIGINT IDENTITY | NO | Clave primaria auto-incremental. |
| `RAZON_SOCIAL` | NVARCHAR(200) | NO | Nombre legal completo de la empresa. |
| `RFC_NIT` | NVARCHAR(20) | NO | Identificador fiscal (RFC México / NIT Colombia u otros). Único en la tabla. |
| `INDUSTRIA` | NVARCHAR(60) | NO | Sector económico. Valores permitidos (catálogo): `TECNOLOGIA`, `MANUFACTURA`, `RETAIL`, `SALUD`, `EDUCACION`, `FINANZAS`, `CONSULTORIA`, `LOGISTICA`, `CONSTRUCCION`, `OTRO`. |
| `TELEFONO` | NVARCHAR(30) | SI | Teléfono principal de la empresa (incluye lada/prefijo). |
| `EMAIL` | NVARCHAR(150) | SI | Correo electrónico de contacto principal. |
| `DIRECCION` | NVARCHAR(300) | SI | Calle, número, colonia / barrio. |
| `CIUDAD` | NVARCHAR(100) | SI | Ciudad o municipio. |
| `PAIS` | NVARCHAR(100) | SI | País; por defecto `'Mexico'`. |
| `SITIO_WEB` | NVARCHAR(200) | SI | URL del sitio web corporativo. |
| `ESTADO` | NVARCHAR(10) | NO | `'ACTIVO'` (default) o `'INACTIVO'`. |
| `FECHA_CREACION` | DATETIME2(0) | NO | Timestamp de inserción; valor por defecto `GETDATE()`. Inmutable. |
| `FECHA_MODIFICACION` | DATETIME2(0) | SI | Timestamp de la última actualización. Se rellena en cada `UPDATE`. |
| `USUARIO_CREACION` | NVARCHAR(100) | NO | `username` del usuario ADMIN que creó el registro. Inmutable. |

#### Índices recomendados

```sql
-- Búsqueda por nombre (búsqueda parcial frecuente)
CREATE INDEX IX_CRM_CLIENTES_RAZON_SOCIAL ON dbo.CRM_CLIENTES (RAZON_SOCIAL);

-- Filtro por estado (columna discriminante de queries habituales)
CREATE INDEX IX_CRM_CLIENTES_ESTADO ON dbo.CRM_CLIENTES (ESTADO);

-- Filtro por industria
CREATE INDEX IX_CRM_CLIENTES_INDUSTRIA ON dbo.CRM_CLIENTES (INDUSTRIA);
```

#### Configuración en `BRM_CONFIG.CFG_CONFIG`

```sql
INSERT INTO BRM_CONFIG.CFG_CONFIG (CFG_CONFIG_VAR, CFG_CONFIG_VALOR, CFG_CONFIG_GRUPO)
VALUES ('CLIENTES_PAGE_SIZE', '20', 'CLIENTES');
```

---

## 6. Impacto en el sistema

### Backend

**Nuevas clases a crear** (paquete `com.sergio.login`):

| Clase | Tipo | Descripción |
|-------|------|-------------|
| `Cliente` | `@Entity` | Entidad JPA que mapea `dbo.CRM_CLIENTES`. |
| `ClienteRepository` | `@Repository` | Extiende `JpaRepository<Cliente, Long>` con métodos de búsqueda paginada y filtros. |
| `ClienteService` | `@Service` | Lógica de negocio: validar unicidad RFC/NIT, gestionar estado, invocar auditoría. |
| `ClienteController` | `@RestController` | Endpoints REST bajo `/api/clientes`. |
| `ClienteDTO` | `record` | Proyección plana para respuestas (excluye campos sensibles innecesarios). |
| `ClienteCreateRequest` | `record` | Payload de entrada para creación/edición con validaciones `@NotBlank`, `@Email`. |

**Endpoints REST nuevos:**

| Método | Endpoint | Roles | Descripción |
|--------|----------|-------|-------------|
| `GET` | `/api/clientes` | ADMIN, REGULAR | Listar (paginado + filtros opcionales). |
| `GET` | `/api/clientes/{id}` | ADMIN, REGULAR | Ver detalle de un cliente. |
| `POST` | `/api/clientes` | ADMIN | Crear cliente. |
| `PUT` | `/api/clientes/{id}` | ADMIN | Editar cliente. |
| `PATCH` | `/api/clientes/{id}/desactivar` | ADMIN | Desactivar cliente (lógico). |
| `PATCH` | `/api/clientes/{id}/reactivar` | ADMIN | Reactivar cliente. |

**Entidad de auditoría:** añadir `CLIENTE` al enum `EntidadAuditable` existente para que `AuditoriaService` lo reconozca.

**Parámetro de paginación:** `ClienteService` debe leer `CLIENTES_PAGE_SIZE` de `ConfigService` (ya existente) para respetar el valor configurado en BD.

### Frontend

**Nuevos componentes Angular:**

| Componente / Archivo | Descripción |
|----------------------|-------------|
| `clientes/clientes.component.ts` | Listado paginado con filtros y toolbar de acciones. |
| `clientes/cliente-form.component.ts` | Formulario reactivo para creación y edición. |
| `clientes/cliente-detail.component.ts` | Vista de solo lectura con ficha completa del cliente. |
| `services/cliente.service.ts` | Wrapper HTTP sobre `/api/clientes`; reutiliza `AuthInterceptor`. |

**Nueva ruta en `app.routes.ts`:**

```typescript
{ path: 'clientes',        component: ClientesComponent,      canActivate: [authGuard] },
{ path: 'clientes/:id',    component: ClienteDetailComponent, canActivate: [authGuard] },
```

- El botón "Nuevo cliente" y las acciones de edición/desactivar solo se renderizan si `rol === 'ADMIN'` (directiva `*ngIf` o `@if` con `AuthService.getRol()`).
- Agregar ítem "Clientes" al menú de navegación del `DashboardComponent`, visible para ambos roles.

### Seguridad

**Cambios en `SecurityConfig.java`:**

```java
.requestMatchers(HttpMethod.GET,   "/api/clientes/**").authenticated()
.requestMatchers(HttpMethod.POST,  "/api/clientes/**").hasAuthority("ROLE_ADMIN")
.requestMatchers(HttpMethod.PUT,   "/api/clientes/**").hasAuthority("ROLE_ADMIN")
.requestMatchers(HttpMethod.PATCH, "/api/clientes/**").hasAuthority("ROLE_ADMIN")
```

Con esto, la capa de seguridad rechaza con `403 Forbidden` cualquier intento de escritura por parte de un usuario `REGULAR`, independientemente de lo que muestre el frontend.

---

## 7. Flujo principal

**Caso: ADMIN crea un nuevo cliente**

```
1. ADMIN accede a /clientes → ClientesComponent carga la lista paginada (GET /api/clientes).
2. ADMIN pulsa "Nuevo cliente" → se abre ClienteFormComponent en modo creación.
3. ADMIN rellena el formulario (RAZON_SOCIAL, RFC_NIT, INDUSTRIA, …) y pulsa "Guardar".
4. Angular valida el formulario reactivo (campos obligatorios, formato email, etc.).
5. Si pasa validación, ClienteService.crear() envía POST /api/clientes con el JWT en el header.
6. JwtFilter valida el token y extrae el username y el rol.
7. SecurityConfig verifica que el rol sea ROLE_ADMIN → permite continuar.
8. ClienteController recibe el request y delega en ClienteService.
9. ClienteService valida unicidad del RFC/NIT contra la BD.
   - Si duplicado → retorna 400 Bad Request con mensaje "RFC/NIT ya registrado".
10. ClienteService persiste el nuevo registro con ESTADO='ACTIVO' y FECHA_CREACION=GETDATE().
11. AuditoriaService.registrar(CLIENTE, id, CREAR, null, clienteDTO, actor, request, null).
12. ClienteController retorna 201 Created con el ClienteDTO.
13. Angular muestra mensaje de éxito y recarga la lista.
```

---

## 8. Flujos alternativos / errores

| Código | Situación | Respuesta API | Comportamiento Frontend |
|--------|-----------|---------------|------------------------|
| `400` | Campo obligatorio vacío | `{"message": "RAZON_SOCIAL es obligatorio"}` | Resaltar campo en rojo con mensaje. |
| `400` | RFC/NIT duplicado | `{"message": "RFC/NIT ya registrado"}` | Toast de error; mantener formulario abierto. |
| `400` | Email con formato inválido | `{"message": "Formato de email inválido"}` | Resaltar campo EMAIL. |
| `400` | Industria fuera del catálogo | `{"message": "Industria no válida"}` | Imposible en frontend (select), posible vía API directa. |
| `403` | REGULAR intenta crear/editar | `{"message": "Acceso denegado"}` | Toast de error; ocultar botón de acción en UI. |
| `404` | ID de cliente no existe | `{"message": "Cliente no encontrado"}` | Redirigir a listado con aviso. |
| `409` | Desactivar cliente ya INACTIVO | `{"message": "El cliente ya está inactivo"}` | Toast informativo; no ejecutar acción. |
| `401` | Token JWT expirado | `{"message": "Token expirado"}` | Redirigir a /login (lógica ya existente en AuthInterceptor). |
| `500` | Error inesperado en BD | `{"message": "Error interno del servidor"}` | Toast genérico de error. |

---

## 9. Dependencias

### Internas (ya existentes en el proyecto)

| Componente | Uso |
|------------|-----|
| `JwtFilter` / `JwtUtil` | Validación del token en cada request a `/api/clientes`. |
| `AuditoriaService` | Registrar eventos CREAR / MODIFICAR / DESACTIVAR / REACTIVAR. |
| `ConfigService` | Leer `CLIENTES_PAGE_SIZE` para la paginación. |
| `AuthInterceptor` (Angular) | Adjuntar el Bearer token automáticamente en todas las llamadas HTTP. |
| `authGuard` / `adminGuard` | Proteger rutas de Angular; `adminGuard` para ruta de creación/edición. |
| `BRM_CONFIG.AUD_AUDITORIA` | Tabla destino de los eventos de auditoría del módulo. |

### Externas / Infraestructura

| Componente | Uso |
|------------|-----|
| SQL Server 2022 (`logindb`) | Almacenamiento de la tabla `dbo.CRM_CLIENTES`. |
| Spring Data JPA + Hibernate 6.6 | ORM; no requiere queries nativas salvo para búsqueda full-text si se requiere en el futuro. |

### Dependencias de datos (precondiciones para testing)

- Debe existir al menos un usuario con rol `ADMIN` en `dbo.usuarios` para ejecutar pruebas de escritura.
- El parámetro `CLIENTES_PAGE_SIZE` debe estar insertado en `BRM_CONFIG.CFG_CONFIG` antes del arranque.

---

## 10. Estimación de complejidad

| Tarea | Puntos | Días estimados |
|-------|--------|----------------|
| Crear tabla `dbo.CRM_CLIENTES` + script SQL | 1 | 0.25 |
| Entidad JPA `Cliente` + `ClienteRepository` | 2 | 0.5 |
| `ClienteService` (lógica, validaciones, auditoría) | 3 | 1.0 |
| `ClienteController` (6 endpoints + manejo de errores) | 3 | 1.0 |
| Actualizar `SecurityConfig` y enum `EntidadAuditable` | 1 | 0.25 |
| `ClientesComponent` Angular (lista + filtros + paginación) | 3 | 1.0 |
| `ClienteFormComponent` Angular (formulario reactivo) | 3 | 1.0 |
| `ClienteDetailComponent` Angular (solo lectura) | 1 | 0.25 |
| `ClienteService` Angular + rutas + menú | 2 | 0.5 |
| Pruebas unitarias backend (JUnit 5 + Mockito) | 3 | 1.0 |
| Pruebas de integración (Postman / Newman) | 2 | 0.5 |
| **TOTAL** | **24** | **7.25 días** |

**Nivel de complejidad global: MEDIO**
Justificación: la lógica de negocio es estándar (CRUD + soft-delete), sin integraciones externas ni procesos batch. La complejidad relativa está en la paginación con filtros combinados y en asegurar la cobertura de auditoría en todos los endpoints de escritura.

---

## 11. Instrucciones para el desarrollador

### Orden de implementación recomendado

1. **Script DDL** — Ejecutar en `logindb` la sentencia `CREATE TABLE dbo.CRM_CLIENTES` de la sección 5. Insertar también el parámetro `CLIENTES_PAGE_SIZE` en `BRM_CONFIG.CFG_CONFIG`.

2. **Backend — Entidad y repositorio**
   - Crear `Cliente.java` con las anotaciones `@Entity`, `@Table(name = "CRM_CLIENTES")`.
   - Usar `@Column(name = "RAZON_SOCIAL", nullable = false, length = 200)` para cada campo.
   - `FECHA_CREACION` se gestiona con `@CreationTimestamp`; `FECHA_MODIFICACION` con `@UpdateTimestamp`.
   - `ClienteRepository` debe declarar un método:
     ```java
     Page<Cliente> findByFilters(String razonSocial, String rfcNit,
                                  String industria, String estado,
                                  Pageable pageable);
     ```
     Implementar con `@Query` JPQL o `Specification<Cliente>` (preferible para filtros opcionales).

3. **Backend — Servicio**
   - `ClienteService.crear()` debe verificar `repo.existsByRfcNit(rfcNit)` antes de persistir.
   - Invocar `auditoriaService.registrar(EntidadAuditable.CLIENTE, ...)` al final de cada operación de escritura, siguiendo el mismo patrón de `UsuarioController`.
   - Leer el tamaño de página con `configService.get("CLIENTES_PAGE_SIZE", "20")`.

4. **Backend — Controlador**
   - Patrón de respuestas: `201 Created` en POST; `200 OK` en PUT/PATCH; `404` si ID no existe; `400` en validaciones; `409` en estado inconsistente.
   - El actor se obtiene igual que en `UsuarioController`: `SecurityContextHolder.getContext().getAuthentication().getName()`.

5. **Backend — Seguridad**
   - Añadir las reglas de autorización en `SecurityConfig.filterChain()` **antes** de `.anyRequest().authenticated()`.
   - Añadir `CLIENTE` al enum `EntidadAuditable`.

6. **Frontend — Servicio Angular**
   - `ClienteService` extiende el patrón de `AuthService`; inyecta `HttpClient`; el token se añade automáticamente via `AuthInterceptor`.

7. **Frontend — Componentes**
   - `ClientesComponent`: tabla con paginador Angular Material + barra de filtros. Botones "Nuevo", "Editar", "Desactivar" visibles solo si `authService.getRol() === 'ADMIN'`.
   - `ClienteFormComponent`: `ReactiveFormsModule`; validators: `Validators.required`, `Validators.email`, `Validators.maxLength`.
   - Agregar ruta `/clientes` a `app.routes.ts` con `canActivate: [authGuard]` (NO `adminGuard`, pues REGULAR también puede ver).

8. **Frontend — Menú**
   - En `DashboardComponent`, agregar el ítem "Clientes" al menú lateral/superior con `routerLink="/clientes"`, visible para todos los roles autenticados.

### Convenciones del proyecto a respetar

- Nombres de clases de dominio en español (`Cliente`, `ClienteDTO`); infraestructura en inglés si aplica.
- Commits en español, descriptivos: ej. `feat(clientes): crear entidad JPA y repositorio`.
- Contraseñas y datos sensibles nunca en código fuente; parámetros de configuración en `BRM_CONFIG.CFG_CONFIG`.
- No hardcodear el tamaño de página; leerlo siempre de `ConfigService`.

### Script SQL completo de inicialización

```sql
-- 1. Crear tabla
CREATE TABLE dbo.CRM_CLIENTES (
    ID                 BIGINT          IDENTITY(1,1)    NOT NULL,
    RAZON_SOCIAL       NVARCHAR(200)                    NOT NULL,
    RFC_NIT            NVARCHAR(20)                     NOT NULL,
    INDUSTRIA          NVARCHAR(60)                     NOT NULL,
    TELEFONO           NVARCHAR(30)                         NULL,
    EMAIL              NVARCHAR(150)                        NULL,
    DIRECCION          NVARCHAR(300)                        NULL,
    CIUDAD             NVARCHAR(100)                        NULL,
    PAIS               NVARCHAR(100)    DEFAULT 'Mexico'    NULL,
    SITIO_WEB          NVARCHAR(200)                        NULL,
    ESTADO             NVARCHAR(10)     DEFAULT 'ACTIVO' NOT NULL,
    FECHA_CREACION     DATETIME2(0)     DEFAULT GETDATE() NOT NULL,
    FECHA_MODIFICACION DATETIME2(0)                        NULL,
    USUARIO_CREACION   NVARCHAR(100)                    NOT NULL,

    CONSTRAINT PK_CRM_CLIENTES         PRIMARY KEY (ID),
    CONSTRAINT UQ_CRM_CLIENTES_RFC_NIT UNIQUE      (RFC_NIT),
    CONSTRAINT CK_CRM_CLIENTES_ESTADO  CHECK       (ESTADO IN ('ACTIVO','INACTIVO')),
    CONSTRAINT CK_CRM_CLIENTES_EMAIL   CHECK       (EMAIL IS NULL OR EMAIL LIKE '%_@__%.__%')
);
GO

-- 2. Índices
CREATE INDEX IX_CRM_CLIENTES_RAZON_SOCIAL ON dbo.CRM_CLIENTES (RAZON_SOCIAL);
CREATE INDEX IX_CRM_CLIENTES_ESTADO       ON dbo.CRM_CLIENTES (ESTADO);
CREATE INDEX IX_CRM_CLIENTES_INDUSTRIA    ON dbo.CRM_CLIENTES (INDUSTRIA);
GO

-- 3. Parámetro de configuración
INSERT INTO BRM_CONFIG.CFG_CONFIG (CFG_CONFIG_VAR, CFG_CONFIG_VALOR, CFG_CONFIG_GRUPO)
VALUES ('CLIENTES_PAGE_SIZE', '20', 'CLIENTES');
GO
```

---

*Documento generado el 11/05/2026 — Módulo Clientes CRM — Sprint 2*
