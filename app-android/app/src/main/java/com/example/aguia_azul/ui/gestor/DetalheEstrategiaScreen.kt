package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheEstrategiaScreen(
    title: String,
    publicationDate: String,
    description: String,
    onNavigateBack: () -> Unit
) {
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Branco)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = publicationDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
private fun DetalheEstrategiaScreenPreview() {
    Aguia_azulTheme {
        DetalheEstrategiaScreen(
            title = "Plano de Redução de Resíduos",
            publicationDate = "Publicado em 24/02/2025",
            description = "Esta estratégia foca na implementação de novos processos de reciclagem na linha de produção para reduzir o desperdício em 15% até o final do semestre.",
            onNavigateBack = {}
        )
    }
}
