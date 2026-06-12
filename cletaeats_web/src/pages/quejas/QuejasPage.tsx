import { useEffect, useState, useCallback } from 'react'

import Button from '../../components/common/buttons/Button'
import DataTable from '../../components/common/table/DataTable'
import StatusBadge from '../../components/common/table/StatusBadge'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import './quejasPage.css'
import { quejasService } from '../../services/quejas/quejasService'
import { confirmService } from '../../services/ui/confirmService'
import { notificationService } from '../../services/ui/notificationService'
import type { Queja, AccionQueja } from '../../types/quejas'

const FILTROS = [
    { label: 'Todas', value: undefined },
    { label: 'Pendientes', value: 0 },
    { label: 'Amonestadas', value: 1 },
    { label: 'Menores', value: 2 },
]

const QuejasPage = () => {
    const [quejas, setQuejas] = useState<Queja[]>([])
    const [loading, setLoading] = useState(true)
    const [filtro, setFiltro] = useState<number | undefined>(undefined)

    const cargar = useCallback(async () => {
        setLoading(true)
        try {
            const data = await quejasService.getAll(filtro)
            setQuejas(data)
        } catch (e) {
            console.error('Error cargando quejas', e)
        } finally {
            setLoading(false)
        }
    }, [filtro])

    useEffect(() => {
        cargar()
    }, [cargar])

    const handleClasificar = async (queja: Queja, accion: AccionQueja) => {
        const texto =
            accion === 'amonestar'
                ? `¿Amonestar a ${queja.repartidor_nombre}? Esto suma una amonestación (4 = suspensión).`
                : `¿Marcar como queja menor? No genera amonestación.`

        const confirmado = await confirmService.confirm(texto)
        if (!confirmado) return

        try {
            await quejasService.clasificar(queja.id, accion)
            notificationService.success(
                accion === 'amonestar'
                    ? 'Queja amonestada correctamente.'
                    : 'Queja marcada como menor.'
            )
            await cargar()
        } catch (e) {
            console.error('Error clasificando queja', e)
            notificationService.error('No se pudo clasificar la queja.')
        }
    }

    const badgePorEstado = (estadoTexto: string) => {
        // StatusBadge colorea según el texto; mostramos el estado legible
        return <StatusBadge status={estadoTexto} />
    }

    return (
        <div>
            <PageHeader
                title="Quejas"
                subtitle="Gestión de quejas de clientes sobre repartidores"
            />

            <div className="quejas-filtros">
                {FILTROS.map((f) => (
                    <Button
                        key={f.label}
                        variant={filtro === f.value ? 'primary' : 'secondary'}
                        onClick={() => setFiltro(f.value)}
                    >
                        {f.label}
                    </Button>
                ))}
            </div>

            <DataTable
                loading={loading}
                data={quejas}
                columns={[
                    { key: 'repartidor_nombre', title: 'Repartidor' },
                    { key: 'cliente_nombre', title: 'Cliente' },
                    { key: 'motivo', title: 'Motivo' },
                    {
                        key: 'descripcion',
                        title: 'Descripción',
                        render: (q: Queja) => (
                            <span title={q.descripcion}>
                                {q.descripcion?.length > 50
                                    ? q.descripcion.slice(0, 50) + '…'
                                    : q.descripcion}
                            </span>
                        ),
                    },
                    { key: 'fecha', title: 'Fecha' },
                    {
                        key: 'estado',
                        title: 'Estado',
                        render: (q: Queja) => badgePorEstado(q.estado_texto),
                    },
                    {
                        key: 'actions',
                        title: 'Acciones',
                        render: (q: Queja) =>
                            q.estado === 0 ? (
                                <div className="flex gap-2">
                                    <Button
                                        variant="danger"
                                        onClick={() => handleClasificar(q, 'amonestar')}
                                    >
                                        Amonestar
                                    </Button>
                                    <Button
                                        variant="secondary"
                                        onClick={() => handleClasificar(q, 'menor')}
                                    >
                                        Queja menor
                                    </Button>
                                </div>
                            ) : (
                                <span className="quejas-resuelta">Clasificada</span>
                            ),
                    },
                ]}
            />
        </div>
    )
}

export default QuejasPage