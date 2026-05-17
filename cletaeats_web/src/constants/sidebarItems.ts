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
]