package com.example.aguia_azul.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.R
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulClaro
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.AzulProfundo
import com.example.aguia_azul.ui.theme.Branco

private val HeaderShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Scaffold(
        topBar = {
            RoleSelectionHeader()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bem-vindo",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(46.dp))

            Text(
                text = "Escolha seu perfil",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            RoleButton(
                title = "Operador",
                icon = Icons.Default.Engineering,
                onClick = { onRoleSelected("operador") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoleButton(
                title = "Gestor",
                icon = Icons.Default.ManageAccounts,
                onClick = { onRoleSelected("gestor") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoleButton(
                title = "Liderança",
                icon = Icons.Default.Leaderboard,
                onClick = { onRoleSelected("lideranca") }
            )
        }
    }
}

@Composable
private fun RoleSelectionHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(HeaderShape)
            .background(Brush.linearGradient(listOf(AzulProfundo, AzulEscuro)))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_branca_contorno),
            contentDescription = "Logo Águia Azul",
            modifier = Modifier.height(150.dp)
        )
    }
}

@Composable
fun RoleButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(20.dp),
        color = AzulEscuro.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, AzulEscuro.copy(alpha = 0.1f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Surface(
                color = AzulEscuro,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Branco,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = AzulEscuro
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun RoleSelectionScreenPreview() {
    Aguia_azulTheme {
        RoleSelectionScreen(onRoleSelected = {})
    }
}
