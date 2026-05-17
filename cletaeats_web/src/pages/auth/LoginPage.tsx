import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import Button from '../../components/common/buttons/Button'
import TextInput from '../../components/common/inputs/TextInput'

import { useAuth } from '../../hooks/useAuth'

const LoginPage = () => {
    const navigate = useNavigate()

    const { login } = useAuth()

    const [email, setEmail] = useState('')
    const [password, setPassword] =
        useState('')

    const [loading, setLoading] =
        useState(false)

    const [error, setError] =
        useState('')

    const handleLogin = async () => {
        try {
            setLoading(true)

            setError('')

            await login(email, password)

            navigate('/')
        } catch (error) {
            if (error instanceof Error) {
                setError(error.message)
            } else {
                setError(
                    'Credenciales incorrectas',
                )
            }
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-[var(--gris-oscuro)]">
            <div className="w-[380px] rounded-2xl border border-[var(--gris-borde)] bg-[var(--gris-medio)] p-10">
                <div className="mb-1 text-3xl font-extrabold text-[var(--naranja)]">
                    🛵 CletaEats
                </div>

                <div className="flex flex-col gap-4">
                    <TextInput
                        label="Correo electrónico"
                        placeholder="admin@cletaeats.com"
                        type="email"
                        value={email}
                        onChange={setEmail}
                    />

                    <TextInput
                        label="Contraseña"
                        placeholder="••••••"
                        type="password"
                        value={password}
                        onChange={setPassword}
                    />

                    <Button
                        onClick={handleLogin}
                        disabled={loading}
                    >
                        {loading
                            ? 'Ingresando...'
                            : 'Iniciar sesión'}
                    </Button>

                    {error && (
                        <div className="text-center text-sm text-[var(--error)]">
                            {error}
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}

export default LoginPage