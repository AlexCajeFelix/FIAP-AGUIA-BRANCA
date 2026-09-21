package com.example.aguia_azul.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.ui.theme.AzulCeu
import com.example.aguia_azul.ui.theme.AzulClaro
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.AzulNevoa
import com.example.aguia_azul.ui.theme.CinzaBorda
import com.example.aguia_azul.ui.theme.CinzaTexto

/** Cantos e cores dos campos, num lugar so: campo com aparencia propria em cada tela vira remendo. */
val AguiaFieldShape: Shape = RoundedCornerShape(16.dp)

@Composable
fun aguiaFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AzulClaro,
    unfocusedBorderColor = CinzaBorda,
    focusedLabelColor = AzulEscuro,
    unfocusedLabelColor = CinzaTexto,
    cursorColor = AzulClaro,
    focusedContainerColor = AzulNevoa.copy(alpha = 0.45f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLeadingIconColor = AzulEscuro,
    unfocusedLeadingIconColor = CinzaTexto
)

/**
 * O campo de texto do app.
 *
 * O icone fica dentro de um quadrado azul claro: solto, em cinza, ele desaparecia na borda do
 * campo. A borda muda de cor com foco — e o unico jeito de saber onde se esta digitando quando
 * a tela tem tres campos iguais.
 */
@Composable
fun AguiaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focado by interactionSource.collectIsFocusedAsState()
    val fundoIcone by animateColorAsState(
        targetValue = if (focado) AzulClaro else AzulNevoa,
        animationSpec = tween(200),
        label = "fundo_icone"
    )
    val corIcone by animateColorAsState(
        targetValue = if (focado) MaterialTheme.colorScheme.onPrimary else AzulEscuro,
        animationSpec = tween(200),
        label = "cor_icone"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = AguiaFieldShape,
        colors = aguiaFieldColors(),
        interactionSource = interactionSource,
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon?.let {
            {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(fundoIcone),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = corIcone,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

/** Cores da barra inferior: o cinza default some no fundo claro. */
@Composable
fun aguiaSelectedIndicator() = AzulCeu.copy(alpha = 0.22f)
