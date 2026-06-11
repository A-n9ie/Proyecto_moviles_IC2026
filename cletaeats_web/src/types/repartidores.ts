export interface Repartidor {
    id: number
    nombre: string
    email: string          // viene como CORREO en el backend
    correo: string
    telefono: string
    cedula: string
    disponible: number      // 1=puede recibir pedidos, 0=ocupado
    estado: number   // USUARIO.ESTADO: 1=puede iniciar sesión, 0=bloqueado
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