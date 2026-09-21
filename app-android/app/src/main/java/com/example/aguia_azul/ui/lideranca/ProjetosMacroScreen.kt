package com.example.aguia_azul.ui.lideranca

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.data.ProjectApiModel
import com.example.aguia_azul.data.ProjectStatuses
import com.example.aguia_azul.data.normalizeProjectStatus
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

private val projetoMacroFiltros = listOf("Todos", "Aprovado", "Em Producao", "Finalizado", "Rejeitado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjetosMacroScreen(
    projects: List<ProjectApiModel>,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProjetosMacro: () -> Unit,
    onNavigateToGestao: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filteredProjects = projects.filter { project ->
        selectedFilter == "Todos" || normalizeProjectStatus(project.status) == selectedFilter
    }

    Scaffold(
        containerColor = Branco,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Projetos Macro",
                        color = AzulEscuro,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = AzulEscuro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Branco)
            )
        },
        bottomBar = {
            LiderancaBottomBar(
                currentTab = LiderancaTab.ProjetosMacro,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToProjetosMacro = onNavigateToProjetosMacro,
                onNavigateToGestao = onNavigateToGestao
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(projetoMacroFiltros) { filtro ->
                    FilterChip(
                        selected = selectedFilter == filtro,
                        onClick = { selectedFilter = filtro },
                        label = { Text(filtro) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredProjects) { project ->
                    val normalizedStatus = normalizeProjectStatus(project.status)
                    ElevatedCard(
                        onClick = { onProjectClick(project.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AzulEscuro,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = badgeBackground(normalizedStatus),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = normalizedStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeContent(normalizedStatus),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExecutiveMetricColumn(
                                    label = "INVESTIMENTO",
                                    value = project.investment,
                                    modifier = Modifier.weight(1f)
                                )
                                ExecutiveDivider()
                                ExecutiveMetricColumn(
                                    label = "ROI (%)",
                                    value = project.achievedRoi,
                                    modifier = Modifier.weight(1f)
                                )
                                ExecutiveDivider()
                                ExecutiveMetricColumn(
                                    label = "ECONOMIA",
                                    value = project.actualSavings,
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutiveMetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = AzulEscuro,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
private fun ExecutiveDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(42.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

private fun badgeBackground(status: String): Color = when (status) {
    ProjectStatuses.FINALIZED -> Color(0xFFDDF5E6)
    ProjectStatuses.APPROVED -> Color(0xFFFFF1CC)
    ProjectStatuses.REJECTED -> Color(0xFFFFE2E0)
    else -> Color(0xFFE4EEFF)
}

private fun badgeContent(status: String): Color = when (status) {
    ProjectStatuses.FINALIZED -> Color(0xFF1B7F3B)
    ProjectStatuses.APPROVED -> Color(0xFF8A5A00)
    ProjectStatuses.REJECTED -> Color(0xFFB3261E)
    else -> AzulEscuro
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun ProjetosMacroScreenPreview() {
    Aguia_azulTheme {
        ProjetosMacroScreen(
            projects = emptyList(),
            onNavigateBack = {},
            onNavigateToDashboard = {},
            onNavigateToProjetosMacro = {},
            onNavigateToGestao = {},
            onProjectClick = {}
        )
    }
}
