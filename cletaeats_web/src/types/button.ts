import type { ReactNode } from 'react'

export interface ButtonProps {
    children: ReactNode
    onClick?: () => void
    type?: 'button' | 'submit'
    disabled?: boolean
    variant?: 'primary' | 'danger' | 'secondary' | 'success'
}