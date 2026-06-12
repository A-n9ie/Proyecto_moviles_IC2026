export interface Queja {
    id: number
    motivo: string
    descripcion: string
    fecha: string
    estado: number          // 0=pendiente, 1=amonestada, 2=menor
    estado_texto: string
    pedido_id: number | null
    cliente_id: number
    cliente_nombre: string
    repartidor_id: number
    repartidor_nombre: string
}

export type AccionQueja = 'amonestar' | 'menor'