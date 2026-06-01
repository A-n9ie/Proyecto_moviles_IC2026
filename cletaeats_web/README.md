# CletaEats Web — Panel de Administración

Panel de administración web para la plataforma CletaEats. Permite gestionar todos los recursos del negocio: restaurantes, combos, productos, clientes, repartidores y pedidos. Construido con **React 19**, **TypeScript** y **Vite**, consumiendo la API REST del backend.

---

## Tecnologías

| Herramienta | Versión | Uso |
|---|---|---|
| React | 19.x | Framework UI |
| TypeScript | ~6.0 | Tipado estático |
| Vite | 8.x | Bundler y servidor de desarrollo |
| React Router v7 | 7.x | Enrutamiento SPA |
| TanStack Query (React Query) | 5.x | Caché y sincronización de datos del servidor |
| Axios | 1.x | Cliente HTTP |
| React Hot Toast | 2.x | Notificaciones tipo toast |
| SweetAlert2 | 11.x | Diálogos de confirmación |
| React Leaflet | 5.x | Mapa interactivo para coordenadas de restaurantes |
| Tailwind CSS | 4.x | Utilidades CSS |
| React Select | 5.x | Selects multi-opción para categorías |

---

## Estructura del proyecto

```
cletaeats_web/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
│
└── src/
    ├── main.tsx                        # Punto de entrada
    ├── App.tsx
    │
    ├── app/
    │   ├── layouts/MainLayout.tsx      # Sidebar + contenido principal
    │   ├── providers/AppProviders.tsx  # React Query, Auth, Router
    │   └── routes/
    │       ├── AppRouter.tsx           # Definición de rutas
    │       └── ProtectedRoute.tsx      # Guard de autenticación
    │
    ├── pages/
    │   ├── auth/LoginPage.tsx
    │   ├── dashboard/DashboardPage.tsx
    │   ├── restaurantes/
    │   │   ├── RestaurantesPage.tsx
    │   │   ├── RestauranteForm.tsx     # Formulario con mapa de coordenadas
    │   │   ├── CategoriasTab.tsx
    │   │   ├── ProductosTab.tsx
    │   │   └── MapaPicker.tsx          # Selector de ubicación con Leaflet
    │   ├── combos/
    │   │   ├── CombosPage.tsx
    │   │   └── ComboForm.tsx
    │   ├── clientes/
    │   │   ├── ClientesPage.tsx
    │   │   └── ClienteForm.tsx
    │   ├── repartidores/
    │   │   ├── RepartidoresPage.tsx
    │   │   └── RepartidorForm.tsx
    │   └── pedidos/PedidosPage.tsx
    │
    ├── components/
    │   ├── common/
    │   │   ├── buttons/Button.tsx
    │   │   ├── cards/               # StatsCard, DashboardSection, ComboPreviewCard
    │   │   ├── inputs/TextInput.tsx
    │   │   ├── loaders/Spinner.tsx
    │   │   ├── modal/Modal.tsx
    │   │   ├── select/MultiSelect.tsx
    │   │   ├── table/               # DataTable, StatusBadge, TableEmpty, TableLoader
    │   │   ├── tabs/Tabs.tsx
    │   │   └── upload/ImageUpload.tsx
    │   └── ui/
    │       ├── pageHeader/PageHeader.tsx
    │       └── sidebar/Sidebar.tsx
    │
    ├── hooks/                          # Custom hooks con React Query
    │   ├── useAuth.ts
    │   ├── useRestaurantes.ts
    │   ├── useCombos.ts
    │   ├── useClientes.ts
    │   ├── useRepartidores.ts
    │   ├── usePedidos.ts
    │   ├── useCategorias.ts
    │   ├── useProductos.ts
    │   └── useDashboard.ts
    │
    ├── services/
    │   ├── api/
    │   │   ├── axiosClient.ts          # Instancia Axios con interceptores JWT
    │   │   └── tokenStorage.ts         # Persistencia del token en localStorage
    │   ├── auth/authService.ts
    │   ├── restaurantes/restaurantesService.ts
    │   ├── combos/combosService.ts
    │   ├── clientes/clientesService.ts
    │   ├── repartidores/repartidoresService.ts
    │   ├── pedidos/pedidosService.ts
    │   ├── categorias/categoriasService.ts
    │   ├── productos/productosService.ts
    │   ├── dashboard/dashboardService.ts
    │   └── ui/
    │       ├── notificationService.ts  # Wrapper de react-hot-toast
    │       └── confirmService.ts       # Wrapper de SweetAlert2
    │
    ├── context/
    │   ├── AuthContext.ts
    │   └── AuthProvider.tsx
    │
    ├── types/                          # Interfaces TypeScript por entidad
    ├── constants/
    │   ├── api.ts                      # URL base de la API
    │   └── sidebarItems.ts
    ├── lib/react-query/
    │   ├── queryClient.ts
    │   ├── queryKeys.ts
    │   └── createEntityHooks.ts        # Factory de hooks CRUD genéricos
    └── utils/
        ├── formatDate.ts
        └── formatNumber.ts
```

