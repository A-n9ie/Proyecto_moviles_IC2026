import { useEffect, useState } from 'react'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import Spinner from '../../components/common/loaders/Spinner'
import { axiosClient } from '../../services/api/axiosClient'
import './reportesPage.css'

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

            <div className="reportes-grid">

                <div className="reporte-card">
                    <h3>🏆 Restaurante con más pedidos</h3>
                    <p className="reporte-valor"><strong>{data.restaurante_mas_pedidos.nombre}</strong></p>
                    <p className="reporte-sub">{data.restaurante_mas_pedidos.total} pedidos</p>
                </div>

                <div className="reporte-card">
                    <h3>📉 Restaurante con menos pedidos</h3>
                    <p className="reporte-valor"><strong>{data.restaurante_menos_pedidos.nombre}</strong></p>
                    <p className="reporte-sub">{data.restaurante_menos_pedidos.total} pedidos</p>
                </div>

                <div className="reporte-card full">
                    <h3>💰 Monto total por restaurante</h3>
                    <table className="reportes-table">
                        <thead>
                            <tr>
                                <th>Restaurante</th>
                                <th>Monto</th>
                            </tr>
                        </thead>
                        <tbody>
                            {data.monto_por_restaurante.map((r: any) => (
                                <tr key={r.nombre}>
                                    <td>{r.nombre}</td>
                                    <td>₡{r.monto_total?.toLocaleString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="reporte-card">
                    <h3>👤 Cliente top</h3>
                    <p className="reporte-valor"><strong>{data.cliente_top.nombre}</strong></p>
                    <p className="reporte-sub">{data.cliente_top.total} pedidos</p>
                </div>

                <div className="reporte-card">
                    <h3>⏰ Hora pico</h3>
                    <p className="reporte-valor"><strong>{data.hora_pico.hora}:00 hrs</strong></p>
                    <p className="reporte-sub">{data.hora_pico.total} pedidos en esa hora</p>
                </div>

            </div>
        </div>
    )
}

export default ReportesPage