import { useState } from 'react'
import Button from '../../components/common/buttons/Button'
import DataTable from '../../components/common/table/DataTable'
import { useCategorias } from '../../hooks/useCategorias'
import type { Categoria } from '../../types/restaurantes'

const CategoriasTab = () => {
    const { categorias, loading, handleCreate, handleDelete } = useCategorias()
    const [nuevaCategoria, setNuevaCategoria] = useState('')

    return (
        <div>
            <div className="flex gap-2 mb-4">
                <input
                    value={nuevaCategoria}
                    onChange={e => setNuevaCategoria(e.target.value)}
                    placeholder="Nueva categoría (ej. italiana)"
                />
                <Button
                    onClick={() => { if (nuevaCategoria.trim()) { handleCreate(nuevaCategoria.trim()); setNuevaCategoria('') } }}
                >
                    + Agregar
                </Button>
            </div>
            <DataTable
                loading={loading}
                data={categorias}
                columns={[
                    { key: 'nombre', title: 'Nombre' },
                    {
                        key: 'actions', title: 'Acciones',
                        render: (c: Categoria) => (
                            <Button variant="danger" onClick={() => handleDelete(c.id, c.nombre)}>
                                Eliminar
                            </Button>
                        ),
                    },
                ]}
            />
        </div>
    )
}
export default CategoriasTab