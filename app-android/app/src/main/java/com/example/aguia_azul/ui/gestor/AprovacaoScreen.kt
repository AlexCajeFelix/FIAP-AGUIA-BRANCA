package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro

@Composable
fun AprovacaoScreen(
    ideaId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNovoProjeto: (String) -> Unit,
    viewModel: GestorViewModel = viewModel()
) {
    val idea = viewModel.getIdeaById(ideaId)
    var comment by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CurvedHeader(
                title = null,
                profileIcon = Icons.AutoMirrored.Filled.ArrowBack,
                showLogo = true,
                logoHeight = 88.dp,
                profileIconOnStart = true,
                onProfileClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (idea != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Detalhes da ideia",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = idea.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                MetadataCard(
                    unit = idea.operatorName,
                    date = idea.date
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Descricao",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    // A descricao e a que o operador escreveu, nao um paragrafo de exemplo:
                    // o gestor estava decidindo sobre um texto que nao era da ideia.
                    Text(
                        text = idea.description,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isExpanded) "Ver menos" else "Ver mais",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Estimativa de impacto nao existe no contrato da API. Enquanto nao
                    // existir, a tela mostra o que ela tem: quem enviou e quando.
                    ImpactMetric(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        text = "Enviada por ${idea.operatorName}",
                        modifier = Modifier.weight(1f)
                    )
                    ImpactMetric(
                        icon = Icons.Default.AttachMoney,
                        text = "Recebida em ${idea.date}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Attachment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Anexos (2)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Adicionar comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.approveIdea(ideaId)
                            onNavigateToNovoProjeto(ideaId)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulEscuro)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aprovar")
                    }
                    Button(
                        onClick = {
                            viewModel.rejectIdea(ideaId)
                            onNavigateBack()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.78f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rejeitar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ideia nao encontrada")
            }
        }
    }
}

@Composable
private fun MetadataCard(
    unit: String,
    date: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetadataItem(
                icon = Icons.Default.LocationOn,
                label = "Unidade",
                value = unit.ifBlank { "Vitoria" },
                modifier = Modifier.weight(1f)
            )
            MetadataItem(
                icon = Icons.Default.DateRange,
                label = "Data",
                value = date,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetadataItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "$label $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun ImpactMetric(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=900dp,dpi=420"
)
@Composable
fun AprovacaoScreenPreview() {
    Aguia_azulTheme {
        AprovacaoScreen(
            ideaId = "1",
            onNavigateBack = {},
            onNavigateToNovoProjeto = {}
        )
    }
}
