package com.example.aguia_azul.ui.operador

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.aguia_azul.ui.components.EmptyState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinhasIdeiasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCriarIdeia: () -> Unit = {},
    viewModel: OperadorViewModel = viewModel()
) {
    val ideas by viewModel.ideas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Ideias", color = AzulEscuro) },
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
        containerColor = Branco
    ) { innerPadding ->
        when {
            isLoading && ideas.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AzulEscuro)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (ideas.isEmpty()) {
                        item {
                            EmptyState(
                                message = "Você ainda não enviou nenhuma ideia",
                                hint = "Toque em Criar Ideia para registrar uma melhoria ou relatar um problema da operação. O gestor recebe na fila de aprovações.",
                                actionLabel = "Criar ideia",
                                onAction = onNavigateToCriarIdeia
                            )
                        }
                    }

                    items(ideas) { idea ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = idea.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AzulEscuro,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = idea.category,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = idea.date,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    StatusBadge(status = idea.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val normalizedStatus = status.lowercase()
    val background = when (normalizedStatus) {
        "aprovada" -> Color(0xFFDDF5E6)
        "recusada" -> Color(0xFFFADDDD)
        else -> Color(0xFFFFF1CC)
    }
    val content = when (normalizedStatus) {
        "aprovada" -> Color(0xFF1B7F3B)
        "recusada" -> Color(0xFFB3261E)
        else -> Color(0xFF8A5A00)
    }

    Surface(
        color = background,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
    ) {
        Text(
            text = status,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun MinhasIdeiasScreenPreview() {
    Aguia_azulTheme {
        MinhasIdeiasScreen(onNavigateBack = {})
    }
}
