# 🍔 CletaEats — Sistema de Delivery de Comida

## Integrantes del equipo:
- Angie Franciny Pérez Arauz
- Mariana Villalobos Ramírez
- Iván Espinoza Mora

CletaEats es una plataforma de delivery de comida rápida desarrollada como proyecto académico para el curso de Programación Móvil (IC-2026). El sistema está compuesto por tres componentes principales que trabajan juntos sobre una arquitectura cliente-servidor:


| Componente | Tecnología | Descripción |
|---|---|---|
| **Backend** | Python / FastAPI | API REST con autenticación JWT y base de datos SQLite |
| **Web (Admin)** | React / TypeScript / Vite | Panel de administración para gestión del negocio |
| **Mobile** | Android / Kotlin / Jetpack Compose | App para clientes y repartidores |

---

## Arquitectura general

```
┌─────────────────────┐     ┌─────────────────────┐
│   Web (Admin Panel) │     │   Mobile App         │
│   React + Vite      │     │   Kotlin + Compose   │
│   Puerto 5173       │     │   Android 8+         │
└──────────┬──────────┘     └──────────┬───────────┘
           │                           │
           │        REST / JWT         │
           └──────────────┬────────────┘
                          │
              ┌───────────▼───────────┐
              │   Backend API         │
              │   FastAPI + SQLite    │
              │   Puerto 8000         │
              │   Render.com (prod)   │
              └───────────────────────┘
                          │
              ┌───────────▼───────────┐
              │   Firebase Firestore  │
              │   (modo Cloud mobile) │
              └───────────────────────┘
```

**Principio de diseño:** La aplicación web contiene todas las funcionalidades administrativas (CRUD completo del negocio, reportes). La app móvil es un subconjunto liviano orientado al consumo: explorar restaurantes, hacer pedidos y realizar entregas.

---

## Roles del sistema

| Rol | Plataforma | Funciones principales |
|---|---|---|
| `ADMIN` | Web | Gestión total: restaurantes, combos, clientes, repartidores, pedidos |
| `CLIENTE` | Mobile | Explorar restaurantes, hacer pedidos, ver historial, pagar |
| `REPARTIDOR` | Mobile | Ver pedidos asignados, actualizar estado de entrega |

---

## Repositorio

```
cletaeats/
├── cletaeats_backend/    # API REST Python/FastAPI
├── cletaeats_mobile/     # App Android Kotlin
└── cletaeats_web/        # Panel admin React/TypeScript
```

---

## Usuarios de prueba

| Rol | Email | Contraseña |
|---|---|---|
| Admin (web) | `admin@cletaeats.com` | `admin123` |
| Cliente (mobile) | `cliente@test.com` | `123456` |
| Repartidor (mobile) | `repartidor@test.com` | `123456` |

---

## URL de producción

- **API:** `https://proyecto-moviles-ic2026.onrender.com`
- **Documentación interactiva:** `https://proyecto-moviles-ic2026.onrender.com/docs`

---

## Inicio rápido (desarrollo local)

```bash
# 1. Backend
cd cletaeats_backend
pip install -r requirements.txt
python db_init.py
uvicorn app:app --reload --port 8000

# 2. Web (nueva terminal)
cd cletaeats_web
npm install
npm run dev

# 3. Mobile
# Abrir cletaeats_mobile/ en Android Studio y ejecutar en emulador o dispositivo
```

Ver los README individuales de cada componente para instrucciones detalladas.
