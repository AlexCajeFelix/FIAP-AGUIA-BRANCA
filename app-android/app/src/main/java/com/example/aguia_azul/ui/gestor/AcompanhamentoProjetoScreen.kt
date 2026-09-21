package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.ui.components.AguiaTextField
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.components.ErrorState
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

/**
 * Acompanhamento do projeto: progresso e gasto.
 *
 * Os campos anteriores (etapa, status, ROI alcancado, economia realizada) nao existiam do lado
 * do servidor — o botao "Salvar" so voltava para a tela anterior. Aqui ficaram os dois que a
 * API realmente grava, e o status deixou de ser campo editavel porque ele e consequencia do
 * progresso: 0 planeja, 1 a 99 executa, 100 conclui.
 */
@Composable
fun AcompanhamentoProjetoScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: GestorViewModel
) {
    val projects by viewModel.projects.collectAsState()
    val project = projects.firstOrNull { it.id == projectId }

    var progress by remember(project) { mutableStateOf(project?.progressPercent?.toString() ?: "") }
    var spent by remember(project) { mutableStateOf(project?.spentValue?.toString() ?: "") }

    val isSaving by viewModel.isSavingProject.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val progressValue = progress.trim().toIntOrNull()
    val spentValue = spent.trim().replace(",", ".").toDoubleOrNull()
    val progressIsValid = progress.isBlank() || (progressValue != null && progressValue in 0..100)
    val spentIsValid = spent.isBlank() || (spentValue != null && spentValue >= 0)
    // A API recusa PATCH sem nenhuma metrica; o botao nem deixa chegar la.
    val isFormValid = (progress.isNotBlank() || spent.isNotBlank()) &&
        progressIsValid && spentIsValid && !isSaving

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = project?.title ?: "Projeto $projectId",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Status atual: ${project?.status ?: "—"} · Orcamento ${project?.investment ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            errorMessage?.let { message ->
                ErrorState(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }

            AguiaTextField(
                value = progress,
                onValueChange = { progress = it.filter(Char::isDigit) },
                label = "Progresso (%)",
                leadingIcon = Icons.Default.Percent,
                supportingText = "0 a 100. Em 100 o projeto passa a Finalizado.",
                isError = !progressIsValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AguiaTextField(
                value = spent,
                onValueChange = { spent = it },
                label = "Gasto acumulado (R$)",
                leadingIcon = Icons.Default.AttachMoney,
                supportingText = "Cada alteração vira um registro no histórico do projeto.",
                isError = !spentIsValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateMetrics(
                        projectId = projectId,
                        progress = progressValue,
                        spent = spentValue,
                        onSuccess = onNavigateBack
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulEscuro,
                    contentColor = Branco,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Branco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salvar Acompanhamento")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AcompanhamentoProjetoScreenPreview() {
    Aguia_azulTheme {
        AcompanhamentoProjetoScreen(
            projectId = "1",
            onNavigateBack = {},
            viewModel = GestorViewModel()
        )
    }
}
