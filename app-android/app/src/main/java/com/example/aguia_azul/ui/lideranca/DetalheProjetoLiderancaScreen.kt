package com.example.aguia_azul.ui.lideranca

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.data.ProjectApiModel
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

@Composable
fun DetalheProjetoLiderancaScreen(
    project: ProjectApiModel?,
    onNavigateBack: () -> Unit
) {
    val projectState = project ?: ProjectApiModel(
        id = "",
        title = "Projeto nao encontrado",
        status = "Em Aprovação",
        currentStage = "Sem dados",
        investment = "-",
        estimatedRoi = "-",
        achievedRoi = "-",
        estimatedSavings = "-",
        actualSavings = "-",
        unit = "Nao informada",
        manager = "Nao informado",
        impact = "Sem alinhamento estrategico disponivel.",
        creationDate = "",
        progress = 0f
    )

    Scaffold(
        containerColor = Branco,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulEscuro)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Branco
                        )
                    }
                    Text(
                        text = projectState.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Branco,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Resumo Financeiro") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialHighlight(
                        label = "ROI",
                        value = projectState.achievedRoi,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialHighlight(
                        label = "Economia",
                        value = projectState.actualSavings,
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialHighlight(
                        label = "Investimento",
                        value = projectState.investment,
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SectionCard(title = "Status da Implantação") {
                Text(
                    text = projectState.currentStage,
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulEscuro,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { projectState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(10.dp),
                    color = AzulEscuro,
                    trackColor = AzulEscuro.copy(alpha = 0.12f)
                )
                Text(
                    text = "${(projectState.progress * 100).toInt()}% concluido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            SectionCard(title = "Equipe Responsável") {
                DetailLine(icon = Icons.Default.LocationOn, label = "Unidade", value = projectState.unit)
                DetailLine(icon = Icons.Default.Groups, label = "Gestor responsavel", value = projectState.manager)
            }

            SectionCard(title = "Impacto Estratégico") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = AzulEscuro
                    )
                    Text(
                        text = "Alinhamento com diretrizes corporativas",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = projectState.impact,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Branco),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun FinancialHighlight(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = AzulEscuro.copy(alpha = 0.05f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AzulEscuro
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AzulEscuro,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun DetalheProjetoLiderancaScreenPreview() {
    Aguia_azulTheme {
        DetalheProjetoLiderancaScreen(
            project = null,
            onNavigateBack = {}
        )
    }
}
