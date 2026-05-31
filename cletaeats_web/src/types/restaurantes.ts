export interface RestauranteRequest {
    cedula_juridica: string
    nombre: string
    categoria_ids: number[]
    direccion: string
    imagen_url: string
    latitud: number | null
    longitud: number | null
}

export interface Restaurante {
    id: number
    cedula_juridica: string
    nombre: string
    categorias: Categoria[]
    direccion: string
    imagen_url: string
    estado: number
    latitud: number | null 
    longitud: number | null 
}

export interface Categoria {
    id: number
    nombre: string
}