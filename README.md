# Proyecto Móviles IC2026 - Hábitos App

## Descripción General

Este proyecto es una aplicación móvil para el seguimiento de hábitos, desarrollada como parte del curso IC2026. La aplicación consta de dos componentes principales:

- **Backend**: API REST implementada en Python que maneja la lógica de negocio, autenticación de usuarios y gestión de hábitos.
- **Frontend**: Aplicación Android desarrollada en Kotlin con Jetpack Compose que proporciona la interfaz de usuario para interactuar con los hábitos.

## Tecnologías Utilizadas

- **Backend**:
  - Python 3.x
  - Servidor HTTP personalizado (sin frameworks externos)
  - Arquitectura limpia (Clean Architecture) con capas de dominio, casos de uso y repositorios
  - Base de datos SQL (archivos de creación en `data/sql/`)

- **Frontend**:
  - Kotlin
  - Jetpack Compose para la UI
  - Android SDK mínimo 24 (Android 7.0)

## Estructura del Proyecto

```
Proyecto_moviles_IC2026/
├── backend_habitos/          # API backend en Python
│   ├── main.py              # Punto de entrada del servidor
│   ├── core/                # Lógica de negocio
│   ├── data/                # Capa de datos y repositorios
│   ├── domain/              # Interfaces y dominio
│   └── interface/           # Controladores y servicios
├── data/sql/                # Scripts SQL para la base de datos
└── HabitosApp/              # Aplicación Android
    ├── app/                 # Código fuente de la app
    └── build.gradle.kts     # Configuración de Gradle
```

## Instalación y Configuración

### Prerrequisitos

- Python 3.x instalado
- Android Studio con SDK configurado
- Emulador Android o dispositivo físico

### Configuración del Backend

1. Navegar a la carpeta `backend_habitos`
2. Ejecutar `python main.py`
3. El servidor se iniciará en `localhost:8000`

### Configuración del Frontend

1. Abrir Android Studio
2. Importar el proyecto desde la carpeta `HabitosApp`
3. Ejecutar la aplicación en un emulador o dispositivo

### Configuración de Red

- **Emulador Android**: Usar `http://10.0.2.2:8000` como URL base
- **Dispositivo físico**: Cambiar HOST en `main.py` a `"0.0.0.0"` y usar la IP local del PC (ej: `http://192.168.1.105:8000`)

## Funcionalidades

- Registro e inicio de sesión de usuarios
- Creación, edición y eliminación de hábitos
- Seguimiento del progreso de hábitos
- Interfaz intuitiva con Jetpack Compose

## Contribución

Proyecto académico para el curso IC2026. Para modificaciones, seguir la estructura de Clean Architecture en el backend y las mejores prácticas de Android en el frontend.

## Licencia

Este proyecto es para fines educativos.