package com.futbol.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futbol.app.data.model.Entrenador
import com.futbol.app.data.model.EquipoSimple
import com.futbol.app.ui.components.*
import com.futbol.app.ui.components.FotoUrlInput
import com.futbol.app.viewmodel.EntrenadoresUiState
import com.futbol.app.viewmodel.EntrenadoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenadoresScreen(
    navController: NavController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: EntrenadoresViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var entrenadorToEdit by remember { mutableStateOf<Entrenador?>(null) }
    var entrenadorToDelete by remember { mutableStateOf<Entrenador?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenadores", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(if (darkTheme) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                            null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is EntrenadoresUiState.Loading -> LoadingScreen()
                is EntrenadoresUiState.Error -> ErrorScreen(state.message, { viewModel.cargarEntrenadores() })
                is EntrenadoresUiState.Success -> {
                    if (state.entrenadores.isEmpty()) {
                        EmptyScreen("No hay entrenadores registrados")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
                            items(state.entrenadores, key = { it.idEntrenador }) { entrenador ->
                                EntrenadorCard(
                                    entrenador = entrenador,
                                    onEdit = { entrenadorToEdit = entrenador },
                                    onDelete = { entrenadorToDelete = entrenador }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        EntrenadorBottomSheet(
            title = "Nuevo Entrenador",
            onDismiss = { showAddSheet = false },
            onConfirm = { ent, onDone ->
                viewModel.crearEntrenador(ent,
                    onSuccess = { onDone(true); showAddSheet = false; snackbarMessage = "✅ Entrenador creado correctamente" },
                    onError   = { onDone(false); snackbarMessage = "❌ Error: $it" })
            }
        )
    }

    entrenadorToEdit?.let { ent ->
        EntrenadorBottomSheet(
            title = "Editar Entrenador",
            entrenadorInicial = ent,
            onDismiss = { entrenadorToEdit = null },
            onConfirm = { updated, onDone ->
                viewModel.actualizarEntrenador(ent.idEntrenador, updated,
                    onSuccess = { onDone(true); entrenadorToEdit = null; snackbarMessage = "✅ Entrenador actualizado" },
                    onError   = { onDone(false); snackbarMessage = "❌ Error: $it" })
            }
        )
    }

    entrenadorToDelete?.let { ent ->
        ConfirmDialog(
            title = "Eliminar Entrenador",
            message = "¿Eliminar al entrenador ${ent.nombre}?",
            onConfirm = {
                viewModel.eliminarEntrenador(ent.idEntrenador,
                    onSuccess = { snackbarMessage = "🗑️ Entrenador eliminado" },
                    onError   = { snackbarMessage = "❌ Error al eliminar" })
                entrenadorToDelete = null
            },
            onDismiss = { entrenadorToDelete = null }, isDestructive = true
        )
    }
}

@Composable
fun EntrenadorCard(entrenador: Entrenador, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!entrenador.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = entrenador.fotoUrl,
                        contentDescription = entrenador.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.RecordVoiceOver, null,
                        tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entrenador.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(entrenador.especialidad, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    entrenador.equipo?.nombre?.let { "Equipo: $it" } ?: "Equipo ID: ${entrenador.idEquipo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenadorBottomSheet(
    title: String,
    entrenadorInicial: Entrenador? = null,
    onDismiss: () -> Unit,
    onConfirm: (Entrenador, onDone: (Boolean) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf(entrenadorInicial?.nombre ?: "") }
    var especialidad by remember { mutableStateOf(entrenadorInicial?.especialidad ?: "") }
    var idEquipoText by remember { mutableStateOf(entrenadorInicial?.idEquipo?.toString() ?: "") }
    var fotoUrl by remember { mutableStateOf(entrenadorInicial?.fotoUrl ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val nombreError = nombre.isBlank()
    val especialidadError = especialidad.isBlank()
    val equipoError = idEquipoText.toIntOrNull() == null
    val isValid = !nombreError && !especialidadError && !equipoError

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it; errorMsg = null },
                label = { Text("Nombre completo") },
                isError = nombreError && nombre.isNotEmpty(),
                supportingText = { if (nombreError && nombre.isNotEmpty()) Text("Campo requerido") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = especialidad, onValueChange = { especialidad = it; errorMsg = null },
                label = { Text("Especialidad") },
                isError = especialidadError && especialidad.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Ej: Preparación física, Porteros...") },
                leadingIcon = { Icon(Icons.Default.Star, null) }
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = idEquipoText, onValueChange = { idEquipoText = it; errorMsg = null },
                label = { Text("ID del Equipo") },
                isError = idEquipoText.isNotBlank() && equipoError,
                supportingText = { if (idEquipoText.isNotBlank() && equipoError) Text("Debe ser un número válido") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Groups, null) }
            )
            Spacer(Modifier.height(10.dp))
            FotoUrlInput(
                value = fotoUrl,
                onValueChange = { fotoUrl = it },
                modifier = Modifier.fillMaxWidth()
            )
            errorMsg?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isValid) {
                        isSaving = true
                        errorMsg = null
                        onConfirm(
                            Entrenador(
                                idEntrenador = entrenadorInicial?.idEntrenador ?: 0,
                                nombre = nombre,
                                especialidad = especialidad,
                                fotoUrl = fotoUrl.trim().takeIf { it.isNotBlank() },
                                equipo = EquipoSimple(idEquipo = idEquipoText.toInt(), nombre = "")
                            )
                        ) { success ->
                            isSaving = false
                            if (!success) errorMsg = "No se pudo guardar. Intenta de nuevo."
                        }
                    }
                },
                enabled = isValid && !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                else Text("Guardar", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { if (!isSaving) onDismiss() },
                modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}