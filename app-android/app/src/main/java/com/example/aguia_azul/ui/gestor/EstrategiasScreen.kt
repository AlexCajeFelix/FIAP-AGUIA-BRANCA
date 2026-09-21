package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.data.StrategyApiModel
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.components.EmptyState
import com.example.aguia_azul.ui.theme.AzulNevoa
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstrategiasScreen(
    strategies: List<StrategyApiModel>,
    onNavigateBack: () -> Unit,
    onStrategyClick: (StrategyApiModel) -> Unit
) {
    Scaffold(
        containerColor = Branco,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Diretrizes Estratégicas",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (strategies.isEmpty()) {
                item {
                    EmptyState(
                        message = "Nenhuma diretriz publicada",
                        hint = "As diretrizes são definidas pela liderança e orientam quais ideias viram projeto. Assim que houver uma publicada, ela aparece aqui."
                    )
                }
            }

            items(strategies) { strategy ->
                ElevatedCard(
                    onClick = { onStrategyClick(strategy) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strategy.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AzulEscuro
                        )
                        Text(
                            text = strategy.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Horizonte e data sao dados que a API ja devolve e ninguem mostrava.
                        HorizonteChip(horizon = strategy.horizon, date = strategy.date)
                    }
                }
            }
        }
    }
}

/** Curto, medio ou longo prazo — o campo horizon do contrato, em portugues. */
@Composable
private fun HorizonteChip(horizon: String, date: String) {
    val rotulo = when (horizon.uppercase()) {
        "SHORT" -> "Curto prazo"
        "MEDIUM" -> "Médio prazo"
        "LONG" -> "Longo prazo"
        else -> "Sem prazo definido"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(AzulNevoa, androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelSmall,
                color = AzulEscuro
            )
        }
        if (date.isNotBlank()) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun EstrategiasScreenPreview() {
    Aguia_azulTheme {
        EstrategiasScreen(
            strategies = emptyList(),
            onNavigateBack = {},
            onStrategyClick = {}
        )
    }
}
