import { axiosClient } from '../api/axiosClient'
import type { Restaurante, RestauranteRequest } from '../../types/restaurantes'

export const restaurantesService = {
    getAll: async (): Promise<Restaurante[]> => {
        const r = await axiosClient.get<Restaurante[]>('/admin/restaurantes')
        return r.data
    },
    create: async (body: RestauranteRequest): Promise<Restaurante> => {
        const r = await axiosClient.post<Restaurante>('/admin/restaurantes', body)
        return r.data
    },
    update: async (id: number, body: Partial<RestauranteRequest>): Promise<Restaurante> => {
        const r = await axiosClient.put<Restaurante>(`/admin/restaurantes/${id}`, body)
        return r.data
    },
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`/admin/restaurantes/${id}`)
    },
}