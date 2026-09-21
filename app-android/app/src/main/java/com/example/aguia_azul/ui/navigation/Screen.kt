package com.example.aguia_azul.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object RoleSelection : Screen("role_selection")

    // Operador
    object HomeOperador : Screen("home_operador")
    object CriarIdeia : Screen("criar_ideia")
    object MinhasIdeias : Screen("minhas_ideias")
    object MeuPerfilOperador : Screen("meu_perfil_operador")

    // Gestor
    object HomeGestor : Screen("home_gestor")
    object ListaProjetos : Screen("projetos?filtro={filtro}") {
        fun createRoute(filtro: String = "Aprovados") = "projetos?filtro=$filtro"
    }
    object DetalheProjetoGestor : Screen("detalhe_projeto_gestor/{projectId}") {
        fun createRoute(projectId: String) = "detalhe_projeto_gestor/$projectId"
    }
    object EstrategiasView : Screen("estrategias_view")
    object DetalheEstrategia : Screen("detalhe_estrategia")
    object Aprovacao : Screen("aprovacao/{ideaId}") {
        fun createRoute(ideaId: String) = "aprovacao/$ideaId"
    }
    object NovoProjeto : Screen("novo_projeto/{ideaId}") {
        fun createRoute(ideaId: String) = "novo_projeto/$ideaId"
    }
    object AcompanhamentoProjeto : Screen("acompanhamento_projeto/{ideaId}") {
        fun createRoute(ideaId: String) = "acompanhamento_projeto/$ideaId"
    }

    // Lideranca
    object HomeLideranca : Screen("home_lideranca")
    object ProjetosMacro : Screen("projetos_macro")
    object DetalheProjetoLideranca : Screen("detalhe_projeto_lideranca/{projectId}") {
        fun createRoute(projectId: String) = "detalhe_projeto_lideranca/$projectId"
    }
    object GerenciarEstrategia : Screen("gerenciar_estrategia")
}
