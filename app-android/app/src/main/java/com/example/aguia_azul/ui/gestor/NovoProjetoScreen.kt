package com.example.aguia_azul.ui.gestor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aguia_azul.ui.components.AguiaTextField
import com.example.aguia_azul.ui.components.CurvedHeader
import com.example.aguia_azul.ui.components.ErrorState
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

/**
 * Promocao de ideia aprovada a projeto.
 *
 * Antes o botao "Salvar Projeto" apenas voltava para a tela anterior: nada saia do aparelho.
 * Agora ele chama POST /projects/from-idea/{ideaId}, e o unico dado que a API guarda na
 * promocao e o orcamento — os campos de ROI e prazo sairam porque nao tinham onde ser
 * gravados, e campo que some ao salvar e pior do que campo que nao existe.
 */
@Composable
fun NovoProjetoScreen(
    ideaId: String,
    onNavigateBack: () -> Unit,
    viewModel: GestorViewModel
) {
    var budget by remember { mutableStateOf("") }
    val isSaving by viewModel.isSavingProject.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val idea = viewModel.getIdeaById(ideaId)

    val budgetValue = budget.trim().replace(".", "").replace(",", ".").toDoubleOrNull()
    val isFormValid = budgetValue != null && budgetValue > 0 && !isSaving

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
                text = idea?.title ?: "Nova frente",
                style = MaterialTheme.typography.headlineMedium,
                color = AzulEscuro,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "A ideia aprovada vira projeto com o orcamento definido aqui. " +
                    "O progresso comeca em zero e e atualizado no acompanhamento.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            errorMessage?.let { message ->
                ErrorState(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }

            AguiaTextField(
                value = budget,
                onValueChange = { budget = it },
                label = "Orçamento do projeto (R$)",
                leadingIcon = Icons.Default.AttachMoney,
                supportingText = "O progresso começa em zero e é atualizado no acompanhamento.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.promoteIdea(ideaId, budgetValue ?: 0.0, onSuccess = onNavigateBack)
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
                    Text("Criar Projeto")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun NovoProjetoScreenPreview() {
    Aguia_azulTheme {
        NovoProjetoScreen(ideaId = "1", onNavigateBack = {}, viewModel = GestorViewModel())
    }
}
