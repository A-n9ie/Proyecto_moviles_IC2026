package com.example.cletaeats_mobile.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Recibe solo dígitos (máx 4) y los muestra como MM/YY.
 * El estado real sigue siendo "1234", esto solo cambia lo que se ve.
 */
object CardDateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(4)
        val formatted = when {
            digits.length <= 2 -> digits
            else -> "${digits.take(2)}/${digits.drop(2)}"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 2) offset else offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else (offset - 1).coerceAtMost(4)
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/** Extrae solo dígitos y limita a 4 (MMYY). Usar en onValueChange. */
fun soloDigitosFecha(input: String): String =
    input.filter { it.isDigit() }.take(4)