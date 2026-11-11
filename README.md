# Veterinaria San Marcos – Evaluación 3 (backend Spring Boot + React)

| Aplicación | Puerto | Responsabilidad | Base propia |
|---|---:|---|---|
| gateway | 8080 | Única entrada del frontend: rutas, CORS y Swagger agregado | Sin BD |
| usuarios | 8081 | Registro, login, JWT, refresh y CRUD de usuarios | sanmarcos_usuarios |
| catalogo | 8082 | CRUD de productos, precios y stock | sanmarcos_catalogo |
| pedidos | 8083 | Compras, historial y estados | sanmarcos_pedidos |
| citas | 8084 | Citas veterinarias y mensajes de contacto | sanmarcos_citas |

`backend/common` es una biblioteca compartida (errores, seguridad JWT, OpenAPI), no un servicio.
Cada servicio tiene las capas `controller`, `service`, `repository`, `model`, `dto`, `config` y `exception`.

## Requisitos

- JDK 17 o superior (probado con JDK 21). `JAVA_HOME` debe apuntar a ese JDK.
- Node.js **20.19+ o 22.12+** (recomendado Node 24). Con Node 20.17 Vite 8 no funciona.
- Docker Desktop solo si se quiere usar `docker compose`.

## Inicio rápido (Windows): doble clic en INICIAR.bat

Descarga el proyecto, abre la carpeta y haz doble clic en **INICIAR.bat**. Verifica Java 17+ y Node 20.19+ (los instala con winget si faltan), compila el backend, instala el frontend, inicia todo y abre `http://localhost:5173`. La primera vez demora unos minutos. Para apagar todo: **DETENER.bat**.

## Ejecutar local sin Docker (H2)

```powershell
.\scripts\iniciar-local.ps1        # compila si hace falta e inicia los 5 servicios
cd frontend
npm install
npm run dev                        # http://localhost:5173
```

Detener: `.\scripts\detener-local.ps1`. Swagger: <http://localhost:8080/swagger-ui.html>.
Usa siempre `localhost` (no 127.0.0.1) para que la cookie de sesión funcione.

## Ejecutar con Docker (MySQL)

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Frontend en <http://localhost:5173>, API en <http://localhost:8080>.

## Cuentas iniciales

| Correo | Contraseña | Rol |
|---|---|---|
| admin@sanmarcos.cl | Admin1234 | ADMIN |
| vendedor@sanmarcos.cl | Vende1234 | VENDEDOR |
| cliente@gmail.com | Cliente123 | CLIENTE |

## Pruebas

```powershell
cd backend ; .\mvnw.cmd verify     # 37 pruebas de integración
cd frontend ; npm run test         # 43 pruebas
```

## Roles

- **ADMIN**: acceso total.
- **VENDEDOR**: solo ve lista y detalle de productos y de pedidos.
- **CLIENTE**: tienda, sus compras, sus citas y su perfil.

## Antes de subir a GitHub

No subir `target/`, `node_modules/`, `.run/`, `data/` ni `.env` (ya están en `.gitignore`).
Las capturas para el manual están en `docs/capturas/`.
