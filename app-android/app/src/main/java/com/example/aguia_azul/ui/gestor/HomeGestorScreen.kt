package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.data.ProjectApiModel
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.components.HeaderMenuItem
import com.example.aguia_azul.ui.components.EmptyState
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulCeu
import com.example.aguia_azul.ui.theme.AzulClaro
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.AzulNevoa
import com.example.aguia_azul.ui.theme.CinzaTexto
import com.example.aguia_azul.ui.theme.Branco

@Composable
fun HomeGestorScreen(
    onNavigateToAprovacao: (String) -> Unit,
    onNavigateToAcompanhamento: (String) -> Unit,
    onNavigateToProjetos: (String) -> Unit,
    onNavigateToEstrategias: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: GestorViewModel = viewModel()
) {
    // A carga acontece quando a tela aparece, ja com sessao: no init do ViewModel ela
    // rodava na composicao do grafico de navegacao, antes do login.
    LaunchedEffect(Unit) { viewModel.loadData() }

    val pipeline by viewModel.pipeline.collectAsState()
    val activeProjects by viewModel.activeProjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    HomeGestorContent(
        onNavigateToAprovacao = onNavigateToAprovacao,
        onNavigateToAcompanhamento = onNavigateToAcompanhamento,
        onNavigateToProjetos = onNavigateToProjetos,
        onNavigateToEstrategias = onNavigateToEstrategias,
        onLogout = onLogout,
        pipeline = pipeline,
        activeProjects = activeProjects,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRefresh = { viewModel.loadData() }
    )
}

@Composable
fun HomeGestorContent(
    onNavigateToAprovacao: (String) -> Unit,
    onNavigateToAcompanhamento: (String) -> Unit,
    onNavigateToProjetos: (String) -> Unit,
    onNavigateToEstrategias: () -> Unit,
    onLogout: () -> Unit = {},
    pipeline: List<SubmittedIdea>,
    activeProjects: List<ProjectApiModel> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRefresh: () -> Unit = {}
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
            val navigationColors = NavigationBarItemDefaults.colors(
                selectedIconColor = AzulEscuro,
                selectedTextColor = AzulEscuro,
                // Cinza puro ao lado de azul puxa para o esverdeado e some no fundo claro.
                indicatorColor = AzulCeu.copy(alpha = 0.22f),
                unselectedIconColor = CinzaTexto,
                unselectedTextColor = CinzaTexto
            )

            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.ViewKanban, contentDescription = "Pipeline") },
                    label = { Text("Pipeline") },
                    colors = navigationColors
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToProjetos("Aprovados") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Projetos") },
                    label = { Text("Projetos") },
                    colors = navigationColors
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToEstrategias,
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estrategias") },
                    label = { Text("Estrategias") },
                    colors = navigationColors
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRefresh,
                modifier = Modifier.size(56.dp),
                containerColor = AzulEscuro,
                contentColor = Branco
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar Status",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            if (isLoading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                Text(
                    text = "Pipeline de Aprovações",
                    style = MaterialTheme.typography.displayLarge,
                    color = AzulEscuro,
                    fontSize = 29.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            if (pipeline.isEmpty() && !isLoading) {
                item {
                    EmptyState(
                        message = "Nenhuma ideia esperando decisão",
                        hint = "Quando um operador enviar uma ideia, ela aparece aqui para você aprovar ou recusar.",
                        actionLabel = "Atualizar",
                        onAction = onRefresh
                    )
                }
            }

            items(pipeline) { idea ->
                PipelineIdeaCard(
                    idea = idea,
                    onClick = { onNavigateToAprovacao(idea.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProjetos("EmProducao") },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Projetos em Execução",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AzulEscuro,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Abrir projetos em execucao",
                        tint = AzulEscuro
                    )
                }
            }

            if (activeProjects.isEmpty() && !isLoading) {
                item {
                    EmptyState(
                        message = "Nenhum projeto em execução",
                        hint = "Aprove uma ideia e defina o orçamento para ela virar projeto. O progresso passa a ser acompanhado aqui.",
                        actionLabel = "Ver todos os projetos",
                        onAction = { onNavigateToProjetos("Aprovados") }
                    )
                }
            }

            items(activeProjects) { project ->
                ActiveProjectCard(
                    project = project,
                    onClick = { onNavigateToAcompanhamento(project.id.toString()) }
                )
            }

            // Espaco para o cartao final nao terminar atras do FAB e da barra inferior.
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }
}

@Composable
private fun ActiveProjectCard(
    project: ProjectApiModel,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val progressoAnimado by animateFloatAsState(
                    targetValue = project.progress,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "progresso_projeto"
                )
                LinearProgressIndicator(
                    progress = { progressoAnimado },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp),
                    color = AzulClaro,
                    trackColor = AzulNevoa,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(project.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PipelineIdeaCard(
    idea: SubmittedIdea,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = idea.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // A API nao tem unidade; tem quem enviou e quando. Mostrar o dado que existe
                // e melhor do que um icone de local apontando para um traco.
                PipelineMetadata(
                    icon = Icons.Default.Person,
                    text = listOf(idea.operatorName, idea.date)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PipelineMetadata(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun HomeGestorScreenPreview() {
    val sampleData = listOf(
        SubmittedIdea("1", "Joao Silva", "Vitoria", "Otimizacao de Rota X", "Reduzir o tempo de entrega em 15%...", "20/05/2026"),
        SubmittedIdea("2", "Maria Santos", "Serra", "Novo Sistema de Triagem", "Implementar IA para classificar pacotes...", "21/05/2026")
    )
    Aguia_azulTheme {
        HomeGestorContent(
            onNavigateToAprovacao = {},
            onNavigateToAcompanhamento = {},
            onNavigateToProjetos = {},
            onNavigateToEstrategias = {},
            pipeline = sampleData
        )
    }
}
