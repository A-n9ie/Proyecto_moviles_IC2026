# CletaEats Mobile

Aplicación Android para clientes y repartidores de la plataforma CletaEats. Desarrollada en **Kotlin** con **Jetpack Compose**, siguiendo arquitectura MVVM y el patrón Repository. Soporta tres modos de almacenamiento de datos seleccionables en tiempo de ejecución.

---

## Tecnologías y dependencias

| Librería | Versión | Uso |
|---|---|---|
| Kotlin | 2.3.21 | Lenguaje principal |
| Jetpack Compose BOM | 2026.05.00 | UI declarativa |
| Navigation Compose | 2.9.8 | Navegación entre pantallas |
| ViewModel + Lifecycle | 2.10.0 / 2.8.4 | Gestión de estado |
| Coroutines | 1.8.1 | Asincronía |
| Retrofit | 2.11.0 | Cliente HTTP REST |
| OkHttp Logging | 4.12.0 | Interceptor de logs HTTP |
| Gson | 2.10.1 | Serialización JSON |
| Room | 2.7.1 | Base de datos local SQLite |
| KSP | 2.3.21-1.0.31 | Procesador de anotaciones para Room |
| Firebase BOM | 33.7.0 | Firebase SDK |
| Firebase Firestore KTX | — | Base de datos cloud |
| Google Maps Compose | 4.3.3 | Mapa de restaurantes |
| Play Services Location | 21.3.0 | GPS y ubicación en tiempo real |
| Material3 | — | Componentes de diseño |
| Material Icons Extended | — | Iconos vectoriales |

---

## Arquitectura

La app sigue **MVVM (Model-View-ViewModel)** con separación por capas:

```
UI Layer (Compose Screens)
    │  observa StateFlow
    ▼
ViewModel Layer
    │  llama
    ▼
Repository Layer (interfaces)
    │  implementado por
    ▼
Data Sources
    ├── Remote  (Retrofit → API REST)
    ├── Local   (Room → SQLite local)
    └── Cloud   (Firebase Firestore)
```

### Estructura de paquetes

```
com.example.cletaeats_mobile/
├── CletaEatsApplication.kt         # Application, inicializa AppContainer
├── MainActivity.kt
├── AppContainer.kt                 # Inyección de dependencias manual (service locator)
│
├── data/
│   ├── local/
│   │   ├── DataMode.kt             # Enum: API_REMOTA | LOCAL_SQLITE | CLOUD
│   │   ├── SessionManager.kt       # SharedPreferences: token, modo, perfil
│   │   └── db/
│   │       ├── CletaEatsDatabase.kt    # Room Database (singleton)
│   │       ├── RestauranteDao.kt       # DAO con queries SQL
│   │       └── RestauranteEntity.kt    # Entidad Room
│   ├── remote/
│   │   ├── RetrofitClient.kt       # Configuración Retrofit + OkHttp
│   │   ├── AuthApiService.kt
│   │   ├── RestauranteApiService.kt
│   │   ├── ComboApiService.kt
│   │   ├── PedidoApiService.kt
│   │   └── TarjetaApiService.kt
│   ├── repository/
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── RestauranteRepositoryImpl.kt    # Fuente: API REST
│   │   ├── RestauranteLocalRepositoryImpl.kt  # Fuente: Room/SQLite
│   │   ├── RestauranteCloudRepositoryImpl.kt  # Fuente: Firestore
│   │   ├── ComboRepositoryImpl.kt
│   │   ├── PedidoRepositoryImpl.kt
│   │   └── TarjetaRepositoryImpl.kt
│   └── sync/
│       └── SyncManager.kt          # Sincronización Cloud ↔ SQLite
│
├── domain/
│   ├── Result.kt                   # sealed class Success / Error
│   ├── interfaces/                 # Contratos de repositorios
│   └── model/                      # Modelos de dominio
│       ├── Usuario.kt, Restaurante.kt, Combo.kt
│       ├── Producto.kt, Pedido.kt, Tarjeta.kt
│       ├── ItemCarrito.kt, FacturaData.kt
│       └── RestauranteConCombos.kt
│
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── RestauranteViewModel.kt
│   ├── ComboViewModel.kt
│   ├── CarritoViewModel.kt
│   ├── PedidosClienteViewModel.kt
│   ├── PedidosRepartidorViewModel.kt
│   └── TarjetaViewModel.kt
│
└── ui/
    ├── auth/
    │   ├── LoginScreen.kt
    │   ├── RegisterScreen.kt
    │   └── SeleccionModoScreen.kt      # Selector de modo de datos (Lab 5)
    ├── cliente/
    │   ├── RestaurantesScreen.kt       # Lista con filtro por categoría
    │   ├── CombosScreen.kt             # Combos con carrito y GPS
    │   ├── CarritoScreen.kt            # Resumen, tarjeta, confirmación
    │   ├── FacturaScreen.kt            # Recibo post-pago
    │   ├── MisPedidosScreen.kt         # Historial de pedidos
    │   └── MapaRestaurantesScreen.kt   # Mapa con pines de restaurantes
    ├── repartidor/
    │   └── PedidosRepartidorScreen.kt
    ├── components/
    │   ├── CletaButton.kt
    │   ├── CletaTextField.kt
    │   ├── CletaTopBar.kt
    │   └── ErrorBanner.kt
    ├── navigation/
    │   ├── AppRoutes.kt
    │   └── AppNavigation.kt
    ├── theme/
    │   ├── Color.kt                    # Paleta: CletaNaranja, grises, etc.
    │   ├── Theme.kt
    │   └── Type.kt
    └── utils/
        └── Formatters.kt              # toCRC() — formato moneda costarricense
```

