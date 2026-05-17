import { axiosClient } from '../api/axiosClient.ts'

import type { LoginRequest, User } from '../../types/auth'

export const authService = {
    login: async (body: LoginRequest) => {
        const response = await axiosClient.post<User>(
            '/auth/login',
            body,
        )

        return response.data
    },

    logout: async () => {
        await axiosClient.post('/auth/logout')
    },
}