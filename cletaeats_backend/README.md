# CletaEats Backend

API REST para la plataforma CletaEats, construida con **FastAPI** y **SQLite** bajo una arquitectura limpia por capas. Desplegada en producción sobre **Render.com**.

---

## Tecnologías

| Herramienta | Versión | Uso |
|---|---|---|
| Python | 3.11 | Lenguaje principal |
| FastAPI | ≥ 0.111 | Framework web / API REST |
| Uvicorn | ≥ 0.29 | Servidor ASGI |
| SQLAlchemy | ≥ 2.0 | ORM y manejo de base de datos |
| python-jose | ≥ 3.3 | Generación y validación de tokens JWT |
| python-multipart | ≥ 0.0.9 | Soporte para formularios multipart |
| SQLite | — | Base de datos (archivo local) |

---

## Arquitectura

```
cletaeats_backend/
├── app.py                          # Punto de entrada, registro de routers
├── db_init.py                      # Script de inicialización de la base de datos
├── requirements.txt
├── render.yaml                     # Configuración de despliegue en Render
│
├── core/
│   ├── entities/                   # Modelos de dominio (entidades del negocio)
│   │   ├── usuario.py
│   │   ├── cliente.py
│   │   ├── repartidor.py
│   │   ├── restaurante.py
│   │   ├── combo.py
│   │   ├── producto.py
│   │   ├── pedido.py
│   │   ├── detalle_pedido.py
│   │   ├── tarjeta_cliente.py
│   │   └── categoria.py
│   └── use_cases/                  # Lógica de negocio
│       ├── auth_use_cases.py
│       ├── combo_use_cases.py
│       ├── pedido_use_cases.py
│       └── restaurante_use_cases.py
│
├── data/
│   ├── database/
│   │   ├── db_connection.py        # Conexión SQLAlchemy
│   │   └── tables.py               # Definición de tablas DDL
│   ├── repositories/               # Implementaciones de acceso a datos
│   │   ├── usuario_repository.py
│   │   ├── cliente_repository.py
│   │   ├── repartidor_repository.py
│   │   ├── restaurante_repository.py
│   │   ├── combo_repository.py
│   │   ├── producto_repository.py
│   │   ├── pedido_repository.py
│   │   ├── categoria_repository.py
│   │   └── tarjeta_cliente_repository.py
│   └── utils/
│       └── mapper_utils.py
│
├── domain/
│   └── interfaces/                 # Contratos de repositorios
│
├── middleware/
│   └── auth_middleware.py          # Guards JWT por rol
│
├── routes/
│   ├── auth_routes.py              # /auth — login, registro
│   ├── admin_routes.py             # /admin — gestión completa
│   ├── public_routes.py            # /restaurantes, /combos, /categorias
│   ├── cliente_routes.py           # /cliente — tarjetas, pedidos
│   └── repartidor_routes.py        # /repartidor — pedidos asignados
│
└── services/
    ├── jwt_service.py
    ├── hash_service.py
    └── session_service.py
```

---

## Modelo de datos

```
USUARIO ──< CLIENTE ──< TARJETA_CLIENTE
        ──< REPARTIDOR

RESTAURANTE ──< COMBO ──< COMBO_PRODUCTO >── PRODUCTO
            ──< RESTAURANTE_CATEGORIA >── CATEGORIA
            ──< PRODUCTO

PEDIDO ──< DETALLE_PEDIDO >── COMBO
PEDIDO >── CLIENTE
PEDIDO >── RESTAURANTE
PEDIDO >── REPARTIDOR (nullable)
```

**Estados del pedido:** `0` Pendiente → `1` En preparación → `2` En camino → `3` Entregado → `4` Cancelado

---

## Endpoints principales

### Autenticación — `/auth`

| Método | Ruta | Descripción | Rol requerido |
|---|---|---|---|
| POST | `/auth/login` | Iniciar sesión | — |
| POST | `/auth/registro/cliente` | Registrar nuevo cliente | — |
| POST | `/auth/registro/repartidor` | Registrar repartidor | — |
| POST | `/auth/logout` | Cerrar sesión (instrucción al cliente) | — |

**Body de login:**
```json
{
  "email": "cliente@test.com",
  "password": "123456",
  "platform": "MOBILE"
}
```
> `platform` puede ser `MOBILE` (acepta CLIENTE y REPARTIDOR) o `WEB` (acepta ADMIN).

**Respuesta de login:**
```json
{
  "token": "eyJ...",
  "id_usuario": 1,
  "email": "cliente@test.com",
  "rol": "CLIENTE",
  "nombre": "Juan Cliente",
  "id_perfil": 1
}
```

### Públicos (requieren JWT pero cualquier rol)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/restaurantes` | Listar restaurantes activos, filtrable por `?categoria=X` |
| GET | `/combos?restaurante={id}` | Combos de un restaurante con sus productos |
| GET | `/categorias` | Listar categorías de comida |

