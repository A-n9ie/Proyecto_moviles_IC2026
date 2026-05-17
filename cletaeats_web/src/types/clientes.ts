export interface Cliente {
    id: number
    nombre: string
    email: string
    cedula: string
    telefono: string
    direccion: string
    estado: number
}

export interface ClienteRequest {
    nombre: string
    email: string
    cedula: string
    telefono: string
    direccion: string
}