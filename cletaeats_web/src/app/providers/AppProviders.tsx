import type {
    ReactNode,
} from 'react'

import {
    QueryClientProvider,
} from '@tanstack/react-query'

import { AuthProvider } from '../../context/AuthProvider'

import { queryClient } from '../../lib/react-query/queryClient'

interface Props {
    children: ReactNode
}

const AppProviders = ({
                          children,
                      }: Props) => {
    return (
        <QueryClientProvider
            client={queryClient}
        >
            <AuthProvider>
                {children}
            </AuthProvider>
        </QueryClientProvider>
    )
}

export default AppProviders