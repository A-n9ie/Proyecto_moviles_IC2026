import { axiosClient } from '../api/axiosClient'
import type { Cliente, ClienteRequest } from '../../types/clientes'

export const clientesService = {
    getAll: async (): Promise<Cliente[]> => {
        const r = await axiosClient.get<Cliente[]>('/admin/clientes')
        return r.data
    },
    create: async (body: ClienteRequest): Promise<Cliente> => {
        // El backend crea clientes via /auth/registro/cliente
        const r = await axiosClient.post<Cliente>('/auth/registro/cliente', {
            ...body,
            password: '123456',           // contraseña temporal
            confirmar_password: '123456',
            cedula: body.cedula ?? '',
            tarjeta: '0000000000000000',
        })
        return r.data
    },
    update: async (id: number, body: Partial<ClienteRequest>): Promise<Cliente> => {
        const r = await axiosClient.put<Cliente>(`/admin/clientes/${id}`, body)
        return r.data
    },
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`/admin/clientes/${id}`)
    },
    toggleEstado: async (id: number, estadoActual: number): Promise<void> => {
        await axiosClient.put(`/admin/clientes/${id}`, {
            estado: estadoActual === 1 ? 0 : 1,
        })
    },
}