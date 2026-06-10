// src/services/api/axiosClient.ts
import axios from 'axios'
import { tokenStorage } from './tokenStorage'

export const axiosClient = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8000',
})

axiosClient.interceptors.request.use((config) => {
    const token = tokenStorage.get()
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const isLoginRequest = error.config?.url?.includes('/auth/login')
if (error.response?.status === 401 && !isLoginRequest) {
    tokenStorage.remove()
    localStorage.removeItem('cletaeats_user')
    window.location.href = '/login'
}

        const message =
    error.response?.data?.error ??
    error.response?.data?.message ??
    error.response?.data?.detail ??
    (error.response?.status === 401 ? 'Correo o contraseña incorrectos' : 'Error de servidor')
        return Promise.reject(new Error(message))
    },
)