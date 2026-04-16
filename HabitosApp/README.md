# Hábitos App - Frontend Android

## Descripción

Esta es la aplicación móvil Android para el seguimiento de hábitos, desarrollada en Kotlin con Jetpack Compose. Proporciona una interfaz intuitiva para que los usuarios puedan gestionar sus hábitos diarios, conectándose al backend API para persistir los datos.

## Funcionalidades

- **Interfaz moderna**: UI construida con Jetpack Compose
- **Gestión de hábitos**: Crear, editar, eliminar y marcar hábitos como completados
- **Autenticación**: Registro e inicio de sesión de usuarios
- **Sincronización**: Conexión con el backend para almacenar datos en la nube
- **Responsive**: Adaptable a diferentes tamaños de pantalla

## Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Networking**: HTTP requests para comunicación con backend
- **SDK Mínimo**: Android 7.0 (API 24)
- **SDK Objetivo**: Android 15 (API 36)

## Dependencias Principales

- `androidx.core.ktx` - Extensiones Kotlin para Android
- `androidx.lifecycle.runtime.ktx` - Componentes de lifecycle
- `androidx.activity.compose` - Integración de Compose con Activities
- `androidx.compose.ui` - Componentes UI de Compose
- `androidx.compose.ui.graphics` - Gráficos y animaciones
- `androidx.compose.material3` - Material Design 3

## Cómo Ejecutar

### Prerrequisitos
- Android Studio Arctic Fox o superior
- JDK 11
- SDK de Android configurado

### Pasos
1. Abrir Android Studio
2. Seleccionar "Open" y navegar a la carpeta `HabitosApp`
3. Esperar a que Gradle sincronice las dependencias
4. Conectar un dispositivo Android o iniciar un emulador
5. Hacer clic en "Run" (botón verde de play)

## Configuración de la Aplicación

### URL del Backend
La aplicación se conecta al backend en las siguientes URLs:
- **Emulador**: `http://10.0.2.2:8000`
- **Dispositivo físico**: `http://<IP_DEL_PC>:8000`

Cambiar la URL base en el código según sea necesario (buscar `BASE_URL` en el código fuente).

### Permisos
La aplicación requiere los siguientes permisos:
- `INTERNET` - Para comunicación con el backend
- `ACCESS_NETWORK_STATE` - Para verificar conectividad

## Estructura del Código

```
HabitosApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/habitosapp/
│   │   │   ├── ui/          # Componentes de UI con Compose
│   │   │   ├── viewmodel/   # ViewModels para lógica de UI
│   │   │   ├── model/       # Modelos de datos
│   │   │   ├── network/     # Cliente HTTP y APIs
│   │   │   └── utils/       # Utilidades
│   │   ├── res/             # Recursos (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts     # Dependencias y configuración
├── build.gradle.kts         # Configuración del proyecto
└── settings.gradle.kts      # Configuración de módulos
```

## Arquitectura

La aplicación sigue el patrón MVVM:
- **Model**: Representa los datos y la lógica de negocio
- **View**: Componentes de UI en Compose
- **ViewModel**: Maneja el estado de la UI y comunica Model con View

## Manejo de Estados

- Estados de carga durante requests HTTP
- Estados de error con mensajes informativos
- Estados de éxito con feedback al usuario
- Gestión de sesiones de usuario

## Testing

Incluye tests instrumentados en `src/androidTest/` y tests unitarios en `src/test/`.

## Build y Release

### Debug Build
- Ejecutar normalmente en Android Studio

### Release Build
- Configurar `buildTypes.release` en `build.gradle.kts`
- Generar APK firmado desde Build > Generate Signed Bundle/APK

## Troubleshooting

### Problemas Comunes
- **Error de conexión**: Verificar que el backend esté ejecutándose y la URL sea correcta
- **Emulador no conecta**: Usar 10.0.2.2 en lugar de localhost
- **Dependencias no resuelven**: Limpiar cache de Gradle (File > Invalidate Caches)

### Logs
- Usar LogCat en Android Studio para debugging
- Filtrar por tag de la aplicación: `com.example.habitosapp`

## Contribución

Proyecto académico. Seguir las guías de Kotlin y Jetpack Compose para nuevas funcionalidades.