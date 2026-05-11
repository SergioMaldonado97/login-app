# BA — Módulo de Contactos CRM

**Fecha:** 11/05/2026
**Estado:** PENDIENTE DE DESARROLLO
**Sprint:** 2
**Autor:** Analista de Negocio — CRM Squad
**Versión:** 1.0

---

## 1. Descripción general

El módulo de Contactos gestiona las personas físicas asociadas a un cliente registrado en el CRM. Cada contacto representa un interlocutor real dentro de la organización cliente: puede ser un decisor, un usuario técnico, un responsable de compras, etc.

Un **cliente** (`dbo.CRM_CLIENTES`) puede tener **múltiples contactos**. La relación es 1:N. Los contactos no existen de forma independiente: siempre pertenecen a un cliente activo.

El módulo cubre la creación, consulta, edición y baja lógica de contactos. La baja es lógica (campo `activo = 0`), lo que preserva la integridad histórica de las interacciones registradas en el CRM.

**Alcance del Sprint 2:**

- CRUD completo de contactos (baja lógica, no física).
- Asociación obligatoria a un cliente existente y activo.
- Registro de canal de contacto preferido.
- Control de acceso diferenciado por rol (ADMIN / REGULAR).
- Trazabilidad de creación mediante `creado_por` y `fecha_creacion`.

**Fuera de alcance (Sprint 2):**

- Historial de interacciones por contacto.
- Importación masiva de contactos (CSV/Excel).
- Envío de comunicaciones desde el módulo.
- Foto o archivos adjuntos al contacto.

---

## 2. Historias de usuario

### HU-CON-01 — Listar contactos de un cliente

> **Como** usuario autenticado (ADMIN o REGULAR),
> **quiero** ver la lista de contactos asociados a un cliente,
> **para** identificar rápidamente con quién debo comunicarme.

**Criterios de prioridad:** Alta
**Estimación:** 3 puntos

---

### HU-CON-02 — Crear un nuevo contacto

> **Como** usuario autenticado (ADMIN o REGULAR),
> **quiero** registrar un nuevo contacto asociado a un cliente,
> **para** mantener actualizada la agenda de interlocutores del CRM.

**Criterios de prioridad:** Alta
**Estimación:** 5 puntos

---

### HU-CON-03 — Editar un contacto existente

> **Como** usuario autenticado (ADMIN o REGULAR),
> **quiero** modificar los datos de un contacto (cargo, teléfono, email, canal preferido),
> **para** mantener la información vigente y precisa.

**Criterios de prioridad:** Alta
**Estimación:** 3 puntos

---

### HU-CON-04 — Dar de baja un contacto (baja lógica)

> **Como** usuario ADMIN,
> **quiero** marcar un contacto como inactivo,
> **para** desactivarlo sin eliminar su historial ni su trazabilidad.

**Criterios de prioridad:** Media
**Estimación:** 2 puntos

---

### HU-CON-05 — Ver detalle de un contacto

> **Como** usuario autenticado (ADMIN o REGULAR),
> **quiero** consultar el detalle completo de un contacto,
> **para** ver todos sus datos de comunicación antes de contactarlo.

**Criterios de prioridad:** Media
**Estimación:** 2 puntos

---

## 3. Criterios de aceptación

### HU-CON-01 — Listar contactos

- [ ] El listado se filtra por `id_cliente`; no se muestra mezcla de clientes.
- [ ] Por defecto se muestran solo los contactos con `activo = 1`.
- [ ] El ADMIN puede activar un filtro para ver también los contactos inactivos.
- [ ] El listado muestra: nombre completo, cargo, email, teléfono, canal preferido y estado.
- [ ] Si el cliente no tiene contactos, se muestra el mensaje: _"Este cliente aún no tiene contactos registrados."_
- [ ] El listado es accesible desde la ficha del cliente.

### HU-CON-02 — Crear contacto

