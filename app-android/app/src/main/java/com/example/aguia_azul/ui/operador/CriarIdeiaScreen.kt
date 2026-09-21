package com.example.aguia_azul.ui.operador

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aguia_azul.ui.theme.Aguia_azulTheme
import com.example.aguia_azul.ui.components.AguiaTextField
import com.example.aguia_azul.ui.components.ErrorState
import com.example.aguia_azul.ui.theme.AzulEscuro
import com.example.aguia_azul.ui.theme.Branco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarIdeiaScreen(
    onNavigateBack: () -> Unit,
    viewModel: OperadorViewModel = viewModel()
) {
    val categorias = listOf(
        "Logística e Rotas",
        "Manutenção de Frota",
        "Atendimento ao Passageiro",
        "Segurança Operacional",
        "Processos Internos",
        "Sustentabilidade"
    )
    var tipoContribuicao by remember { mutableStateOf("Ideia") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoriaSelecionada by remember { mutableStateOf(categorias[0]) }
    var expanded by remember { mutableStateOf(false) }
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isSuggesting by viewModel.isSuggesting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isFormValid = title.isNotBlank() && description.isNotBlank() && !isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar Ideia",
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
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Ideia" to "Sugerir Ideia", "Problema" to "Relatar Problema").forEach { (value, label) ->
                    val activeColor = if (value == "Problema") Color(0xFFC62828) else AzulEscuro
                    OutlinedButton(
                        onClick = { tipoContribuicao = value },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (tipoContribuicao == value) activeColor else Branco,
                            contentColor = if (tipoContribuicao == value) Branco else activeColor
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AguiaTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título da ideia ou problema",
                leadingIcon = Icons.Default.Lightbulb
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = categoriaSelecionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria/Tipo") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                categoriaSelecionada = categoria
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AguiaTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descrição",
                leadingIcon = Icons.AutoMirrored.Filled.Notes,
                singleLine = false,
                minLines = 5,
                modifier = Modifier.height(190.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // A chamada vai para a nossa API, que fala com o Gemini com a chave guardada no
            // servidor. O texto volta para o campo e a pessoa edita antes de enviar.
            TextButton(
                onClick = {
                    viewModel.improveDescription(title, description) { description = it }
                },
                enabled = title.isNotBlank() && description.isNotBlank() && !isSuggesting,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isSuggesting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    Text(text = "Melhorando...", modifier = Modifier.padding(start = 8.dp))
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Text(text = "Melhorar com IA", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null
                )
                Text(
                    text = "Anexar Imagem ou Vídeo",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { message ->
                ErrorState(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    // A API guarda titulo e descricao; tipo e categoria nao tem campo no
                    // contrato. Em vez de descartar a escolha da pessoa em silencio, ela vai
                    // no fim da descricao — ate o backend ganhar esses campos.
                    val descricaoCompleta = buildString {
                        append(description.trim())
                        append("\n\n")
                        append("Tipo: ")
                        append(tipoContribuicao)
                        append(" · Categoria: ")
                        append(categoriaSelecionada)
                    }
                    // Só sai da tela quando o servidor confirmou: navegar antes fazia a ideia
                    // parecer enviada mesmo quando a chamada falhava.
                    viewModel.addIdea(title, descricaoCompleta) { onNavigateBack() }
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
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = Branco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar Ideia")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4)
@Composable
private fun CriarIdeiaScreenPreview() {
    Aguia_azulTheme {
        CriarIdeiaScreen(onNavigateBack = {})
    }
}
