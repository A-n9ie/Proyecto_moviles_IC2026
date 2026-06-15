import type { SidebarItem } from '../types/sidebar'

export const sidebarItems: SidebarItem[] = [
    {
        label: 'Dashboard',
        icon: '📊',
        path: '/',
    },
    {
        label: 'Pedidos',
        icon: '📦',
        path: '/pedidos',
    },
    {
        label: 'Clientes',
        icon: '👤',
        path: '/clientes',
    },
    {
        label: 'Repartidores',
        icon: '🏍️',
        path: '/repartidores',
    },
    {
        label: 'Restaurantes',
        icon: '🍔',
        path: '/restaurantes',
    },
    {
        label: 'Combos',
        icon: '🥡',
        path: '/combos',
    },
    {
    label: 'Reportes',
    icon: '📈',
    path: '/reportes',
    },
    {
    label: 'Quejas',
    icon: '⚠️',
    path: '/quejas',
    },
    {
    label: 'Bitácora',
    icon: '📝',
    path: '/bitacora',
    },
]