import { useEffect, useState } from 'react'
import { axiosClient } from '../../services/api/axiosClient'

interface Evento {
    ID: number
    USUARIO_ID: number | null
    ROL: string
    ACCION: string
    DETALLE: string
    FECHA: string
}

const ACCION_LABEL: Record<string,string> = {
    LOGIN:            '🔐 Inicio de sesión',
    PEDIDO_CREADO:    '🛒 Pedido creado',
    PEDIDO_CANCELADO: '❌ Pedido cancelado',
    PEDIDO_ENTREGADO: '✅ Pedido entregado',
    TARJETA_AGREGADA: '💳 Tarjeta agregada',
    TARJETA_ELIMINADA:'🗑️ Tarjeta eliminada',
    PERFIL_EDITADO:   '✏️ Perfil editado',
}

export default function BitacoraPage(){
    const [eventos, setEventos] = useState<Evento[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(()=>{
        axiosClient.get<Evento[]>('/admin/bitacora')
            .then(r => setEventos(r.data))
            .catch(() => setEventos([]))
            .finally(() => setLoading(false))
    },[])

    return (
        <div className="page-container">
            <div className="page-header">
                <h2 className="page-title">🗒️ Bitácora de auditoría</h2>
            </div>
            {loading ? (
                <div className="loading-state">Cargando...</div>
            ) : eventos.length === 0 ? (
                <div className="empty-state">Sin eventos registrados.</div>
            ) : (
                <div className="table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                {['Fecha', 'Rol', 'Acción', 'Detalle', 'Usuario ID'].map(h => (
                                    <th key={h}>{h}</th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {eventos.map((e, i) => (
                                <tr key={e.ID} className={i % 2 === 0 ? 'even' : 'odd'}>
                                    <td>{e.FECHA}</td>
                                    <td>{e.ROL}</td>
                                    <td>{ACCION_LABEL[e.ACCION] ?? e.ACCION}</td>
                                    <td>{e.DETALLE || '—'}</td>
                                    <td>{e.USUARIO_ID ?? '—'}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}
