import { useEffect, useState } from 'react'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import Spinner from '../../components/common/loaders/Spinner'
import { axiosClient } from '../../services/api/axiosClient'

const ReportesPage = () => {
    const [data, setData] = useState<any>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        axiosClient.get('/admin/reportes')
            .then(r => setData(r.data))
            .catch(() => setError('Error al cargar reportes'))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <div className="flex items-center justify-center py-20"><Spinner /></div>
    if (error) return <div className="text-[var(--error)]">{error}</div>

    return (
        <div>
            <PageHeader title="Reportes" subtitle="Estadísticas generales del sistema" />

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1.5rem', padding: '1.5rem' }}>

                <div className="card">
                    <h3>🏆 Restaurante con más pedidos</h3>
                    <p><strong>{data.restaurante_mas_pedidos.nombre}</strong></p>
                    <p>{data.restaurante_mas_pedidos.total} pedidos</p>
                </div>

                <div className="card">
                    <h3>📉 Restaurante con menos pedidos</h3>
                    <p><strong>{data.restaurante_menos_pedidos.nombre}</strong></p>
                    <p>{data.restaurante_menos_pedidos.total} pedidos</p>
                </div>

                <div className="card" style={{ gridColumn: 'span 2' }}>
                    <h3>💰 Monto total por restaurante</h3>
                    <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '0.5rem' }}>
                        <thead>
                            <tr>
                                <th style={{ textAlign: 'left', padding: '0.5rem', borderBottom: '1px solid var(--border)' }}>Restaurante</th>
                                <th style={{ textAlign: 'right', padding: '0.5rem', borderBottom: '1px solid var(--border)' }}>Monto</th>
                            </tr>
                        </thead>
                        <tbody>
                            {data.monto_por_restaurante.map((r: any) => (
                                <tr key={r.nombre}>
                                    <td style={{ padding: '0.5rem' }}>{r.nombre}</td>
                                    <td style={{ padding: '0.5rem', textAlign: 'right' }}>₡{r.monto_total?.toLocaleString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="card">
                    <h3>👤 Cliente top</h3>
                    <p><strong>{data.cliente_top.nombre}</strong></p>
                    <p>{data.cliente_top.total} pedidos</p>
                </div>

                <div className="card">
                    <h3>⏰ Hora pico</h3>
                    <p><strong>{data.hora_pico.hora}:00 hrs</strong></p>
                    <p>{data.hora_pico.total} pedidos en esa hora</p>
                </div>

            </div>
        </div>
    )
}

export default ReportesPage