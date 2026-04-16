# Backend Hábitos - API REST

## Descripción

Este es el backend de la aplicación Hábitos, implementado como una API REST en Python puro sin frameworks externos. Proporciona endpoints para la gestión de usuarios y hábitos, siguiendo los principios de Clean Architecture.

## Funcionalidades

- **Autenticación de usuarios**: Registro, login y gestión de sesiones
- **Gestión de hábitos**: Crear, leer, actualizar y eliminar hábitos
- **Validación de datos**: Verificación de entrada y manejo de errores
- **Sesiones**: Manejo de sesiones de usuario para autenticación

## Tecnologías y Arquitectura

- **Lenguaje**: Python 3.x
- **Servidor**: HTTP server personalizado usando `http.server`
- **Arquitectura**: Clean Architecture con capas separadas:
  - **Domain**: Interfaces y entidades de negocio
  - **Use Cases**: Lógica de aplicación
  - **Interface**: Controladores y servicios HTTP
  - **Data**: Repositorios y conexión a base de datos

## Endpoints de la API

### Autenticación
- `POST /auth/register` - Registro de nuevo usuario
- `POST /auth/login` - Inicio de sesión
- `POST /auth/logout` - Cierre de sesión

### Hábitos
- `GET /habits` - Obtener hábitos del usuario
- `POST /habits` - Crear nuevo hábito
- `PUT /habits/{id}` - Actualizar hábito
- `DELETE /habits/{id}` - Eliminar hábito

## Cómo Ejecutar

1. Asegurarse de tener Python 3.x instalado
2. Navegar a la carpeta `backend_habitos`
3. Ejecutar: `python main.py`
4. El servidor se iniciará en el puerto 8000

## Configuración de Red

### Para Emulador Android (por defecto)
- HOST = "localhost"
- URL base en Android: `http://10.0.2.2:8000`

### Para Dispositivo Físico
1. Cambiar en `main.py`: `HOST = "0.0.0.0"`
2. Usar la IP local del PC en Android: `http://<IP_LOCAL>:8000`
3. Asegurar que PC y dispositivo estén en la misma red WiFi
4. Verificar que el firewall permita conexiones en el puerto 8000

## Base de Datos

Los scripts SQL para crear las tablas se encuentran en `../data/sql/`:
- `creacion_tablas.sql` - Estructura de tablas
- `tipos_codigos.sql` - Datos iniciales

## Estructura del Código

```
backend_habitos/
├── main.py                 # Punto de entrada
├── core/
│   ├── entities/          # Entidades de dominio (Usuario, Habito)
│   └── use_cases/         # Casos de uso (Auth, Habitos)
├── data/
│   ├── database/          # Conexión a BD
│   └── repositories/      # Implementación de repositorios
├── domain/
│   └── interfaces/        # Interfaces de repositorios
├── interface/
│   ├── controllers/       # Controladores HTTP
│   └── services/          # Servicios (sesiones)
└── comoEjecutar.txt       # Instrucciones de ejecución
```

## Dependencias

No requiere dependencias externas de Python. Usa solo la biblioteca estándar.

## Manejo de Errores

La API devuelve respuestas JSON con códigos de estado HTTP apropiados:
- 200: Éxito
- 400: Error de cliente (datos inválidos)
- 401: No autorizado
- 404: Recurso no encontrado
- 500: Error interno del servidor

## Logs

Los logs del servidor se muestran en la consola. Para debugging en Android Studio, revisar LogCat.