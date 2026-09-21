package com.example.aguia_azul.ui.lideranca

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.data.StrategyApiModel
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import kotlinx.coroutines.delay

@Composable
fun GerenciarEstrategiaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToProjetosMacro: () -> Unit = {},
    onNavigateToGestao: () -> Unit = {},
    viewModel: LiderancaViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    val strategies by viewModel.strategies.collectAsState()
    var editingStrategy by remember { mutableStateOf<StrategyApiModel?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

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
        },
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = {
                        editingStrategy = null
                        showDialog = true
                    },
                    containerColor = AzulEscuro
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova diretriz")
                }
            }
        },
        bottomBar = {
            LiderancaBottomBar(
                currentTab = LiderancaTab.Gestao,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToProjetosMacro = onNavigateToProjetosMacro,
                onNavigateToGestao = onNavigateToGestao
            )
        }
    ) { innerPadding ->
        Crossfade(targetState = isLoading, label = "loading_fade") { loading ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AzulEscuro)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Carregando diretrizes estratégicas...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Gerenciar Estrategias",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AzulEscuro,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Defina as diretrizes para a plataforma",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    if (strategies.isEmpty()) {
                        item {
                            Text(
                                "Nenhuma diretriz cadastrada.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }

                    items(strategies) { strategy ->
                        ElevatedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strategy.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strategy.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            editingStrategy = strategy
                                            showDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AzulEscuro)
                                    }
                                    IconButton(onClick = { viewModel.deleteStrategy(strategy.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        StrategyDialog(
            strategy = editingStrategy,
            onDismiss = { showDialog = false },
            onSave = { id, title, description ->
                viewModel.saveStrategy(id, title, description)
                showDialog = false
            }
        )
    }
}

@Composable
private fun StrategyDialog(
    strategy: StrategyApiModel?,
    onDismiss: () -> Unit,
    onSave: (Int?, String, String) -> Unit
) {
    var title by remember(strategy) { mutableStateOf(strategy?.title.orEmpty()) }
    var description by remember(strategy) { mutableStateOf(strategy?.description.orEmpty()) }
    val canSave = title.isNotBlank() && description.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (strategy == null) "Nova estrategia" else "Editar estrategia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titulo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descricao") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(strategy?.id, title, description) },
                enabled = canSave
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GerenciarEstrategiaScreenPreview() {
    Aguia_azulTheme {
        GerenciarEstrategiaScreen(onNavigateBack = {})
    }
}
