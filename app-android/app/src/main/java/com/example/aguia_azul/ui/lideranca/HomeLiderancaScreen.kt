package com.example.aguia_azul.ui.lideranca

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.components.HeaderMenuItem
import com.example.aguia_azul.ui.components.ErrorState
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro

@Composable
fun HomeLiderancaScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToProjetosMacro: () -> Unit = {},
    onNavigateToEstrategias: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: LiderancaViewModel = viewModel()
) {
    // A carga acontece quando a tela aparece, ja com sessao: no init do ViewModel ela
    // rodava na composicao do grafico de navegacao, antes do login.
    LaunchedEffect(Unit) { viewModel.loadData() }

    val dashboardState by viewModel.dashboardState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    HomeLiderancaContent(
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToProjetosMacro = onNavigateToProjetosMacro,
        onNavigateToEstrategias = onNavigateToEstrategias,
        onLogout = onLogout,
        dashboardState = dashboardState,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRetry = viewModel::loadData
    )
}

@Composable
fun HomeLiderancaContent(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToProjetosMacro: () -> Unit = {},
    onNavigateToEstrategias: () -> Unit = {},
    onLogout: () -> Unit = {},
    dashboardState: LiderancaDashboardState = LiderancaDashboardState(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CurvedHeader(
                title = null,
                profileIcon = Icons.Default.AccountCircle,
                showLogo = true,
                logoHeight = 98.dp,
                menuItems = listOf(
                    HeaderMenuItem("Sair", Icons.AutoMirrored.Filled.Logout, onLogout)
                )
            )
        },
        bottomBar = {
            LiderancaBottomBar(
                currentTab = LiderancaTab.Dashboard,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToProjetosMacro = onNavigateToProjetosMacro,
                onNavigateToGestao = onNavigateToEstrategias
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Painel de Resultados",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            // Erro agora e erro: nao ha mais "offline exibindo os ultimos dados", porque nao
            // ha mais dado guardado para exibir — o que aparecia ali era mock.
            errorMessage?.let { message ->
                ErrorState(message = message, onRetry = onRetry)
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                // Sem o respiro no fim, o ultimo grafico terminava atras da barra inferior.
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    MetricCard(
                        label = "Orcamento total",
                        value = dashboardState.totalBudgetLabel,
                        icon = Icons.Default.AttachMoney
                    )
                }
                item {
                    MetricCard(
                        label = "Projetos Ativos",
                        value = dashboardState.activeProjectsLabel,
                        icon = Icons.AutoMirrored.Filled.List
                    )
                }
                item {
                    MetricCard(
                        label = "Gasto total",
                        value = dashboardState.totalSpentLabel,
                        icon = Icons.Default.AttachMoney
                    )
                }
                item {
                    MetricCard(
                        label = "Projetos",
                        value = dashboardState.projectsCountLabel,
                        icon = Icons.Default.Lightbulb
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    DistributionChartCard(
                        title = "Projetos por status",
                        icon = Icons.Default.PieChart,
                        data = dashboardState.statusDistribution
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    TrendChartCard(
                        title = "Orcamento por data de criacao",
                        icon = Icons.Default.BarChart,
                        data = dashboardState.budgetTrend
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(128.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}


@Composable
private fun DistributionChartCard(
    title: String,
    icon: ImageVector,
    data: List<DistributionSlice>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChartHeader(title = title, icon = icon)

            if (data.isEmpty()) {
                EmptyChartState()
            } else {
                val maxValue = data.maxOf { it.value }.coerceAtLeast(1f)
                data.take(5).forEach { entry ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.label, style = MaterialTheme.typography.bodyMedium)
                            Text(entry.value.toInt().toString(), fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { entry.value / maxValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = AzulEscuro,
                            trackColor = Color(0xFFE7ECF5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChartCard(
    title: String,
    icon: ImageVector,
    data: List<TrendEntry>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChartHeader(title = title, icon = icon)

            if (data.isEmpty()) {
                EmptyChartState()
            } else {
                val maxValue = data.maxOf { it.value }.coerceAtLeast(1f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.takeLast(6).forEach { entry ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = compactValue(entry.value),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((140f * (entry.value / maxValue)).dp.coerceAtLeast(12.dp))
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                    .background(AzulEscuro.copy(alpha = 0.85f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AzulEscuro
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyChartState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sem dados disponiveis.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=900dp,dpi=420"
)
@Composable
fun HomeLiderancaScreenPreview() {
    Aguia_azulTheme {
        HomeLiderancaContent(
            dashboardState = LiderancaDashboardState(
                totalBudgetLabel = "R$ 2.230.000,00",
                activeProjectsLabel = "1",
                totalSpentLabel = "R$ 469.900,00",
                projectsCountLabel = "3",
                statusDistribution = listOf(
                    DistributionSlice("Aprovado", 1f),
                    DistributionSlice("Em Producao", 1f),
                    DistributionSlice("Finalizado", 1f)
                ),
                budgetTrend = listOf(
                    TrendEntry("21/09", 850_000f, 1),
                    TrendEntry("21/09", 1_200_000f, 2),
                    TrendEntry("21/09", 180_000f, 3)
                )
            ),
            errorMessage = "Sem conexao com o servidor. Confira se a API esta no ar."
        )
    }
}

/** 850000 -> "850k": rotulo de barra nao cabe o valor inteiro. */
private fun compactValue(value: Float): String = when {
    value >= 1_000_000f -> String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.1fM", value / 1_000_000f)
    value >= 1_000f -> "${(value / 1_000f).toInt()}k"
    else -> value.toInt().toString()
}
