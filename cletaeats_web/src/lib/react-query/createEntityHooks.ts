import { useQuery, useMutation } from '@tanstack/react-query'
import { queryClient } from './queryClient'

interface EntityService<T, TRequest> {
    getAll: () => Promise<T[]>
    create?: (body: TRequest) => Promise<T>
    update?: (id: number, body: Partial<TRequest>) => Promise<T>
    remove?: (id: number) => Promise<void>
}

export function createEntityHooks<T, TRequest>(
    queryKey: readonly string[],
    service: EntityService<T, TRequest>,
) {
    const useList = () =>
        useQuery({ queryKey: [...queryKey], queryFn: service.getAll })

    const useCreate = () =>
        useMutation({
            mutationFn: service.create!,
            onSuccess: () => queryClient.invalidateQueries({ queryKey: [...queryKey] }),
        })

    const useUpdate = () =>
        useMutation({
            mutationFn: ({ id, body }: { id: number; body: Partial<TRequest> }) =>
                service.update!(id, body),
            onSuccess: () => queryClient.invalidateQueries({ queryKey: [...queryKey] }),
        })

    const useRemove = () =>
        useMutation({
            mutationFn: service.remove!,
            onSuccess: () => queryClient.invalidateQueries({ queryKey: [...queryKey] }),
        })

    return { useList, useCreate, useUpdate, useRemove }
}