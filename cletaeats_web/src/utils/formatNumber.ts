// Formatos numéricos para Costa Rica (₡, puntos como separador de miles)
const crLocale = 'es-CR'

export const formatColones = (amount: number): string =>
    new Intl.NumberFormat(crLocale, {
        style: 'currency',
        currency: 'CRC',
        minimumFractionDigits: 0,
    }).format(amount)
// → ₡4.000

export const formatNumber = (n: number): string =>
    new Intl.NumberFormat(crLocale).format(n)
// → 1.234

export const formatDecimal = (n: number, decimals = 2): string =>
    new Intl.NumberFormat(crLocale, {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals,
    }).format(n)
// → 1.234,56

export const formatDistance = (km: number): string =>
    `${formatDecimal(km, 1)} km`
// → 3,5 km