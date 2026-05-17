export interface Combo {
    id: number
    restaurante_id: number
    restaurante_nombre: string
    numero_combo: number
    nombre: string
    descripcion: string
    precio: number
    estado: number
}

export interface ComboRequest {
    restaurante_id: number
    numero_combo: number
    nombre: string
    descripcion: string
    precio: number
}