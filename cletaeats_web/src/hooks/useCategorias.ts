import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { categoriasService } from '../services/categorias/categoriasService'
import { notificationService } from '../services/ui/notificationService'
import { confirmService } from '../services/ui/confirmService'

export const useCategorias = () => {
    const qc = useQueryClient()
    const query = useQuery({
        queryKey: ['categorias'],
        queryFn: categoriasService.getAll,
    })
    const createMut = useMutation({
        mutationFn: (nombre: string) => categoriasService.create(nombre),
        onSuccess: () => qc.invalidateQueries({ queryKey: ['categorias'] }),
    })
    const removeMut = useMutation({
        mutationFn: (id: number) => categoriasService.remove(id),
        onSuccess: () => qc.invalidateQueries({ queryKey: ['categorias'] }),
    })

    const handleCreate = async (nombre: string) => {
        try {
            await createMut.mutateAsync(nombre)
            notificationService.success('Categoría creada')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error creando categoría')
        }
    }

    const handleDelete = async (id: number, nombre: string) => {
        const ok = await confirmService.delete(nombre)
        if (!ok) return
        try {
            await removeMut.mutateAsync(id)
            notificationService.success('Categoría eliminada')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error eliminando categoría')
        }
    }

    return {
        categorias: query.data ?? [],
        loading: query.isLoading,
        handleCreate,
        handleDelete,
    }
}