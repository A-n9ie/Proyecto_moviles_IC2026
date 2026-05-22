import { createEntityHooks } from '../lib/react-query/createEntityHooks'
import { productosService } from '../services/productos/productosService'
import { notificationService } from '../services/ui/notificationService'
import { confirmService } from '../services/ui/confirmService'
import type { Producto, ProductoRequest } from '../types/productos'

const hooks = createEntityHooks<Producto, ProductoRequest>(
    ['productos'],
    productosService,
)

export const useProductos = () => {
    const query     = hooks.useList()
    const createMut = hooks.useCreate()
    const updateMut = hooks.useUpdate()
    const removeMut = hooks.useRemove()

    const handleSubmit = async (data: ProductoRequest, producto?: Producto | null) => {
        try {
            if (producto) {
                await updateMut.mutateAsync({ id: producto.id, body: data })
                notificationService.success('Producto actualizado')
            } else {
                await createMut.mutateAsync(data)
                notificationService.success('Producto creado')
            }
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error guardando producto')
            throw e
        }
    }

    const handleDelete = async (producto: Producto) => {
        const ok = await confirmService.delete(producto.nombre)
        if (!ok) return
        try {
            await removeMut.mutateAsync(producto.id)
            notificationService.success('Producto eliminado')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error eliminando producto')
        }
    }

    return {
        productos: query.data ?? [],
        loading: query.isLoading,
        handleSubmit,
        handleDelete,
    }
}