package com.futbol.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.futbol.app.ui.components.FotoUrlInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futbol.app.data.model.*
import com.futbol.app.data.repository.*
import com.futbol.app.navigation.Screen
import com.futbol.app.ui.components.*
import com.futbol.app.viewmodel.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposScreen(
    navController: NavController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: EquiposViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
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
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is EquiposUiState.Loading -> LoadingScreen()
                is EquiposUiState.Error -> ErrorScreen(state.message, { viewModel.cargarEquipos() })
                is EquiposUiState.Success -> {
                    if (state.equipos.isEmpty()) {
                        EmptyScreen("No hay equipos registrados")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
                            items(state.equipos, key = { it.idEquipo }) { equipo ->
                                EquipoCard(equipo = equipo,
                                    onClick = { navController.navigate(Screen.EquipoDetail.createRoute(equipo.idEquipo)) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        EquipoBottomSheet(
            title = "Nuevo Equipo",
            onDismiss = { showAddSheet = false },
            onConfirm = { equipo, onDone ->
                viewModel.crearEquipo(equipo,
                    onSuccess = { onDone(true); showAddSheet = false },
                    onError   = { onDone(false) })
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoCard(equipo: Equipo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                if (!equipo.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = equipo.fotoUrl,
                        contentDescription = equipo.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${equipo.ciudad} · Fundado: ${equipo.fundacion}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoBottomSheet(
    title: String,
    equipoInicial: Equipo? = null,
    onDismiss: () -> Unit,
    onConfirm: (Equipo, onDone: (Boolean) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf(equipoInicial?.nombre ?: "") }
    var ciudad by remember { mutableStateOf(equipoInicial?.ciudad ?: "") }
    var fundacion by remember { mutableStateOf(equipoInicial?.fundacion ?: "") }
    var fotoUrl by remember { mutableStateOf(equipoInicial?.fotoUrl ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val isValid = nombre.isNotBlank() && ciudad.isNotBlank() && fundacion.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it; errorMsg = null },
                label = { Text("Nombre del equipo") }, isError = nombre.isBlank(),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Groups, null) })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = ciudad, onValueChange = { ciudad = it; errorMsg = null },
                label = { Text("Ciudad") }, isError = ciudad.isBlank(),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.LocationCity, null) })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = fundacion, onValueChange = { fundacion = it; errorMsg = null },
                label = { Text("Fecha de fundación (YYYY-MM-DD)") }, isError = fundacion.isBlank(),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) })
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
                    isSaving = true
                    errorMsg = null
                    onConfirm(
                        Equipo(idEquipo = equipoInicial?.idEquipo ?: 0,
                            nombre = nombre, ciudad = ciudad, fundacion = fundacion,
                            fotoUrl = fotoUrl.trim().takeIf { it.isNotBlank() })
                    ) { success ->
                        isSaving = false
                        if (!success) errorMsg = "No se pudo guardar. Intenta de nuevo."
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

//EquipoDetailScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoDetailScreen(
    equipoId: Int, navController: NavController, viewModel: EquiposViewModel = viewModel()
) {
    val equipo by viewModel.selectedEquipo.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

    var jugadores by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var entrenadores by remember { mutableStateOf<List<Entrenador>>(emptyList()) }
    var golesTotal by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    val animatedGoles by animateIntAsState(
        targetValue = golesTotal,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "goles_anim"
    )

    LaunchedEffect(equipoId) {
        viewModel.cargarEquipoById(equipoId)
        cargando = true

        val jRepo   = JugadorRepository()
        val eRepo   = EntrenadorRepository()
        val estRepo = EstadisticasRepository()

        val deferredJugadores    = async { jRepo.getJugadores() }
        val deferredEstadisticas = async { estRepo.getEstadisticas() }
        val deferredEntrenadores = async { eRepo.getEntrenadores() }

        val resJugadores    = deferredJugadores.await()
        val resEstadisticas = deferredEstadisticas.await()
        val resEntrenadores = deferredEntrenadores.await()

        val jugadoresDelEquipo = when (resJugadores) {
            is Result.Success -> resJugadores.data.filter { j -> j.equipo?.idEquipo == equipoId }
            else -> emptyList()
        }
        jugadores = jugadoresDelEquipo

        val idsJugadores = jugadoresDelEquipo.map { it.idJugador }.toSet()
        golesTotal = when (resEstadisticas) {
            is Result.Success -> resEstadisticas.data
                .filter { est -> (est.jugador?.idJugador ?: 0) in idsJugadores }
                .sumOf { it.goles }
            else -> 0
        }

        entrenadores = when (resEntrenadores) {
            is Result.Success -> resEntrenadores.data.filter { e -> e.equipo?.idEquipo == equipoId }
            else -> emptyList()
        }

        cargando = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(equipo?.nombre ?: "Equipo", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        equipo?.let { e ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)) {

                item {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!e.fotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = e.fotoUrl,
                                    contentDescription = e.nombre,
                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Groups, null, Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(e.nombre, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black)
                            Text("${e.ciudad} · ${e.fundacion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    if (cargando) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Cargando estadísticas...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("Jugadores", jugadores.size.toString(), Modifier.weight(1f))
                            StatCard("Entrenadores", entrenadores.size.toString(), Modifier.weight(1f))
                            StatCard("Goles Totales", animatedGoles.toString(), Modifier.weight(1f),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                if (!cargando) {
                    if (jugadores.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Row(modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Jugadores (${jugadores.size})",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        items(jugadores, key = { it.idJugador }) { jugador ->
                            JugadorCard(jugador = jugador, onClick = {
                                navController.navigate(Screen.JugadorDetail.createRoute(jugador.idJugador))
                            })
                        }
                    } else {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Sin jugadores asignados a este equipo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (entrenadores.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            Row(modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecordVoiceOver, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Cuerpo Técnico",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        items(entrenadores, key = { it.idEntrenador }) { ent ->
                            EntrenadorCard(entrenador = ent, onEdit = {}, onDelete = {})
                        }
                    }
                }
            }
        } ?: LoadingScreen(modifier = Modifier.padding(padding))
    }

    if (showDeleteDialog) {
        ConfirmDialog(title = "Eliminar Equipo",
            message = "¿Eliminar ${equipo?.nombre}? Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.eliminarEquipo(equipoId, onSuccess = { navController.popBackStack() }, onError = {})
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }, isDestructive = true)
    }
    if (showEditSheet && equipo != null) {
        EquipoBottomSheet(
            title = "Editar Equipo",
            equipoInicial = equipo,
            onDismiss = { showEditSheet = false },
            onConfirm = { updated, onDone ->
                viewModel.actualizarEquipo(equipoId, updated,
                    onSuccess = { onDone(true); showEditSheet = false; viewModel.cargarEquipoById(equipoId) },
                    onError   = { onDone(false) })
            })
    }
}