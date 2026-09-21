package com.example.aguia_azul.ui.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.components.HeaderMenuItem
import com.example.aguia_azul.ui.components.ErrorState
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulCeu
import com.example.aguia_azul.ui.theme.AzulClaro
import com.example.aguia_azul.ui.theme.AzulMedio
import com.example.aguia_azul.ui.theme.AzulNevoa
import com.example.aguia_azul.ui.theme.CinzaTexto
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeOperadorScreen(
    onNavigateToCriarIdeia: () -> Unit,
    onNavigateToMinhasIdeias: () -> Unit,
    onNavigateToEstrategias: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: OperadorViewModel = viewModel()
) {
    // A carga acontece quando a tela aparece, ja com sessao: no init do ViewModel ela
    // rodava na composicao do grafico de navegacao, antes do login.
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    val ideas by viewModel.ideas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val quickActions = listOf(
        QuickAction("Criar Ideia", Icons.Default.AddCircle, AzulEscuro, onNavigateToCriarIdeia),
        // Laranja, verde e roxo nao sao cores da marca: os quatro atalhos ficam na mesma
        // familia de azul, variando o tom para continuar distinguiveis.
        QuickAction("Minhas Ideias", Icons.AutoMirrored.Filled.List, AzulClaro, onNavigateToMinhasIdeias),
        QuickAction("Estratégias", Icons.Default.Flag, AzulMedio, onNavigateToEstrategias),
        QuickAction("Meu Perfil", Icons.Default.Person, AzulCeu, onNavigateToPerfil)
    )

    Scaffold(
        topBar = {
            CurvedHeader(
                // O nome nao vem da API (o /auth/login devolve so o perfil), entao a saudacao
                // e generica em vez de inventar uma pessoa que nao existe.
                title = "Olá, operador",
                logoHeight = 64.dp,
                // "Meu perfil" ja e um dos atalhos do acesso rapido; repetir no menu so
                // duplicaria o caminho.
                menuItems = listOf(
                    HeaderMenuItem("Sair", Icons.AutoMirrored.Filled.Logout, onLogout)
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMinhasIdeias,
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Minhas Ideias") },
                    label = { Text("Minhas Ideias") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCriarIdeia,
                containerColor = AzulEscuro,
                contentColor = Branco
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova Ideia"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            errorMessage?.let { message ->
                ErrorState(
                    message = message,
                    onRetry = viewModel::loadDashboard,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            ResumoDeIdeiasCard(
                ideas = ideas,
                modifier = Modifier.padding(top = 20.dp)
            )

            Text(
                text = "Acesso Rápido",
                style = MaterialTheme.typography.titleLarge,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = true
            ) {
                items(quickActions) { action ->
                    ElevatedCard(
                        onClick = action.onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(
                                        color = action.accentColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.accentColor,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = AzulEscuro,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * O resumo das ideias de quem esta logado.
 *
 * No lugar de "820 pts / Top 10 da fabrica", que era numero fixo no codigo e nao vinha de
 * lugar nenhum, o cartao conta o que a API devolveu: quantas ideias existem, quantas foram
 * aprovadas e quantas ainda esperam decisao.
 */
@Composable
private fun ResumoDeIdeiasCard(
    ideas: List<com.example.aguia_azul.ui.operador.Idea>,
    modifier: Modifier = Modifier
) {
    val aprovadas = ideas.count { it.status.equals("Aprovada", ignoreCase = true) }
    val emAnalise = ideas.count { it.status.equals("Em analise", ignoreCase = true) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Branco),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (ideas.isEmpty()) "Comece por aqui" else "Suas ideias",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (ideas.isEmpty()) {
                Text(
                    text = "Nenhuma ideia enviada ainda",
                    style = MaterialTheme.typography.titleMedium,
                    color = AzulEscuro,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Registre uma melhoria da operação em Criar Ideia. O gestor recebe na fila de aprovações.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumoNumero("Enviadas", ideas.size, Modifier.weight(1f))
                    ResumoNumero("Aprovadas", aprovadas, Modifier.weight(1f))
                    ResumoNumero("Em análise", emAnalise, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ResumoNumero(rotulo: String, valor: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AzulNevoa, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = AzulEscuro,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = CinzaTexto
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun HomeOperadorScreenPreview() {
    Aguia_azulTheme {
        HomeOperadorScreen(
            onNavigateToCriarIdeia = {},
            onNavigateToMinhasIdeias = {},
            onNavigateToEstrategias = {},
            onNavigateToPerfil = {}
        )
    }
}
