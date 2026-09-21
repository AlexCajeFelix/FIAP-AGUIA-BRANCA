package com.example.aguia_azul.ui.lideranca

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.aguia_azul.ui.theme.AzulCeu
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.CinzaTexto

enum class LiderancaTab {
    Dashboard,
    ProjetosMacro,
    Gestao
}

@Composable
fun LiderancaBottomBar(
    currentTab: LiderancaTab,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProjetosMacro: () -> Unit,
    onNavigateToGestao: () -> Unit
) {
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
            selected = currentTab == LiderancaTab.Dashboard,
            onClick = onNavigateToDashboard,
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            colors = navigationColors
        )
        NavigationBarItem(
            selected = currentTab == LiderancaTab.ProjetosMacro,
            onClick = onNavigateToProjetosMacro,
            icon = { Icon(Icons.Default.Public, contentDescription = "Projetos Macro") },
            label = { Text("Projetos Macro") },
            colors = navigationColors
        )
        NavigationBarItem(
            selected = currentTab == LiderancaTab.Gestao,
            onClick = onNavigateToGestao,
            icon = { Icon(Icons.Default.Groups, contentDescription = "Gestão") },
            label = { Text("Gestão") },
            colors = navigationColors
        )
    }
}
