export interface Cliente {
    id: number
    nombre: string
    email: string
    cedula: string
    telefono: string
    direccion: string
    estado: number
    tarjetas?: Tarjeta[]
}

export interface Tarjeta {
    id: number
    numero: string
    alias: string
    es_principal: number
}

export interface ClienteRequest {
    nombre: string
    email: string
    cedula: string
    telefono: string
    direccion: string
}