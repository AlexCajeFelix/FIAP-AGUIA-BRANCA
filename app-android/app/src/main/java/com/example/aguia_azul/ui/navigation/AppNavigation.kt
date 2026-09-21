package com.example.aguia_azul.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.data.Session
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aguia_azul.ui.auth.AuthViewModel
import com.example.aguia_azul.ui.auth.LoginScreen
import com.example.aguia_azul.ui.auth.RoleSelectionScreen
import com.example.aguia_azul.ui.gestor.AcompanhamentoProjetoScreen
import com.example.aguia_azul.ui.gestor.AprovacaoScreen
import com.example.aguia_azul.ui.gestor.DetalheEstrategiaScreen
import com.example.aguia_azul.ui.gestor.DetalheProjetoGestorScreen
import com.example.aguia_azul.ui.gestor.EstrategiasScreen
import com.example.aguia_azul.ui.gestor.GestorViewModel
import com.example.aguia_azul.ui.gestor.HomeGestorScreen
import com.example.aguia_azul.ui.gestor.ListaProjetosScreen
import com.example.aguia_azul.ui.gestor.NovoProjetoScreen
import com.example.aguia_azul.ui.lideranca.DetalheProjetoLiderancaScreen
import com.example.aguia_azul.ui.lideranca.GerenciarEstrategiaScreen
import com.example.aguia_azul.ui.lideranca.HomeLiderancaScreen
import com.example.aguia_azul.ui.lideranca.LiderancaViewModel
import com.example.aguia_azul.ui.lideranca.ProjetosMacroScreen
import com.example.aguia_azul.ui.operador.CriarIdeiaScreen
import com.example.aguia_azul.ui.operador.HomeOperadorScreen
import com.example.aguia_azul.ui.operador.MeuPerfilOperadorScreen
import com.example.aguia_azul.ui.operador.MinhasIdeiasScreen
import com.example.aguia_azul.ui.operador.OperadorViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val sessionExpired by Session.expired.collectAsState()

    // Quando o refresh e recusado (expirado, ou familia revogada por reuso), a sessao cai no
    // meio de qualquer chamada. Sem isto a tela ficaria repetindo "sessao expirada" sem
    // caminho de volta.
    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            Session.consumeExpired()
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val authViewModel: AuthViewModel = viewModel()

    // Sair limpa a sessao no servidor (revoga o refresh) e volta para a escolha de perfil,
    // zerando a pilha: com o back a pessoa voltaria para uma tela que nao pode mais carregar.
    val sair: () -> Unit = {
        authViewModel.logout {
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val operadorViewModel: OperadorViewModel = viewModel()
    val gestorViewModel: GestorViewModel = viewModel()
    val liderancaViewModel: LiderancaViewModel = viewModel()
    val gestorProjects by gestorViewModel.projects.collectAsState()
    val liderancaProjects by liderancaViewModel.projects.collectAsState()
    val liderancaStrategies by liderancaViewModel.strategies.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        "operador" -> Screen.HomeOperador.route
                        "gestor" -> Screen.HomeGestor.route
                        "lideranca" -> Screen.HomeLideranca.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RoleSelection.route) {
            // A escolha aqui e so o caminho ate o login: quem decide o perfil e o servidor,
            // no /auth/login. Antes, este card definia em qual area a pessoa entrava.
            RoleSelectionScreen(
                onRoleSelected = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.HomeOperador.route) {
            HomeOperadorScreen(
                onNavigateToCriarIdeia = {
                    navController.navigate(Screen.CriarIdeia.route)
                },
                onNavigateToMinhasIdeias = {
                    navController.navigate(Screen.MinhasIdeias.route)
                },
                onNavigateToEstrategias = {
                    navController.navigate(Screen.EstrategiasView.route)
                },
                onLogout = sair,
                onNavigateToPerfil = {
                    navController.navigate(Screen.MeuPerfilOperador.route)
                },
                viewModel = operadorViewModel
            )
        }
        composable(Screen.CriarIdeia.route) {
            CriarIdeiaScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = operadorViewModel
            )
        }
        composable(Screen.MinhasIdeias.route) {
            MinhasIdeiasScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCriarIdeia = { navController.navigate(Screen.CriarIdeia.route) },
                viewModel = operadorViewModel
            )
        }
        composable(Screen.MeuPerfilOperador.route) {
            MeuPerfilOperadorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HomeGestor.route) {
            HomeGestorScreen(
                onNavigateToAprovacao = { ideaId ->
                    navController.navigate(Screen.Aprovacao.createRoute(ideaId))
                },
                onNavigateToAcompanhamento = { ideaId ->
                    navController.navigate(Screen.AcompanhamentoProjeto.createRoute(ideaId))
                },
                onNavigateToProjetos = { filtro ->
                    navController.navigate(Screen.ListaProjetos.createRoute(filtro))
                },
                onNavigateToEstrategias = {
                    navController.navigate(Screen.EstrategiasView.route)
                },
                onLogout = sair,
                viewModel = gestorViewModel
            )
        }
        composable(
            route = Screen.ListaProjetos.route,
            arguments = listOf(
                navArgument("filtro") {
                    type = NavType.StringType
                    defaultValue = "Aprovados"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val filtro = backStackEntry.arguments?.getString("filtro") ?: "Aprovados"
            ListaProjetosScreen(
                initialFiltro = filtro,
                projects = gestorProjects,
                onNavigateBack = { navController.popBackStack() },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.DetalheProjetoGestor.createRoute(projectId))
                }
            )
        }
        composable(Screen.DetalheProjetoGestor.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            DetalheProjetoGestorScreen(
                project = gestorProjects.firstOrNull { it.id == projectId },
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.AcompanhamentoProjeto.createRoute(id))
                }
            )
        }
        composable(Screen.EstrategiasView.route) {
            LaunchedEffect(Unit) { liderancaViewModel.loadData() }

            EstrategiasScreen(
                strategies = liderancaStrategies,
                onNavigateBack = { navController.popBackStack() },
                onStrategyClick = { strategy ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("strategy_title", strategy.title)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("strategy_date", strategy.date)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("strategy_description", strategy.description)
                    navController.navigate(Screen.DetalheEstrategia.route)
                }
            )
        }
        composable(Screen.DetalheEstrategia.route) {
            val strategyState = navController.previousBackStackEntry?.savedStateHandle
            val title = strategyState?.get<String>("strategy_title") ?: "Diretriz estratégica"
            val date = strategyState?.get<String>("strategy_date").orEmpty()
            val description = strategyState?.get<String>("strategy_description")
                ?: "Nenhum conteúdo disponível."

            DetalheEstrategiaScreen(
                title = title,
                publicationDate = date,
                description = description,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Aprovacao.route) { backStackEntry ->
            val ideaId = backStackEntry.arguments?.getString("ideaId") ?: ""
            AprovacaoScreen(
                ideaId = ideaId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNovoProjeto = { id ->
                    navController.navigate(Screen.NovoProjeto.createRoute(id))
                },
                viewModel = gestorViewModel
            )
        }
        composable(Screen.NovoProjeto.route) { backStackEntry ->
            val ideaId = backStackEntry.arguments?.getString("ideaId") ?: ""
            NovoProjetoScreen(
                ideaId = ideaId,
                onNavigateBack = {
                    navController.popBackStack(Screen.HomeGestor.route, false)
                },
                viewModel = gestorViewModel
            )
        }
        composable(Screen.AcompanhamentoProjeto.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("ideaId") ?: ""
            AcompanhamentoProjetoScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = gestorViewModel
            )
        }

        composable(Screen.HomeLideranca.route) {
            HomeLiderancaScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.HomeLideranca.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProjetosMacro = {
                    navController.navigate(Screen.ProjetosMacro.route)
                },
                onNavigateToEstrategias = {
                    navController.navigate(Screen.GerenciarEstrategia.route)
                },
                onLogout = sair,
                viewModel = liderancaViewModel
            )
        }
        composable(Screen.ProjetosMacro.route) {
            ProjetosMacroScreen(
                projects = liderancaProjects,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.HomeLideranca.route)
                },
                onNavigateToProjetosMacro = {
                    navController.navigate(Screen.ProjetosMacro.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToGestao = {
                    navController.navigate(Screen.GerenciarEstrategia.route)
                },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.DetalheProjetoLideranca.createRoute(projectId))
                }
            )
        }
        composable(Screen.DetalheProjetoLideranca.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            DetalheProjetoLiderancaScreen(
                project = liderancaProjects.firstOrNull { it.id == projectId },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GerenciarEstrategia.route) {
            GerenciarEstrategiaScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.HomeLideranca.route)
                },
                onNavigateToProjetosMacro = {
                    navController.navigate(Screen.ProjetosMacro.route)
                },
                onNavigateToGestao = {
                    navController.navigate(Screen.GerenciarEstrategia.route) {
                        launchSingleTop = true
                    }
                },
                viewModel = liderancaViewModel
            )
        }
    }
}
