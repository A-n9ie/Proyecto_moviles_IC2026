export interface LoginRequest {
    email: string
    password: string
    platform: 'WEB'
}

export interface User {
    id: number
    nombre: string
    email: string
    rol: string
    token: string
}

export interface AuthContextType {
    user: User | null
    token: string | null
    isAuthenticated: boolean
    initializing: boolean

    login: (email: string, password: string) => Promise<void>

    logout: () => void
}