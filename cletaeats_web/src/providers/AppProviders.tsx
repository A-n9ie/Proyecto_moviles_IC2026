import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from '../lib/react-query/queryClient'
import { AuthProvider } from '../context/AuthProvider'
import type { ReactNode } from 'react'

interface Props { children: ReactNode }

const AppProviders = ({ children }: Props) => (
    <QueryClientProvider client={queryClient}>
        <AuthProvider>
            {children}
        </AuthProvider>
    </QueryClientProvider>
)

export default AppProviders