- [ ] El formulario requiere como mínimo: nombre, apellido, email y `id_cliente`.
- [ ] No se puede crear un contacto si el cliente asociado no existe o está inactivo.
- [ ] El email debe tener formato válido (regex estándar RFC 5322 simplificado).
- [ ] No puede existir otro contacto activo con el mismo email dentro del mismo cliente.
- [ ] El campo `canal_preferido` acepta únicamente: `EMAIL`, `TELEFONO`, `WHATSAPP`.
- [ ] El campo `creado_por` se rellena automáticamente con el `username` del usuario autenticado (extraído del JWT).
- [ ] La `fecha_creacion` se asigna automáticamente en el backend (no la envía el frontend).
- [ ] Tras guardar, se redirige al listado de contactos del cliente y se muestra un mensaje de éxito.
- [ ] Se registra la acción en `BRM_CONFIG.AUD_AUDITORIA`.

### HU-CON-03 — Editar contacto

- [ ] Se pueden modificar: nombre, apellido, cargo, teléfono, email, canal preferido.
- [ ] No se puede modificar el `id_cliente` ni el `creado_por` ni la `fecha_creacion`.
- [ ] Las validaciones de email y canal preferido aplican igual que en creación.
- [ ] Si el email cambia, se revalida la unicidad dentro del cliente.
- [ ] Tras guardar, se muestra mensaje de éxito y el listado se actualiza.
- [ ] Se registra la acción en `BRM_CONFIG.AUD_AUDITORIA`.

### HU-CON-04 — Baja lógica

- [ ] Solo un usuario con rol `ADMIN` puede ejecutar la baja.
- [ ] El botón de baja no se renderiza en el frontend para usuarios `REGULAR`.
- [ ] El endpoint `DELETE /api/contactos/{id}` devuelve `403 Forbidden` si el rol no es `ADMIN`.
- [ ] La baja establece `activo = 0`; no elimina el registro de la base de datos.
- [ ] Se muestra un diálogo de confirmación antes de ejecutar la baja.
- [ ] Se registra la acción en `BRM_CONFIG.AUD_AUDITORIA`.

### HU-CON-05 — Ver detalle

- [ ] El detalle muestra todos los campos del contacto en modo lectura.
- [ ] Se indica visualmente si el contacto está activo o inactivo.
- [ ] Accesible desde el listado de contactos del cliente.

---

## 4. Reglas de negocio

| ID | Regla |
|----|-------|
| RN-CON-01 | Un contacto debe estar obligatoriamente asociado a un cliente existente y activo en `dbo.CRM_CLIENTES`. |
| RN-CON-02 | No puede existir más de un contacto activo con el mismo email dentro del mismo cliente (`id_cliente` + `email` únicos cuando `activo = 1`). |
| RN-CON-03 | El canal de contacto preferido (`canal_preferido`) solo admite los valores: `EMAIL`, `TELEFONO`, `WHATSAPP`. |
| RN-CON-04 | La baja es siempre lógica (`activo = 0`). Ningún rol puede eliminar físicamente un registro de `dbo.CRM_CONTACTOS`. |
| RN-CON-05 | Solo el rol `ADMIN` puede ejecutar la baja lógica de un contacto. |
| RN-CON-06 | Los roles `ADMIN` y `REGULAR` pueden crear y editar contactos de clientes activos. |
| RN-CON-07 | El campo `creado_por` se asigna en el backend desde el subject del JWT; el frontend no lo envía. |
| RN-CON-08 | La `fecha_creacion` la asigna el backend en el momento del INSERT; el frontend no la envía ni puede modificarla. |
| RN-CON-09 | Un contacto inactivo no puede ser editado. Si se intenta, el backend devuelve `409 Conflict` con el mensaje: _"El contacto está inactivo y no puede ser modificado."_ |
| RN-CON-10 | Todas las altas, modificaciones y bajas quedan registradas en `BRM_CONFIG.AUD_AUDITORIA` con el usuario, la fecha y la acción realizada. |

---

## 5. Modelo de datos

### Tabla `dbo.CRM_CONTACTOS`

