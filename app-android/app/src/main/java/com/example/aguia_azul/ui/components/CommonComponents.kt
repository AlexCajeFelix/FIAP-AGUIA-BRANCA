package com.example.aguia_azul.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.aguia_azul.R
import androidx.compose.foundation.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.ui.theme.AzulCeu
import com.example.aguia_azul.ui.theme.AzulClaro
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.AzulMedio
import com.example.aguia_azul.ui.theme.AzulProfundo
import com.example.aguia_azul.ui.theme.Branco
import androidx.compose.ui.tooling.preview.Devices
import com.example.aguia_azul.ui.theme.Aguia_azulTheme

private val HeaderShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
private val HeaderHeight = 140.dp
/** Com saudacao o header precisa de mais altura: a linha de baixo estava sendo cortada. */
private val HeaderHeightComTitulo = 184.dp
private val HeaderHorizontalPadding = 24.dp
private val HeaderVerticalPadding = 20.dp

@Composable
fun CurvedHeader(
    title: String? = null,
    profileIcon: ImageVector? = Icons.Default.AccountCircle,
    showLogo: Boolean = true,
    logoHeight: Dp = 44.dp,
    profileIconOnStart: Boolean = false,
    onProfileClick: () -> Unit = {},
    /**
     * Itens do menu do perfil. Com a lista preenchida, o icone abre um menu em vez de navegar
     * direto — e por ali que sai o "sair da conta", sem mais um botao solto no header.
     */
    menuItems: List<HeaderMenuItem> = emptyList()
) {
    // O degrade antigo ia de AzulEscuro a AzulClaro e terminava claro exatamente onde fica a
    // asa do logo — branco sobre azul claro, a asa praticamente sumia. Agora o topo e mais
    // fundo e o tom so abre no fim da curva.
    val headerGradient = Brush.linearGradient(
        colors = listOf(AzulProfundo, AzulEscuro)
    )

    // Entrada curta: o header desce e aparece. 400ms de fade + slide nao custa frame nenhum
    // e tira a sensacao de tela que pisca pronta.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val entrada by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "header_entrada"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (title == null) HeaderHeight else HeaderHeightComTitulo)
            .clip(HeaderShape)
            .background(headerGradient)
            // Brilho bem fraco no canto: o suficiente para o bloco nao ser chapado, sem criar
            // a faixa clara que o degrade anterior desenhava no meio do header.
            .background(
                Brush.radialGradient(
                    colors = listOf(AzulCeu.copy(alpha = 0.10f), Color.Transparent),
                    radius = 900f
                )
            )
            // statusBarsPadding porque o app e edge-to-edge: sem isso o botao do perfil
            // ficava debaixo do relogio.
            .statusBarsPadding()
            .padding(
                start = HeaderHorizontalPadding,
                end = HeaderHorizontalPadding,
                top = 8.dp,
                bottom = HeaderVerticalPadding
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Logo e botoes numa linha, saudacao embaixo. Antes logo e titulo ficavam empilhados
        // numa coluna e o botao era centralizado em relacao aos dois, entao ele aparecia no
        // vao entre as duas linhas e a saudacao encostava na borda de baixo.
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profileIconOnStart) {
                    HeaderIconButton(
                        profileIcon = profileIcon,
                        onProfileClick = onProfileClick
                    )
                }

                if (showLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_branca_contorno),
                        contentDescription = "Logo Aguia Branca",
                        modifier = Modifier
                            .height(logoHeight)
                            .graphicsLayer {
                                alpha = entrada
                                translationY = (1f - entrada) * -24f
                            }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (!profileIconOnStart) {
                    HeaderIconButton(
                        profileIcon = profileIcon,
                        menuItems = menuItems,
                        onProfileClick = onProfileClick
                    )
                }
            }

            if (title != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Branco,
                    maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = entrada }
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    profileIcon: ImageVector?,
    onProfileClick: () -> Unit,
    contentDescription: String? = "Perfil",
    menuItems: List<HeaderMenuItem> = emptyList()
) {
    if (profileIcon == null) return

    var menuAberto by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { if (menuItems.isEmpty()) onProfileClick() else menuAberto = true },
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Branco.copy(alpha = 0.18f))
                .border(1.dp, Branco.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = profileIcon,
                contentDescription = contentDescription,
                tint = Branco,
                modifier = Modifier.size(28.dp)
            )
        }

        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false }
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    leadingIcon = { Icon(item.icon, contentDescription = null) },
                    onClick = {
                        menuAberto = false
                        item.onClick()
                    }
                )
            }
        }
    }
}

/** Um item do menu do perfil. */
data class HeaderMenuItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun CircularProgressRing(
    percentage: Float,
    size: Dp = 60.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = AzulEscuro
) {
    val alvo = percentage.coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = alvo,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "anel_progresso"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = AzulEscuro
        )
    }
}

@Composable
fun PastelCard(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, AzulCeu.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PrimaryFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = AzulEscuro,
        contentColor = Branco,
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownTextField(
    value: String,
    label: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            textStyle = textStyle,
            // Mesma forma e mesmas cores dos campos digitaveis: dropdown com aparencia propria
            // parecia outro componente no meio do formulario.
            shape = AguiaFieldShape,
            colors = aguiaFieldColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun CurvedHeaderPreview() {
    Aguia_azulTheme {
        CurvedHeader(title = "Dashboard")
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun CircularProgressRingPreview() {
    Aguia_azulTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressRing(percentage = 0.75f)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun PastelCardPreview() {
    Aguia_azulTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            PastelCard(backgroundColor = AzulClaro.copy(alpha = 0.2f)) {
                Text("Card Content", color = AzulEscuro)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun PrimaryFABPreview() {
    Aguia_azulTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            PrimaryFAB(onClick = {}, icon = Icons.Default.AccountCircle, label = "Profile")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun LargeFloatPreview() {
    Aguia_azulTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LargeFloatingActionButton(onClick = {}) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
            }
        }
    }
}
