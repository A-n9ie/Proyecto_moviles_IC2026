// Alineado al backend: tabla RESTAURANTE con NOMBRE, TIPO_COMIDA, DIRECCION, IMAGEN_URL, ESTADO
export interface Restaurante {
    id: number
    nombre: string
    tipo_comida: string
    direccion: string
    imagen_url: string
    estado: number      // 1=activo, 0=inactivo
}

export interface RestauranteRequest {
    nombre: string
    tipo_comida: string
    direccion: string
    imagen_url: string
}