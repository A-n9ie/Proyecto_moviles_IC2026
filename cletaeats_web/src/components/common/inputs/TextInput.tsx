import { useState, useEffect } from 'react'
import './textInput.css'

export type ValidationRule =
    | { type: 'required'; message?: string }
    | { type: 'min'; value: number; message?: string }
    | { type: 'max'; value: number; message?: string }
    | { type: 'minLength'; value: number; message?: string }
    | { type: 'maxLength'; value: number; message?: string }
    | { type: 'pattern'; value: RegExp; message?: string }
    | { type: 'email'; message?: string }

interface Props {
    label?: string
    placeholder?: string
    value?: string
    type?: string
    onChange?: (value: string) => void
    rules?: ValidationRule[]
    // Llama a este callback con null = válido, string = error
    onValidate?: (error: string | null) => void
    disabled?: boolean
    hint?: string
}

const TextInput = ({
                       label,
                       placeholder,
                       value = '',
                       type = 'text',
                       onChange,
                       rules = [],
                       onValidate,
                       disabled,
                       hint,
                   }: Props) => {
    const [touched, setTouched] = useState(false)
    const [error, setError] = useState<string | null>(null)

    // Revalidar cuando cambia el valor
    useEffect(() => {
        if (!touched) return
        const err = validate(value, rules)
        setError(err)
        onValidate?.(err)
    }, [value, touched])

    const handleBlur = () => {
        setTouched(true)
        const err = validate(value, rules)
        setError(err)
        onValidate?.(err)
    }

    return (
        <div className="text-input-container">
            {label && (
                <label className="text-input-label">
                    {label}
                    {rules.some(r => r.type === 'required') && (
                        <span className="text-input-required"> *</span>
                    )}
                </label>
            )}
            <input
                type={type}
                value={value}
                placeholder={placeholder}
                className={`text-input ${error && touched ? 'text-input--error' : ''}`}
                onChange={(e) => onChange?.(e.target.value)}
                onBlur={handleBlur}
                disabled={disabled}
            />
            {hint && !error && (
                <span className="text-input-hint">{hint}</span>
            )}
            {error && touched && (
                <span className="text-input-error">{error}</span>
            )}
        </div>
    )
}


const validate = (value: string, rules: ValidationRule[]): string | null => {
    for (const rule of rules) {
        switch (rule.type) {
            case 'required':
                if (!value.trim())
                    return rule.message ?? 'Este campo es requerido'
                break
            case 'min':
                if (Number(value) < rule.value)
                    return rule.message ?? `El valor mínimo es ${rule.value}`
                break
            case 'max':
                if (Number(value) > rule.value)
                    return rule.message ?? `El valor máximo es ${rule.value}`
                break
            case 'minLength':
                if (value.length < rule.value)
                    return rule.message ?? `Mínimo ${rule.value} caracteres`
                break
            case 'maxLength':
                if (value.length > rule.value)
                    return rule.message ?? `Máximo ${rule.value} caracteres`
                break
            case 'pattern':
                if (!rule.value.test(value))
                    return rule.message ?? 'Formato inválido'
                break
            case 'email':
                if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value))
                    return rule.message ?? 'Correo inválido'
                break
        }
    }
    return null
}


export default TextInput