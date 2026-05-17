import { useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { pedidosService } from '../services/pedidos/pedidosService'
import { queryKeys } from '../lib/react-query/queryKeys'
import type { PedidoFilters } from '../types/pedidos'

export const usePedidos = () => {
    const query = useQuery({
        queryKey: queryKeys.pedidos,
        queryFn:  pedidosService.getAll,
    })

    const [filters, setFilters] = useState<PedidoFilters>({ search: '', estado: '' })

    const filteredPedidos = useMemo(() => {
        const data = query.data ?? []
        return data.filter((p) => {
            const q = filters.search.toLowerCase()
            const matchSearch = !q ||
                p.cliente_nombre?.toLowerCase().includes(q) ||
                p.restaurante_nombre?.toLowerCase().includes(q)
            const matchEstado = !filters.estado || String(p.estado) === filters.estado
            return matchSearch && matchEstado
        })
    }, [query.data, filters])

    return {
        pedidos:    filteredPedidos,
        loading:    query.isLoading,
        error:      query.error,
        filters,
        setFilters,
        reload:     () => query.refetch(),
    }
}