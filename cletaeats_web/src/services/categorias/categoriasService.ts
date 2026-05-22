import { axiosClient } from '../api/axiosClient'
import type { Categoria } from '../../types/restaurantes'

export const categoriasService = {
    getAll: async (): Promise<Categoria[]> => {
        const r = await axiosClient.get<Categoria[]>('/admin/categorias')
        return r.data
    },
    create: async (nombre: string): Promise<{ id: number }> => {
        const r = await axiosClient.post<{ id: number }>('/admin/categorias', { nombre })
        return r.data
    },
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`/admin/categorias/${id}`)
    },
}