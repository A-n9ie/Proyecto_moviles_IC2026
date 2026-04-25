package com.example.cletaeats.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = CletaNaranja,
    onPrimary        = CletaBlanco,
    primaryContainer = CletaNaranjaOscuro,

    secondary        = CletaNaranjaClaro,
    onSecondary      = CletaGrisOscuro,

    background       = CletaGrisOscuro,
    onBackground     = CletaBlanco,

    surface          = CletaGrisMedio,
    onSurface        = CletaBlanco,
    surfaceVariant   = CletaGrisClaro,
    onSurfaceVariant = CletaTextoSecundario,

    error            = CletaError,
    onError          = CletaBlanco
)

private val LightColorScheme = lightColorScheme(
    primary          = CletaNaranjaLight,
    onPrimary        = CletaBlanco,
    background       = CletaFondoLight,
    onBackground     = CletaGrisOscuro,
    surface          = CletaSurfaceLight,
    onSurface        = CletaGrisOscuro,
    error            = CletaError,
    onError          = CletaBlanco
)

@Composable
fun CletaEatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = CletaTypography,
        content     = content
    )
}