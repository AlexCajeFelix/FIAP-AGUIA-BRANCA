package com.example.aguia_azul.ui.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeuPerfilOperadorScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meu Perfil",
                        color = AzulEscuro
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
        containerColor = Branco
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AzulEscuro,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                SmallFloatingActionButton(
                    onClick = {},
                    modifier = Modifier.align(Alignment.BottomEnd),
                    containerColor = AzulEscuro,
                    contentColor = Branco
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar foto"
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "João Silva",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Operador",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.size(24.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Branco),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow(
                        icon = Icons.Default.Badge,
                        label = "Matrícula",
                        value = "12345-X"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    InfoRow(
                        icon = Icons.Default.Email,
                        label = "E-mail",
                        value = "joao.silva@aguiabranca.com.br"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Unidade",
                        value = "Garagem Vitória"
                    )
                }
            }

            Spacer(modifier = Modifier.size(32.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulEscuro)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
                Text(
                    text = "Alterar Senha",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            TextButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sair da Conta",
                    color = Color(0xFFC62828),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AzulEscuro.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AzulEscuro
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun MeuPerfilOperadorScreenPreview() {
    Aguia_azulTheme {
        MeuPerfilOperadorScreen(onNavigateBack = {})
    }
}
