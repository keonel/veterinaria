# Veterinaria San Marcos – Frontend (React + Vite)

Tienda online, citas veterinarias y panel de administración. Consume la API REST del backend
(Spring Boot) a través del API Gateway.

## Requisitos

- Node.js **20.19+ o 22.12+** (recomendado Node 24).
- El backend iniciado en `http://localhost:8080`.

## Ejecutar

```bash
cp .env.example .env     # VITE_API_URL=http://localhost:8080/api/v1
npm install
npm run dev              # http://localhost:5173
```

Abrir siempre con `localhost` (no `127.0.0.1`) para que la cookie de sesión funcione.

## Scripts

| Comando | Qué hace |
|---|---|
| `npm run dev` | Servidor de desarrollo |
| `npm run build` | Compila para producción en `dist/` |
| `npm run test` | Pruebas con Vitest |
| `npm run lint` | ESLint |

## Integración con el backend

| Archivo | Responsabilidad |
|---|---|
| `src/services/api.js` | Cliente HTTP único: URL del Gateway, cabecera `Authorization`, renovación automática del token |
| `src/context/AuthContext.jsx` | Sesión: login, logout y recuperación al recargar la página |
| `src/context/TiendaContext.jsx` | Productos desde la API, carrito y compra (`POST /pedidos`) |
| `src/components/RutaProtegida.jsx` | Restricción de pantallas por rol |
| `src/hooks/useRecurso.js` | Carga de listas desde la API para el panel |

El access token (JWT) se mantiene solo en memoria. El refresh token viaja en una cookie HttpOnly
que administra el backend. En `localStorage` solo quedan el carrito y una marca de sesión sin datos sensibles.

## Roles

| Rol | Acceso |
|---|---|
| CLIENTE | Tienda, Mis Compras, Solicitar Cita, Mis Citas, Mi Perfil |
| VENDEDOR | Panel: Productos y Pedidos (solo lectura) |
| ADMIN | Panel completo |
