export interface Repartidor {
    id: number
    nombre: string
    email: string          // viene como CORREO en el backend
    correo: string
    telefono: string
    cedula: string
    estado: number         // 1=disponible, 0=ocupado
    km_recorridos_diarios: number
    amonestaciones: number
}

export interface RepartidorRequest {
    nombre: string
    email: string
    cedula: string
    telefono: string
    direccion: string
}