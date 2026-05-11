# login-app — Contexto del proyecto

## Descripción
Aplicación web de autenticación y gestión de usuarios con roles.
Repositorio: https://github.com/SergioMaldonado97/login-app

---

## Stack

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Frontend | Angular + TypeScript | 21 / 5.9 |
| Backend | Spring Boot + Java | 3.4.5 / 21 |
| Base de datos | SQL Server 2022 | localhost:1433 |
| ORM | Spring Data JPA + Hibernate | 6.6 |
| Autenticación | JWT (JJWT 0.12.6) + BCrypt | — |
| Build | Maven (backend) / npm (frontend) | — |

---

## Estructura

```
login-app/
├── login-backend/          Spring Boot REST API (puerto 8080)
│   └── src/main/java/com/sergio/login/
│       ├── AuthController.java       POST /api/auth/login
│       ├── UsuarioController.java    CRUD /api/usuarios (requiere JWT)
│       ├── ConfigController.java     GET /api/config/sesion (público)
│       ├── JwtUtil.java              genera/valida tokens HS256
│       ├── JwtFilter.java            intercepta requests y autentica
│       ├── SecurityConfig.java       rutas públicas vs protegidas
│       ├── ConfigService.java        carga BRM_CONFIG en memoria al arrancar
│       ├── Usuario.java              entidad → dbo.usuarios
│       └── CfgConfig.java            entidad → BRM_CONFIG.CFG_CONFIG
│
├── login-frontend/         Angular SPA (puerto 4200)
│   └── src/app/
│       ├── services/auth.service.ts      JWT en localStorage
│       ├── services/session.service.ts   timeout 20 min desde BD
│       ├── interceptors/auth.interceptor.ts  agrega Bearer token
│       ├── guards/auth.guard.ts          protege rutas autenticadas
│       └── guards/auth.guard.ts (adminGuard)  protege /usuarios
│
├── docs/                   Documentación ODT (auto-generada en cada commit)
│   ├── generar_docs.py
│   ├── stack_tecnologico.odt
│   ├── documentacion_clases.odt
│   └── historial_commits.odt
│
└── .claude/agents/         Agentes de desarrollo
```

---

## Base de datos

- **Servidor:** localhost:1433 — usuario: SA
- **Base de datos:** logindb
- **Tabla usuarios:** `dbo.usuarios` (id, username, password BCrypt, nombre, rol)
- **Tabla config:** `BRM_CONFIG.CFG_CONFIG` (CFG_CONFIG_VAR, CFG_CONFIG_VALOR, CFG_CONFIG_GRUPO)
- **Config actual:** SESSION_TIMEOUT_MS=1200000 (20 min), grupo SESION

---

## API REST

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | /api/auth/login | Pública | Login → JWT |
| GET | /api/config/sesion | Pública | Config grupo SESION |
| GET | /api/usuarios | JWT | Listar usuarios |
| POST | /api/usuarios | JWT | Crear usuario |
| PUT | /api/usuarios/{id} | JWT | Actualizar usuario |
| DELETE | /api/usuarios/{id} | JWT | Eliminar usuario |

---

## Seguridad

- Contraseñas con BCrypt (factor 10) — nunca texto plano
- JWT HS256, expira en 24h
- Spring Security stateless — sin HttpSession
- Rutas públicas: `/api/auth/**`, `/api/config/**`
- Todo lo demás requiere `Authorization: Bearer <token>`

---

## Comandos útiles

```bash
# Arrancar backend
cd login-backend && mvn spring-boot:run

# Arrancar frontend
cd login-frontend && npm start

# Correr tests backend
cd login-backend && mvn test

# Compilar backend sin tests
cd login-backend && mvn package -DskipTests

# Regenerar documentación ODT manualmente
python3 docs/generar_docs.py

# Conectar a SQL Server
/opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P 'Admin1234!' -C -d logindb
```

---

## Convenciones

- **Java:** clases en español cuando son de dominio (ej. `Usuario`), infraestructura en inglés (`JwtFilter`, `SecurityConfig`)
- **Angular:** servicios con sufijo `Service`, guards con sufijo `Guard`, interceptors con sufijo `Interceptor`
- **Git:** commits en español, descriptivos del "qué" y "por qué"
- **Contraseñas:** siempre hashear con `passwordEncoder.encode()` antes de persistir
- **Nuevos parámetros de config:** insertar en `BRM_CONFIG.CFG_CONFIG`, nunca hardcodear en el código

---

## Roles de usuario

| Rol | Acceso |
|-----|--------|
| ADMIN | Dashboard + gestión de usuarios (/usuarios) |
| REGULAR | Solo dashboard |
