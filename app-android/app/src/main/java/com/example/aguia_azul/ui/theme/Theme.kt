package com.example.aguia_azul.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * O tema da marca.
 *
 * Dynamic color continua desligado de proposito: no Android 12+ ele trocaria o azul da Aguia
 * Branca pelas cores do papel de parede de quem instalou.
 *
 * O esquema agora preenche superficie, contorno e variantes — antes so primary e secondary
 * estavam definidos, entao card, campo de texto e divisor caiam no roxo default do Material 3.
 */
private val LightColorScheme = lightColorScheme(
    primary = AzulEscuro,
    onPrimary = Branco,
    primaryContainer = AzulNevoa,
    onPrimaryContainer = AzulProfundo,
    secondary = AzulClaro,
    onSecondary = Branco,
    secondaryContainer = AzulNevoa,
    onSecondaryContainer = AzulEscuro,
    tertiary = AzulCeu,
    onTertiary = Branco,
    background = FundoClaro,
    onBackground = Preto,
    surface = Branco,
    onSurface = Preto,
    surfaceVariant = AzulNevoa,
    onSurfaceVariant = CinzaTexto,
    outline = CinzaBorda,
    outlineVariant = CinzaBorda,
    error = VermelhoErro,
    onError = Branco
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulCeu,
    onPrimary = AzulProfundo,
    primaryContainer = AzulEscuro,
    onPrimaryContainer = Branco,
    secondary = AzulClaro,
    onSecondary = Branco,
    secondaryContainer = AzulMedio,
    onSecondaryContainer = Branco,
    tertiary = AzulCeu,
    onTertiary = AzulProfundo,
    background = Color(0xFF0A1020),
    onBackground = Color(0xFFE6ECF7),
    surface = Color(0xFF111A2E),
    onSurface = Color(0xFFE6ECF7),
    surfaceVariant = Color(0xFF1A2540),
    onSurfaceVariant = Color(0xFFA9B8D4),
    outline = Color(0xFF2C3A57),
    outlineVariant = Color(0xFF2C3A57),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0B06)
)

@Composable
fun Aguia_azulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
