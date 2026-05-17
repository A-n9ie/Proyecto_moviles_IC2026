import type {
    ComboProducto,
} from '../../../types/combos'

import './comboPreviewCard.css'

interface Props {
    nombre: string

    imagen: string

    productos: ComboProducto[]

    subtotal: number

    descuento: number

    total: number
}

const ComboPreviewCard = ({
                              nombre,
                              imagen,
                              productos,
                              subtotal,
                              descuento,
                              total,
                          }: Props) => {
    return (
        <div className="combo-preview-card">
            {imagen && (
                <img
                    src={imagen}
                    alt={nombre}
                    className="combo-preview-image"
                />
            )}

            <div className="combo-preview-content">
                <h3>{nombre}</h3>

                <div className="combo-preview-products">
                    {productos.map(
                        (producto) => (
                            <span
                                key={
                                    producto.id
                                }
                            >
                {
                    producto.nombre
                }
              </span>
                        ),
                    )}
                </div>

                <div className="combo-preview-prices">
                    <div>
                        Subtotal:
                        {' '}
                        ₡
                        {subtotal.toLocaleString()}
                    </div>

                    <div>
                        Descuento:
                        {' '}
                        {descuento}
                        %
                    </div>

                    <div className="combo-total">
                        Total:
                        {' '}
                        ₡
                        {total.toLocaleString()}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ComboPreviewCard