```sql
CREATE TABLE dbo.CRM_CONTACTOS (
    id               INT           IDENTITY(1,1)   NOT NULL,
    id_cliente       INT                           NOT NULL,
    nombre           NVARCHAR(100)                 NOT NULL,
    apellido         NVARCHAR(100)                 NOT NULL,
    cargo            NVARCHAR(150)                 NULL,
    email            NVARCHAR(255)                 NOT NULL,
    telefono         NVARCHAR(30)                  NULL,
    canal_preferido  NVARCHAR(20)                  NOT NULL  DEFAULT 'EMAIL',
    activo           BIT                           NOT NULL  DEFAULT 1,
    fecha_creacion   DATETIME2(0)                  NOT NULL  DEFAULT GETDATE(),
    creado_por       NVARCHAR(100)                 NOT NULL,

    CONSTRAINT PK_CRM_CONTACTOS       PRIMARY KEY (id),
    CONSTRAINT FK_CONTACTOS_CLIENTE   FOREIGN KEY (id_cliente)
        REFERENCES dbo.CRM_CLIENTES (id),
    CONSTRAINT CHK_CANAL_PREFERIDO    CHECK (canal_preferido IN ('EMAIL', 'TELEFONO', 'WHATSAPP')),
    CONSTRAINT UQ_EMAIL_CLIENTE_ACTIVO UNIQUE (id_cliente, email, activo)
);

CREATE INDEX IX_CRM_CONTACTOS_CLIENTE
    ON dbo.CRM_CONTACTOS (id_cliente, activo);
```

**Detalle de columnas:**

| Columna | Tipo | Nulo | Default | Descripción |
|---------|------|------|---------|-------------|
| `id` | INT IDENTITY | NO | — | PK autoincremental. |
| `id_cliente` | INT | NO | — | FK a `dbo.CRM_CLIENTES.id`. Obligatorio. |
| `nombre` | NVARCHAR(100) | NO | — | Nombre de pila del contacto. |
| `apellido` | NVARCHAR(100) | NO | — | Apellido del contacto. |
| `cargo` | NVARCHAR(150) | SI | NULL | Puesto o rol dentro del cliente (ej. "Gerente de TI"). |
| `email` | NVARCHAR(255) | NO | — | Email de contacto. Único por cliente activo. |
| `telefono` | NVARCHAR(30) | SI | NULL | Número de teléfono (incluye prefijo internacional). |
| `canal_preferido` | NVARCHAR(20) | NO | `'EMAIL'` | Canal de comunicación preferido: `EMAIL`, `TELEFONO`, `WHATSAPP`. |
| `activo` | BIT | NO | `1` | `1` = activo, `0` = baja lógica. |
| `fecha_creacion` | DATETIME2(0) | NO | `GETDATE()` | Timestamp de creación asignado por el backend. |
| `creado_por` | NVARCHAR(100) | NO | — | Username del usuario que creó el registro (del JWT). |

**Notas sobre el constraint de unicidad:**
El constraint `UQ_EMAIL_CLIENTE_ACTIVO` sobre `(id_cliente, email, activo)` permite que un email previamente dado de baja pueda reutilizarse para un nuevo contacto activo dentro del mismo cliente, sin violar la unicidad. La validación de negocio (RN-CON-02) se aplica adicionalmente en capa de servicio consultando explícitamente `activo = 1`.

---

## 6. Impacto en el sistema

### Backend

**Nuevas clases / archivos a crear:**

| Artefacto | Tipo | Ruta sugerida |
|-----------|------|---------------|
| `Contacto.java` | Entidad JPA | `com.sergio.login.contacto` |
| `ContactoRepository.java` | Repository (Spring Data) | `com.sergio.login.contacto` |
| `ContactoService.java` | Service | `com.sergio.login.contacto` |
| `ContactoController.java` | REST Controller | `com.sergio.login.contacto` |
| `ContactoDTO.java` | DTO request/response | `com.sergio.login.contacto.dto` |

