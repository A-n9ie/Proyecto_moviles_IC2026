import { useState } from 'react'
import './imageUpload.css'

const API_BASE = import.meta.env.VITE_API_URL // tu URL de Render, ej: https://cletaeats.onrender.com

interface Props {
    value: string
    onChange: (url: string) => void
}

const ImageUpload = ({ value, onChange }: Props) => {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const handleFile = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0]
        if (!file) return

        setLoading(true)
        setError('')

        const formData = new FormData()
        formData.append('file', file)

        try {
            const token = localStorage.getItem('cletaeats_token')
            const response = await fetch(`${API_BASE}/admin/upload-imagen`, {
                method: 'POST',
                headers: { Authorization: `Bearer ${token}` },
                body: formData,
            })
            const data = await response.json()
            onChange(`${API_BASE}${data.url}`)
        } catch {
            setError('Error al subir imagen')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="image-upload">
            {value ? (
                <img src={value} alt="preview" className="image-preview" />
            ) : (
                <div className="image-placeholder">Sin imagen</div>
            )}
            {loading && <p>Subiendo...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <input type="file" accept="image/*" onChange={handleFile} disabled={loading} />
        </div>
    )
}

export default ImageUpload