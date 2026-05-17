import { useQuery } from '@tanstack/react-query'
import { dashboardService } from '../services/dashboard/dashboardService'

export const useDashboard = () => {
    const query = useQuery({
        queryKey: ['dashboard'],
        queryFn:  dashboardService.getDashboardData,
        staleTime: 30_000,
    })

    return {
        data:    query.data ?? null,
        loading: query.isLoading,
        error:   query.isError ? 'Error cargando dashboard' : '',
        reload:  () => query.refetch(),
    }
}