### Cliente — `/cliente` (rol CLIENTE)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/cliente/tarjetas` | Listar tarjetas del cliente autenticado |
| POST | `/cliente/tarjetas` | Agregar nueva tarjeta |
| DELETE | `/cliente/tarjetas/{id}` | Eliminar tarjeta |
| POST | `/cliente/pedidos` | Crear pedido con detalle de combos |
| GET | `/cliente/pedidos` | Historial de pedidos del cliente |

### Repartidor — `/repartidor` (rol REPARTIDOR)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/repartidor/pedidos` | Pedidos asignados o pendientes de asignación |
| PUT | `/repartidor/pedidos/{id}/estado` | Actualizar estado del pedido |

### Admin — `/admin` (rol ADMIN)

| Método | Ruta | Descripción |
|---|---|---|
| GET/POST | `/admin/restaurantes` | Listar y crear restaurantes |
| PUT/DELETE | `/admin/restaurantes/{id}` | Editar y eliminar restaurante |
| GET/POST | `/admin/combos` | Gestión de combos |
| PUT/DELETE | `/admin/combos/{id}` | Editar y eliminar combo |
| GET/POST | `/admin/productos` | Gestión de productos/ingredientes |
| GET | `/admin/clientes` | Listar clientes |
| PUT | `/admin/clientes/{id}/estado` | Activar/desactivar cliente |
| GET/POST | `/admin/repartidores` | Listar y gestionar repartidores |
| GET | `/admin/pedidos` | Todos los pedidos del sistema |
| GET | `/admin/categorias` | Listar categorías |
| POST | `/admin/categorias` | Crear categoría |

---

## Seguridad

- Autenticación con **JWT** (HS256), expiración configurable.
- Contraseñas almacenadas con **bcrypt** mediante `hash_service.py`.
- Separación de plataformas: el backend rechaza login de `ADMIN` desde `MOBILE` y viceversa con un HTTP 403.
- CORS habilitado para todos los orígenes en desarrollo (`allow_origins=["*"]`).

---

## Instalación local

### Requisitos
- Python 3.11+
- pip

### Pasos

```bash
# Clonar/obtener el proyecto
cd cletaeats_backend

# Instalar dependencias
pip install -r requirements.txt

# Crear entorno virtual
python -m venv venv

# Activar entorno virtual

# Windows
venv\Scripts\activate

# Linux / Mac
source venv/bin/activate

# Instalar dependencias
pip install -r requirements.txt

# Crear la BD nueva (solo una vez)
python db_init.py

# Levantar el servidor
uvicorn app:app --reload --port 8000
```

La API estará disponible en `http://localhost:8000`.
La documentación interactiva (Swagger UI) en `http://localhost:8000/docs`.

---

## Despliegue en Render

El archivo `render.yaml` define el servicio completo:

```yaml
services:
  - type: web
    name: cletaeats-backend
    runtime: python
    rootDir: cletaeats_backend
    buildCommand: pip install -r requirements.txt && python db_init.py
    startCommand: uvicorn app:app --host 0.0.0.0 --port $PORT
    envVars:
      - key: PYTHON_VERSION
        value: 3.11.0
```

### Pasos para desplegar en Render

1. Crear cuenta en [render.com](https://render.com).
2. Conectar el repositorio de GitHub/GitLab.
3. Crear un **New Web Service** y apuntar al directorio `cletaeats_backend/`.
4. Render detecta `render.yaml` automáticamente y aplica la configuración.
5. En el primer deploy, `db_init.py` crea e inicializa la base de datos.

> **Nota sobre persistencia:** Render en plan gratuito usa almacenamiento efímero. La base de datos SQLite se reinicia con cada redeploy. Para persistencia real en producción, migrar a PostgreSQL agregando `databases:` en `render.yaml` y ajustando `db_connection.py`.

### Variables de entorno recomendadas en Render

| Variable | Descripción |
|---|---|
| `JWT_SECRET` | Clave secreta para firmar tokens (agregar en Settings > Environment) |
| `PYTHON_VERSION` | `3.11.0` (ya definida en render.yaml) |

### URL de producción actual

```
https://proyecto-moviles-ic2026.onrender.com
```

> El servicio gratuito de Render se "duerme" tras 15 minutos de inactividad. La primera petición tras inactividad puede tardar 30–60 segundos (cold start).

---

## Ejecución de pruebas manuales (curl)

```bash
# Login
curl -X POST https://proyecto-moviles-ic2026.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@test.com","password":"123456","platform":"MOBILE"}'

# Listar restaurantes (reemplazar TOKEN)
curl https://proyecto-moviles-ic2026.onrender.com/restaurantes \
  -H "Authorization: Bearer TOKEN"

# Crear pedido
curl -X POST https://proyecto-moviles-ic2026.onrender.com/cliente/pedidos \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"restaurante_id":1,"distancia_km":3.5,"items":[{"combo_id":1,"cantidad":2,"configuracion":"{}"}]}'
```
