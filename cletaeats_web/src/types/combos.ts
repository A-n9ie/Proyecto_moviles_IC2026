export interface Combo {
    id: number
    restaurante_id: number
    restaurante_nombre: string
    numero_combo: number
    nombre: string
    descripcion: string
    precio: number
    estado: number
    imagen_url?: string
    productos: Producto[]
}

export interface ComboRequest {
    restaurante_id: number
    numero_combo: number
    nombre: string
    descripcion: string
    precio: number
    producto_ids: number[]
    imagen_url?: string
}

export interface Producto {
    id: number
    nombre: string
    descripcion?: string
}

export interface ComboProducto {
    id: number
    nombre: string
    descripcion?: string
}