**Endpoints REST nuevos:**

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| `GET` | `/api/contactos?idCliente={id}` | ADMIN, REGULAR | Listar contactos de un cliente (activos por defecto). |
| `GET` | `/api/contactos/{id}` | ADMIN, REGULAR | Obtener detalle de un contacto. |
| `POST` | `/api/contactos` | ADMIN, REGULAR | Crear nuevo contacto. |
| `PUT` | `/api/contactos/{id}` | ADMIN, REGULAR | Actualizar contacto existente. |
| `DELETE` | `/api/contactos/{id}` | ADMIN | Baja lógica (`activo = 0`). |

**Cambios en clases existentes:**

- `SecurityConfig.java`: añadir regla explícita para `DELETE /api/contactos/**` con `hasAuthority("ROLE_ADMIN")`. El resto de endpoints de contactos se cubre con `.anyRequest().authenticated()`.
- `AuditoriaService` (o equivalente): registrar eventos `CONTACTO_CREADO`, `CONTACTO_EDITADO`, `CONTACTO_BAJA` en `BRM_CONFIG.AUD_AUDITORIA`.

### Frontend

**Nuevos componentes / archivos a crear:**

| Artefacto | Tipo | Ruta sugerida |
|-----------|------|---------------|
| `ContactosComponent` | Componente lista | `src/app/contactos/` |
| `ContactoFormComponent` | Componente formulario (crear/editar) | `src/app/contactos/contacto-form/` |
| `ContactoDetalleComponent` | Componente detalle (lectura) | `src/app/contactos/contacto-detalle/` |
| `contactos.service.ts` | Servicio HTTP | `src/app/services/` |
| `contacto.model.ts` | Interfaz TypeScript | `src/app/models/` |

**Cambios en archivos existentes:**

- `app.routes.ts`: añadir rutas `/contactos` y `/contactos/:id` protegidas con `authGuard`. La ruta de baja no necesita ruta propia (es una acción dentro del listado).
- Menú de navegación (si existe): agregar enlace al módulo de Contactos visible para ambos roles.
- El botón de "Dar de baja" debe renderizarse condicionalmente mediante `*ngIf="isAdmin"` evaluado desde `AuthService`.

**UX a considerar:**

- El formulario de contactos se abre en modo modal o en vista de detalle desde la ficha del cliente.
- El listado de contactos es un sub-componente de la ficha del cliente (no una ruta independiente de primer nivel).
- Indicador visual (badge/chip) para el canal preferido: ícono de correo, teléfono o WhatsApp.

### Seguridad

- El `JwtFilter` existente ya extrae el `username` del token; `ContactoService` debe recibirlo del `SecurityContextHolder` para rellenar `creado_por`.
- El endpoint `DELETE /api/contactos/{id}` debe declararse en `SecurityConfig` con `hasAuthority("ROLE_ADMIN")`, igual que `/api/auditoria/**`.
- El frontend no debe confiar únicamente en la ocultación del botón: el backend valida el rol en cada petición.
- No se exponen IDs de otros clientes en la respuesta; el servicio filtra siempre por `id_cliente` del parámetro y valida que el cliente exista.

---

## 7. Flujo principal

**Caso: Usuario REGULAR crea un contacto nuevo**

```
1. Usuario navega a la ficha de un cliente (CRM_CLIENTES).
2. En la sección "Contactos" de la ficha, pulsa el botón "Nuevo contacto".
3. El frontend abre el formulario ContactoFormComponent en modo creación.
4. El usuario completa los campos obligatorios: nombre, apellido, email.
   - Opcionalmente: cargo, teléfono, canal_preferido.
4. El frontend valida el formulario en cliente (formato email, campos requeridos).
5. El usuario pulsa "Guardar".
6. El frontend envía POST /api/contactos con el JWT en la cabecera Authorization.
7. El JwtFilter valida el token y extrae el username.
8. ContactoService valida:
   a. Que el id_cliente exista y esté activo.
   b. Que no exista otro contacto activo con el mismo email en ese cliente.
9. Se persiste el registro en dbo.CRM_CONTACTOS con activo=1,
   fecha_creacion=NOW(), creado_por=<username del JWT>.
10. Se registra el evento CONTACTO_CREADO en BRM_CONFIG.AUD_AUDITORIA.
11. El backend devuelve 201 Created con el DTO del contacto creado.
12. El frontend cierra el formulario, recarga el listado y muestra toast de éxito.
```

