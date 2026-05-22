import { axiosClient } from '../api/axiosClient'
import type { Producto, ProductoRequest } from '../../types/productos'

export const productosService = {
    getAll: async (): Promise<Producto[]> => {
        const r = await axiosClient.get<Producto[]>('/admin/productos')
        return r.data
    },
    create: async (body: ProductoRequest): Promise<Producto> => {
        const r = await axiosClient.post<Producto>('/admin/productos', body)
        return r.data
    },
    update: async (id: number, body: Partial<ProductoRequest>): Promise<Producto> => {
        const r = await axiosClient.put<Producto>(`/admin/productos/${id}`, body)
        return r.data
    },
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`/admin/productos/${id}`)
    },
}