---

## Modos de almacenamiento (Lab 5)

Después del login, el usuario elige cómo quiere trabajar:

| Modo | Fuente de datos | Requiere internet |
|---|---|---|
| **API Remota** | REST API en Render.com | Sí |
| **Local (SQLite)** | Room Database en el dispositivo | No |
| **Cloud (Firebase)** | Firestore en tiempo real | Sí |

La selección se guarda en `SharedPreferences` vía `SessionManager` y persiste hasta que el usuario cierra sesión. El `AppContainer` crea la implementación correcta del repositorio según el modo activo.

---

## Flujo de pantallas por rol

### CLIENTE
```
Login → SeleccionModo → Restaurantes (con filtros) → Combos (con GPS)
     → Carrito → Factura
     → Mis Pedidos
     → Mapa de Restaurantes
```

### REPARTIDOR
```
Login → SeleccionModo → Pedidos Asignados → Actualizar estado de entrega
```

---

## Permisos requeridos (AndroidManifest)

| Permiso | Motivo |
|---|---|
| `ACCESS_FINE_LOCATION` | Calcular distancia exacta al restaurante para el costo de envío |
| `ACCESS_COARSE_LOCATION` | Fallback de ubicación aproximada |
| `INTERNET` | Comunicación con API y Firebase |

---

## Configuración antes de compilar

### 1. `google-services.json`
El archivo ya está en `app/google-services.json`. Para usar tu propio proyecto Firebase:
1. Ir a [Firebase Console](https://console.firebase.google.com).
2. Crear proyecto → Agregar app Android con el package `com.example.cletaeats_mobile`.
3. Descargar `google-services.json` y reemplazar el existente.
4. En Firestore, crear la colección `restaurantes` con los mismos campos que `RestauranteEntity`.

### 2. URL de la API
**Archivo:** `data/remote/RetrofitClient.kt`

```kotlin
private const val BASE_URL = "https://proyecto-moviles-ic2026.onrender.com/"
```

Cambiar si el backend corre localmente:
```kotlin
// Emulador Android: usar 10.0.2.2 para acceder a localhost del PC
private const val BASE_URL = "http://10.0.2.2:8000/"
```

### 3. KSP (procesador de Room)
Verificar que `app/build.gradle.kts` tiene:
```kotlin
plugins {
    alias(libs.plugins.ksp)   // requerido para Room
}

dependencies {
    ksp(libs.androidx.room.compiler)   // NO usar annotationProcessor
}
```

---

## Instalación y ejecución

### Requisitos
- Android Studio Ladybug (2024.2) o superior
- JDK 17+
- Android SDK API 26+ (mínimo), target API 35
- Dispositivo físico o emulador con Google Play Services (requerido para GPS)

### Pasos

1. Abrir Android Studio → `File > Open` → seleccionar la carpeta `cletaeats_mobile/`.
2. Esperar a que Gradle sincronice las dependencias.
3. Seleccionar un dispositivo/emulador en el menú de ejecución.
4. Hacer **Build → Clean Project** y luego **Run 'app'** (▶).

> Si Room lanza `CletaEatsDatabase_Impl does not exist`, hacer **Build → Clean Project** + **Rebuild Project** para que KSP regenere las clases.

---

## Usuarios de prueba

| Rol | Email | Contraseña |
|---|---|---|
| Cliente | `cliente@test.com` | `123456` |
| Repartidor | `repartidor@test.com` | `123456` |

---

## Notas importantes

- **GPS frío:** Si el dispositivo no ha usado GPS recientemente, la distancia al restaurante se calcula primero con un valor estimado (5 km) y se actualiza automáticamente en los siguientes 10 segundos cuando el GPS obtiene una posición real.
- **Render cold start:** El servidor gratuito tarda ~30–60 segundos en responder si estuvo inactivo. Si el login falla al primer intento, volver a intentarlo.
- **Modo Cloud:** Requiere que el proyecto Firebase esté activo y que la colección `restaurantes` exista en Firestore con datos.
