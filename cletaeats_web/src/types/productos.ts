export interface Producto {
    id: number
    restaurante_id: number
    restaurante_nombre: string
    nombre: string
    descripcion: string
    estado: number
}

export interface ProductoRequest {
    restaurante_id: number
    nombre: string
    descripcion: string
}