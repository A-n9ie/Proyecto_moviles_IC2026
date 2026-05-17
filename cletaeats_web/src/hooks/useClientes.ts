import { createEntityHooks } from '../lib/react-query/createEntityHooks'
import { clientesService } from '../services/clientes/clientesService'
import { queryKeys } from '../lib/react-query/queryKeys'
import { notificationService } from '../services/ui/notificationService'
import { confirmService } from '../services/ui/confirmService'
import type { Cliente, ClienteRequest } from '../types/clientes'

const hooks = createEntityHooks<Cliente, ClienteRequest>(
    queryKeys.clientes,
    clientesService,
)

export const useClientes = () => {
    const query      = hooks.useList()
    const createMut  = hooks.useCreate()
    const updateMut  = hooks.useUpdate()
    const removeMut  = hooks.useRemove()

    const handleSubmit = async (data: ClienteRequest, cliente?: Cliente | null) => {
        try {
            if (cliente) {
                await updateMut.mutateAsync({ id: cliente.id, body: data })
                notificationService.success('Cliente actualizado')
            } else {
                await createMut.mutateAsync(data)
                notificationService.success('Cliente creado')
            }
        } catch (e: any) {
            notificationService.error(e?.message ?? (cliente ? 'Error actualizando' : 'Error creando'))
            throw e
        }
    }

    const handleDelete = async (cliente: Cliente) => {
        const ok = await confirmService.delete(cliente.nombre)
        if (!ok) return
        try {
            await removeMut.mutateAsync(cliente.id)
            notificationService.success('Cliente eliminado')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error eliminando cliente')
        }
    }

    return {
        clientes:  query.data ?? [],
        loading:   query.isLoading,
        error:     query.error,
        handleSubmit,
        handleDelete,
        creating:  createMut.isPending,
        updating:  updateMut.isPending,
        deleting:  removeMut.isPending,
    }
}