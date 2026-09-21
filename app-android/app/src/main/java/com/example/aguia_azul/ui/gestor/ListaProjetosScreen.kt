package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.aguia_azul.ui.components.EmptyState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val projetoFiltros = listOf(
    "Aprovados" to "Aprovados",
    "Em Producao" to "Em Producao",
    "Finalizados" to "Finalizados",
    "Rejeitados" to "Rejeitados"
)

private fun normalizeProjetoFiltro(filtro: String): String = when (filtro) {
    "EmProducao" -> "Em Producao"
    "Em Produção" -> "Em Producao"
    else -> filtro.ifBlank { "Aprovados" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProjetosScreen(
    initialFiltro: String,
    projects: List<ProjectApiModel>,
    onNavigateBack: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    var selectedFiltro by remember { mutableStateOf(normalizeProjetoFiltro(initialFiltro)) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(initialFiltro) {
        selectedFiltro = normalizeProjetoFiltro(initialFiltro)
    }

    val projetosVisiveis = projects
        .asSequence()
        .filter { project ->
            when (selectedFiltro) {
                "Todos" -> true
                "Aprovados" -> project.status == "Aprovado"
                "Em Producao" -> project.status == "Em Producao"
                "Finalizados" -> project.status == "Finalizado"
                "Rejeitados" -> project.status == "Rejeitado"
                else -> true
            }
        }
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .sortedByDescending { it.creationDate }
        .toList()

    Scaffold(
        containerColor = Branco,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Projetos",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                placeholder = { Text("Pesquisar projetos...") },
                singleLine = true
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(projetoFiltros) { (value, label) ->
                    FilterChip(
                        selected = selectedFiltro == value,
                        onClick = { selectedFiltro = value },
                        label = { Text(label) },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp
                )
            ) {
                if (projetosVisiveis.isEmpty()) {
                    item {
                        EmptyState(
                            message = "Nenhum projeto neste filtro",
                            hint = "Projetos nascem de ideias aprovadas: aprove uma ideia na pipeline e defina o orçamento para ela aparecer aqui."
                        )
                    }
                }

                items(projetosVisiveis) { projeto ->
                    ElevatedCard(
                        onClick = { onProjectClick(projeto.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = projeto.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AzulEscuro,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = projeto.unit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Criado em ${projeto.creationDate}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun ListaProjetosScreenPreview() {
    Aguia_azulTheme {
        ListaProjetosScreen(
            initialFiltro = "EmProducao",
            projects = emptyList(),
            onNavigateBack = {},
            onProjectClick = {}
        )
    }
}
