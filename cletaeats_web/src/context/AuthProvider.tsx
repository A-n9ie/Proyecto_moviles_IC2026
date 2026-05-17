import {
    useMemo,
    useState,
} from 'react'

import type { ReactNode } from 'react'

import type {
    AuthContextType,
    User,
} from '../types/auth'

import { AuthContext } from './AuthContext'

import { authService } from '../services/auth/authService'
import { tokenStorage } from '../services/api/tokenStorage'

interface Props {
    children: ReactNode
}

export const AuthProvider = ({
                                 children,
                             }: Props) => {
    const token = tokenStorage.get()

    const [user, setUser] = useState<User | null>(() => {
        const storedUser =
            localStorage.getItem('cletaeats_user')

        return storedUser
            ? JSON.parse(storedUser)
            : null
    })

    const login = async (
        email: string,
        password: string,
    ) => {
        const response = await authService.login({
            email,
            password,
            platform: 'WEB',
        })

        if (
            response.rol === 'CLIENTE' ||
            response.rol === 'REPARTIDOR'
        ) {
            throw new Error(
                'Este rol solo puede acceder desde la app móvil',
            )
        }

        tokenStorage.set(response.token)

        localStorage.setItem(
            'cletaeats_user',
            JSON.stringify(response),
        )

        setUser(response)
    }

    const logout = async () => {
        try {
            await authService.logout()
        } catch (error) {
            console.error(error)
        }

        tokenStorage.remove()

        localStorage.removeItem('cletaeats_user')

        setUser(null)
    }

    const value = useMemo<AuthContextType>(
        () => ({
            user,
            token,
            isAuthenticated: !!token,

            login,

            logout,
        }),
        [user, token],
    )

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}