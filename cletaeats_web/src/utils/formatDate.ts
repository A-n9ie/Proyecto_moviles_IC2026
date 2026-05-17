// Formatos de fecha/hora para Costa Rica (UTC-6)
const crLocale = 'es-CR'
const crTimeZone = 'America/Costa_Rica'

export const formatDate = (dateStr: string): string => {
    if (!dateStr) return '—'
    return new Intl.DateTimeFormat(crLocale, {
        timeZone: crTimeZone,
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(new Date(dateStr))
}
// → 15/05/2026

export const formatDateTime = (dateStr: string): string => {
    if (!dateStr) return '—'
    return new Intl.DateTimeFormat(crLocale, {
        timeZone: crTimeZone,
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(dateStr))
}
// → 15/05/2026, 07:23 p. m.

export const formatTimeOnly = (dateStr: string): string => {
    if (!dateStr) return '—'
    return new Intl.DateTimeFormat(crLocale, {
        timeZone: crTimeZone,
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(dateStr))
}
// → 07:23 p. m.