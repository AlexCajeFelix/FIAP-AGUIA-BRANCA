package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheProjetoGestorScreen(
    project: ProjectApiModel?,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val projectState = project ?: ProjectApiModel(
        id = "",
        title = "Projeto nao encontrado",
        status = "Nao definido",
        currentStage = "Nao definido",
        investment = "-",
        estimatedRoi = "-",
        achievedRoi = "-",
        estimatedSavings = "-",
        actualSavings = "-",
        unit = "Unidade indisponivel",
        manager = "Nao informado",
        impact = "",
        creationDate = "",
        progress = 0f
    )

    Scaffold(
        containerColor = Branco,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = AzulEscuro
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(projectState.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar projeto",
                            tint = AzulEscuro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Branco)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = projectState.title,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Unidade",
                        value = projectState.unit
                    )
                    DetailRow(
                        icon = Icons.Default.Timeline,
                        label = "Etapa atual",
                        value = projectState.currentStage
                    )
                    DetailRow(
                        icon = Icons.Default.Verified,
                        label = "Status",
                        value = projectState.status
                    )
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ROI",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulEscuro,
                        fontWeight = FontWeight.Bold
                    )
                    ComparisonRow(
                        label = "Estimado",
                        value = projectState.estimatedRoi
                    )
                    ComparisonRow(
                        label = "Atual",
                        value = projectState.achievedRoi
                    )
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Economia",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulEscuro,
                        fontWeight = FontWeight.Bold
                    )
                    ComparisonRow(
                        label = "Estimado",
                        value = projectState.estimatedSavings
                    )
                    ComparisonRow(
                        label = "Atual",
                        value = projectState.actualSavings
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AzulEscuro
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = AzulEscuro
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun DetalheProjetoGestorScreenPreview() {
    Aguia_azulTheme {
        DetalheProjetoGestorScreen(
            project = null,
            onNavigateBack = {},
            onEditClick = {}
        )
    }
}