---

## 8. Flujos alternativos / errores

| Escenario | Trigger | Respuesta backend | Mensaje frontend |
|-----------|---------|-------------------|------------------|
| Email duplicado en el mismo cliente | POST/PUT con email ya existente (`activo=1`) | `409 Conflict` | "Ya existe un contacto activo con ese email en este cliente." |
| Cliente inexistente o inactivo | `id_cliente` no válido | `422 Unprocessable Entity` | "El cliente asociado no existe o está inactivo." |
| Canal preferido inválido | Valor fuera de enum | `400 Bad Request` | "El canal preferido debe ser EMAIL, TELEFONO o WHATSAPP." |
| Intento de baja por REGULAR | `DELETE` con rol `REGULAR` | `403 Forbidden` | "No tiene permisos para dar de baja un contacto." |
| Intento de editar contacto inactivo | `PUT` sobre `activo=0` | `409 Conflict` | "El contacto está inactivo y no puede ser modificado." |
| Token JWT ausente o expirado | Cualquier request sin token válido | `401 Unauthorized` | Redirige al login (interceptor Angular). |
| Contacto no encontrado | `GET/PUT/DELETE` con id inexistente | `404 Not Found` | "Contacto no encontrado." |
| Error interno de servidor | Excepción no controlada | `500 Internal Server Error` | "Error inesperado. Por favor, intente nuevamente." |

---

## 9. Dependencias

| Dependencia | Tipo | Estado | Notas |
|-------------|------|--------|-------|
| `dbo.CRM_CLIENTES` | Tabla BD (FK) | Desarrollo en Sprint 2 | Los contactos no pueden crearse si la tabla de clientes no existe. Contactos depende de Clientes en el sprint. |
| `BRM_CONFIG.AUD_AUDITORIA` | Tabla BD (auditoría) | Disponible (Sprint 1) | Ya utilizada por otros módulos. |
| `JwtFilter` / `JwtUtil` | Infraestructura auth | Disponible (Sprint 1) | Se reutiliza sin cambios para extraer `username`. |
| `SecurityConfig` | Seguridad | Disponible (Sprint 1) | Requiere modificación mínima para el endpoint DELETE. |
| `authGuard` / `adminGuard` | Guards Angular | Disponibles (Sprint 1) | Se reutilizan para las nuevas rutas. |
| `AuthInterceptor` Angular | Interceptor HTTP | Disponible (Sprint 1) | Adjunta el JWT a todas las peticiones automáticamente. |

**Orden de implementación recomendado dentro del Sprint 2:**

1. Tabla `dbo.CRM_CLIENTES` (prerequisito).
2. Tabla `dbo.CRM_CONTACTOS` (script DDL).
3. Backend: entidad, repository, service, controller.
4. Frontend: servicio HTTP, modelo, componentes, rutas.

---

## 10. Estimación de complejidad

| Historia | Puntos | Complejidad | Justificación |
|----------|--------|-------------|---------------|
| HU-CON-01 Listar contactos | 3 | Baja | GET con filtro; reutiliza patrones existentes de listado. |
| HU-CON-02 Crear contacto | 5 | Media | Validaciones de negocio (unicidad email, cliente activo), auditoría, extracción del username del JWT. |
| HU-CON-03 Editar contacto | 3 | Baja-Media | Igual que creación pero sin la lógica de `creado_por`; reutiliza formulario. |
| HU-CON-04 Baja lógica | 2 | Baja | PATCH/DELETE simple; principal complejidad está en el control de acceso por rol. |
| HU-CON-05 Ver detalle | 2 | Baja | GET by id; componente de lectura. |
| **DDL + migración BD** | 2 | Baja | Script sencillo; sin datos de migración. |
| **TOTAL** | **17** | — | — |

