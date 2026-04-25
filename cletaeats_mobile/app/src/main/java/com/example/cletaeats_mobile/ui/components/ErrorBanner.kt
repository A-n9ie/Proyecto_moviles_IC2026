package com.example.cletaeats_mobile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cletaeats.ui.theme.CletaError

/**
 * Banner de error que aparece sobre el formulario.
 * Solo se muestra si message no es null.
 */
@Composable
fun ErrorBanner(message: String?) {
    if (message == null) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = CletaError.copy(alpha = 0.15f))
    ) {
        Text(
            text     = message,
            color    = CletaError,
            modifier = Modifier.padding(12.dp)
        )
    }
}