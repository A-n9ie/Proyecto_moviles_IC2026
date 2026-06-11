import { axiosClient } from '../api/axiosClient'
import type { Repartidor, RepartidorRequest } from '../../types/repartidores'

export const repartidoresService = {
    getAll: async (): Promise<Repartidor[]> => {
        const r = await axiosClient.get<Repartidor[]>('/admin/repartidores')
        return r.data
    },
    create: async (body: RepartidorRequest): Promise<Repartidor> => {
        const r = await axiosClient.post<Repartidor>('/auth/registro/repartidor', {
            ...body,
            password: '123456',
            confirmar_password: '123456',
            cedula: body.cedula ?? '',
            correo_contacto: body.email,
            tarjeta: '0000000000000000',
        })
        return r.data
    },
    update: async (id: number, body: Partial<RepartidorRequest>): Promise<Repartidor> => {
        const r = await axiosClient.put<Repartidor>(`/admin/repartidores/${id}`, body)
        return r.data
    },
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`/admin/repartidores/${id}`)
    },
    toggleEstado: async (id: number, estadoCuentaActual: number): Promise<void> => {
        await axiosClient.put(`/admin/repartidores/${id}`, {
            estado_cuenta: estadoCuentaActual === 1 ? 0 : 1,
        })
    },
    agregarAmonestacion: async (id: number, motivo: string): Promise<{ amonestaciones: number; suspendido: boolean; mensaje: string }> => {
        const r = await axiosClient.post(`/admin/repartidores/${id}/amonestacion`, { motivo })
        return r.data
    },
}