---

## Funcionalidades del panel

### Dashboard
- Estadísticas generales: total de restaurantes, clientes, repartidores y pedidos activos.
- Acceso rápido a las secciones principales.

### Restaurantes
- Listado con filtro por categoría.
- Crear y editar restaurante con: nombre, cédula jurídica, dirección, categorías (multi-select), estado activo/inactivo.
- **Selector de coordenadas con mapa Leaflet** — hacer clic en el mapa establece latitud y longitud automáticamente.
- Gestión de productos/ingredientes del restaurante desde pestañas dentro del formulario.

### Combos
- Listado de combos por restaurante.
- Crear y editar combo: número, nombre, descripción, precio, productos incluidos.
- Vista previa del combo con sus ingredientes.

### Clientes
- Listado completo de clientes registrados.
- Activar/desactivar cuenta de cliente.

### Repartidores
- Listado con estado y amonestaciones.
- Crear repartidor con datos completos: cédula, teléfono, dirección, tarjeta de pago.
- Editar estado y amonestaciones.

### Pedidos
- Vista de todos los pedidos del sistema con estado actual.
- Filtrado por estado (pendiente, en preparación, en camino, entregado, cancelado).

---

## Instalación y ejecución local

### Requisitos
- Node.js 18+
- npm 9+

### Pasos

```bash
cd cletaeats_web

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev
```

La aplicación estará en `http://localhost:5173`.

### Configuración de la URL de la API

**Archivo:** `src/constants/api.ts`

```typescript
// Desarrollo local
export const API_URL = 'http://localhost:8000'

// Producción (cambiar antes de hacer build)
export const API_URL = 'https://proyecto-moviles-ic2026.onrender.com'
```

> Para un entorno de producción real, mover esto a variables de entorno con `.env`:
> ```
> VITE_API_URL=https://proyecto-moviles-ic2026.onrender.com
> ```
> Y en `api.ts`: `export const API_URL = import.meta.env.VITE_API_URL`

---

## Build para producción

```bash
npm run build
```

Genera la carpeta `dist/` con los archivos estáticos listos para desplegar en cualquier servicio de hosting estático (Netlify, Vercel, GitHub Pages, Render Static Site, etc.).

```bash
# Vista previa del build de producción localmente
npm run preview
```

---

## Credenciales de acceso

El panel web solo acepta usuarios con rol `ADMIN`. La API rechaza con HTTP 403 si se intenta iniciar sesión con un cliente o repartidor.

| Campo | Valor |
|---|---|
| Email | `admin@cletaeats.com` |
| Contraseña | `admin123` |

---

## Notas de diseño

- Los estilos siguen la paleta de colores de CletaEats (naranja `#FF6B35`, grises oscuros) para mantener coherencia visual con la app móvil.
- Los formularios usan validación en cliente antes de enviar al backend.
- React Query maneja el caché automáticamente: al crear, editar o eliminar un registro se invalida el query correspondiente y la lista se actualiza sola.
- El interceptor de Axios en `axiosClient.ts` agrega el header `Authorization: Bearer TOKEN` automáticamente a todas las peticiones autenticadas y redirige al login si recibe un 401.
