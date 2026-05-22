export interface Restaurante {
    id: number
    cedula_juridica: string
    nombre: string
    categorias: Categoria[]
    direccion: string
    imagen_url: string
    estado: number
}

export interface RestauranteRequest {
    cedula_juridica: string
    nombre: string
    categoria_ids: number[]
    direccion: string
    imagen_url: string
}

export interface Categoria {
    id: number
    nombre: string
}