**Complejidad global del módulo: MEDIA**

El módulo no introduce tecnologías nuevas. Su complejidad reside en la correcta gestión del control de acceso diferenciado y en las validaciones de integridad relacional (FK cliente + unicidad email).

---

## 11. Instrucciones para el desarrollador

### 11.1 Base de datos

1. Verificar que la tabla `dbo.CRM_CLIENTES` existe antes de ejecutar el DDL de contactos (FK dependency).
2. Ejecutar el script DDL de la sección 5 en la base de datos `logindb`.
3. Validar el constraint `CHK_CANAL_PREFERIDO` con un INSERT de prueba con valor inválido (debe fallar con error de constraint).
4. Validar la FK insertando un contacto con `id_cliente` inexistente (debe fallar).

### 11.2 Backend

1. Crear el paquete `com.sergio.login.contacto` (y sub-paquete `dto`).
2. La entidad `Contacto.java` debe usar `@ManyToOne` sobre `Cliente` para la FK.
3. En `ContactoService.java`, obtener el username autenticado con:
   ```java
   String username = SecurityContextHolder.getContext()
       .getAuthentication().getName();
   ```
4. Añadir en `SecurityConfig.java`:
   ```java
   .requestMatchers(HttpMethod.DELETE, "/api/contactos/**").hasAuthority("ROLE_ADMIN")
   ```
   Esta línea debe ir **antes** de `.anyRequest().authenticated()`.
5. Validar en el servicio (no solo en BD) que no exista contacto con `activo=1`, mismo `id_cliente` y mismo `email` antes de hacer el INSERT/UPDATE.
6. El endpoint DELETE no elimina el registro: ejecuta `UPDATE SET activo = 0`.
7. Registrar en auditoría usando el servicio/repositorio existente de `BRM_CONFIG.AUD_AUDITORIA`.

### 11.3 Frontend

1. Añadir en `app.routes.ts` las rutas de contactos protegidas con `authGuard`:
   ```typescript
   { path: 'clientes/:idCliente/contactos',
     component: ContactosComponent,
     canActivate: [authGuard] },
   { path: 'clientes/:idCliente/contactos/nuevo',
     component: ContactoFormComponent,
     canActivate: [authGuard] },
   { path: 'clientes/:idCliente/contactos/:id/editar',
     component: ContactoFormComponent,
     canActivate: [authGuard] },
   ```
2. El botón "Dar de baja" se condiciona con:
   ```typescript
   isAdmin(): boolean {
     return this.authService.getRol() === 'ADMIN';
   }
   ```
   Y en el template: `*ngIf="isAdmin()"`.
3. El servicio `contactos.service.ts` debe usar el `AuthInterceptor` existente (automático); no añadir cabeceras de auth manualmente.
4. Manejar el `403` del DELETE mostrando un mensaje de error, aunque el botón esté oculto (defensa en profundidad).
5. El campo `canal_preferido` se implementa como `<select>` con las opciones `EMAIL`, `TELEFONO`, `WHATSAPP` (valores exactos en mayúsculas, que son los que espera el backend y el constraint de BD).

### 11.4 Pruebas mínimas requeridas

- [ ] Crear contacto como REGULAR → éxito (201).
- [ ] Crear contacto con email duplicado (mismo cliente, activo) → error 409.
- [ ] Editar contacto como REGULAR → éxito (200).
- [ ] Dar de baja como ADMIN → éxito (200, `activo=0` en BD).
- [ ] Intentar dar de baja como REGULAR → error 403.
- [ ] Verificar registro en `BRM_CONFIG.AUD_AUDITORIA` tras cada operación de escritura.
- [ ] Verificar que el contacto dado de baja no aparece en el listado por defecto.

---

*Documento generado para el Sprint 2 del CRM login-app. Revisión y aprobación requerida del Tech Lead antes de iniciar desarrollo.*
