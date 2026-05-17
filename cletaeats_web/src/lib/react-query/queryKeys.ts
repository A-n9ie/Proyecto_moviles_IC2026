// REEMPLAZAR completo:
export const queryKeys = {
    clientes:     ['clientes']     as const,
    pedidos:      ['pedidos']      as const,
    repartidores: ['repartidores'] as const,
    restaurantes: ['restaurantes'] as const,
    combos:       ['combos']       as const,
    combosByRestaurante: (id: number) => ['combos', id] as const,
}