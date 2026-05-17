import type { ButtonProps } from '../../../types/button'

import './button.css'

const Button = ({
                    children,
                    onClick,
                    type = 'button',
                    disabled = false,
                    variant = 'primary',
                }: ButtonProps) => {
    return (
        <button
            type={type}
            disabled={disabled}
            onClick={onClick}
            className={`custom-button ${variant}`}
        >
            {children}
        </button>
    )
}

export default Button