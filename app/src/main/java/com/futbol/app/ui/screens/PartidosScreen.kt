package com.futbol.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futbol.app.data.model.Equipo
import com.futbol.app.data.model.EquipoSimple
import com.futbol.app.data.model.EstadisticasJugador
import com.futbol.app.data.model.Partido
import com.futbol.app.data.repository.EquipoRepository
import com.futbol.app.data.repository.EstadisticasRepository
import com.futbol.app.data.repository.Result
import com.futbol.app.ui.components.*
import com.futbol.app.viewmodel.PartidosUiState
import com.futbol.app.viewmodel.PartidosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidosScreen(
    navController: NavController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: PartidosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Resultados", "Próximos")
    var showAddSheet by remember { mutableStateOf(false) }
    var partidoToEdit by remember { mutableStateOf<Partido?>(null) }
    var partidoToDelete by remember { mutableStateOf<Partido?>(null) }
    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partidos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
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
            AnimatedVisibility(visible = fabVisible,
                enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                exit = scaleOut(tween(200)) + fadeOut(tween(200))) {
                FloatingActionButton(onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            when (val state = uiState) {
                is PartidosUiState.Loading -> LoadingScreen()
                is PartidosUiState.Error -> ErrorScreen(state.message, { viewModel.cargarPartidos() })
                is PartidosUiState.Success -> {
                    val today = try { java.time.LocalDate.now().toString() } catch (e: Exception) { "2025-01-01" }
                    val resultados = state.partidos.filter { it.fecha <= today }
                    val proximos = state.partidos.filter { it.fecha > today }
                    val lista = if (selectedTab == 0) resultados else proximos

                    if (lista.isEmpty()) {
                        EmptyScreen(if (selectedTab == 0) "No hay resultados registrados" else "No hay próximos partidos")
                    } else {
                        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
                            items(lista, key = { it.idPartido }) { partido ->
                                if (selectedTab == 0) {
                                    ResultadoCard(
                                        partido = partido,
                                        onEdit = { partidoToEdit = partido },
                                        onDelete = { partidoToDelete = partido }
                                    )
                                } else {
                                    ProximoPartidoCard(partido = partido)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        PartidoBottomSheet(title = "Nuevo Partido", onDismiss = { showAddSheet = false },
            onConfirm = { p, onDone -> viewModel.crearPartido(p, onSuccess = { onDone(true); showAddSheet = false }, onError = { onDone(false) }) })
    }
    partidoToEdit?.let { p ->
        PartidoBottomSheet(title = "Editar Partido", partidoInicial = p, onDismiss = { partidoToEdit = null },
            onConfirm = { updated, onDone -> viewModel.actualizarPartido(p.idPartido, updated, onSuccess = { onDone(true); partidoToEdit = null }, onError = { onDone(false) }) })
    }
    partidoToDelete?.let { p ->
        ConfirmDialog(
            title = "Eliminar Partido",
            message = "¿Eliminar el partido del ${p.fecha}?",
            onConfirm = { viewModel.eliminarPartido(p.idPartido, onSuccess = {}, onError = {}); partidoToDelete = null },
            onDismiss = { partidoToDelete = null }, isDestructive = true
        )
    }
}

@Composable
fun ResultadoCard(partido: Partido, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var estadisticasPartido by remember { mutableStateOf<List<EstadisticasJugador>>(emptyList()) }
    var cargandoStats by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val animatedGolesLocal by animateIntAsState(targetValue = partido.golesLocal,
        animationSpec = tween(800, easing = FastOutSlowInEasing), label = "gl")
    val animatedGolesVisita by animateIntAsState(targetValue = partido.golesVisita,
        animationSpec = tween(800, easing = FastOutSlowInEasing), label = "gv")

    val (badgeText, badgeColor) = when {
        partido.golesLocal > partido.golesVisita -> "LOCAL GANA" to Color(0xFF1565C0)
        partido.golesLocal < partido.golesVisita -> "VISITA GANA" to Color(0xFFE63946)
        else -> "EMPATE" to Color(0xFF6C757D)
    }

    // Estadísticas simuladas por partido
    val cornerLocal = (partido.golesLocal * 2 + 3)
    val cornerVisita = (partido.golesVisita * 2 + 2)
    val faltasLocal = (partido.golesVisita * 3 + 8)
    val faltasVisita = (partido.golesLocal * 3 + 6)
    val pasesLocal = 350 + partido.golesLocal * 20
    val pasesVisita = 300 + partido.golesVisita * 20
    val tirosLocal = partido.golesLocal * 3 + 4
    val tirosVisita = partido.golesVisita * 3 + 3

    Card(
        onClick = {
            expanded = !expanded
            if (expanded && estadisticasPartido.isEmpty() && !cargandoStats) {
                cargandoStats = true
                scope.launch {
                    val repo = EstadisticasRepository()
                    when (val r = repo.getEstadisticasByPartido(partido.idPartido)) {
                        is Result.Success -> { estadisticasPartido = r.data; cargandoStats = false }
                        else -> cargandoStats = false
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(animationSpec = tween(250)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: badge + fecha
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(badgeText, color = Color.White, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(partido.fecha, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Marcador
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Text(partido.equipoLocal?.nombre ?: "Local", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary).padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text("$animatedGolesLocal – $animatedGolesVisita", fontSize = 22.sp,
                        fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                }
                Text(partido.equipoVisita?.nombre ?: "Visita", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }

            // Panel expandido
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))

                // Estadio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stadium, null, Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(partido.estadio, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))

                // Goles por jugador desde API
                if (cargandoStats) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                } else if (estadisticasPartido.isNotEmpty()) {
                    // Goles del local
                    val golesLocal = estadisticasPartido.filter {
                        it.goles > 0 && (partido.equipoLocal?.idEquipo == null ||
                                it.jugador?.nombre != null)
                    }
                    if (golesLocal.isNotEmpty()) {
                        Text("⚽ Goles ${partido.equipoLocal?.nombre ?: "Local"}",
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        golesLocal.forEach { stat ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Default.SportsSoccer, null, Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("${stat.jugador?.nombre ?: "Jugador"} (×${stat.goles})",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Estadísticas del partido (valores calculados/simulados)
                Text("📊 Estadísticas del Partido", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))

                EstadisticaFilaPartido("Tiros al arco", tirosLocal, tirosVisita)
                EstadisticaFilaPartido("Tiros de esquina", cornerLocal, cornerVisita)
                EstadisticaFilaPartido("Faltas totales", faltasLocal, faltasVisita)
                EstadisticaFilaPartido("Pases", pasesLocal, pasesVisita)

                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Editar")
                    }
                    TextButton(onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadisticaFilaPartido(label: String, valorLocal: Int, valorVisita: Int) {
    val total = (valorLocal + valorVisita).coerceAtLeast(1).toFloat()
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(valorLocal.toString(), style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
                Box(Modifier.weight(valorLocal / total).background(MaterialTheme.colorScheme.primary))
                Box(Modifier.weight(valorVisita / total).background(MaterialTheme.colorScheme.secondary))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(valorVisita.toString(), style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.width(32.dp))
    }
}

@Composable
fun ProximoPartidoCard(partido: Partido) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${partido.equipoLocal?.nombre ?: "Local"} vs ${partido.equipoVisita?.nombre ?: "Visita"}",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(partido.fecha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(partido.estadio, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp)) {
                Text("PRÓXIMO", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoBottomSheet(
    title: String, partidoInicial: Partido? = null, onDismiss: () -> Unit,
    onConfirm: (Partido, onDone: (Boolean) -> Unit) -> Unit
) {
    var fecha by remember { mutableStateOf(partidoInicial?.fecha ?: "") }
    var estadio by remember { mutableStateOf(partidoInicial?.estadio ?: "") }
    var golesLocal by remember { mutableStateOf(partidoInicial?.golesLocal?.toString() ?: "0") }
    var golesVisita by remember { mutableStateOf(partidoInicial?.golesVisita?.toString() ?: "0") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var equipos by remember { mutableStateOf<List<Equipo>>(emptyList()) }
    var loadingEquipos by remember { mutableStateOf(true) }
    var equipoLocal by remember { mutableStateOf<Equipo?>(null) }
    var equipoVisita by remember { mutableStateOf<Equipo?>(null) }
    var expandedLocal by remember { mutableStateOf(false) }
    var expandedVisita by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val repo = EquipoRepository()
        when (val r = repo.getEquipos()) {
            is Result.Success -> {
                equipos = r.data
                // Pre-seleccionar si estamos editando
                partidoInicial?.equipoLocal?.idEquipo?.let { id ->
                    equipoLocal = r.data.firstOrNull { it.idEquipo == id }
                }
                partidoInicial?.equipoVisita?.idEquipo?.let { id ->
                    equipoVisita = r.data.firstOrNull { it.idEquipo == id }
                }
            }
            else -> {}
        }
        loadingEquipos = false
    }

    val isValid = fecha.isNotBlank() && estadio.isNotBlank() &&
            equipoLocal != null && equipoVisita != null

    ModalBottomSheet(onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(value = fecha, onValueChange = { fecha = it },
                label = { Text("Fecha (YYYY-MM-DD)") }, isError = fecha.isBlank(),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) })
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(value = estadio, onValueChange = { estadio = it },
                label = { Text("Estadio") }, isError = estadio.isBlank(),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Stadium, null) })
            Spacer(Modifier.height(10.dp))

            if (loadingEquipos) {
                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Cargando equipos...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                // Dropdown equipo local
                ExposedDropdownMenuBox(expanded = expandedLocal,
                    onExpandedChange = { expandedLocal = it }) {
                    OutlinedTextField(
                        value = equipoLocal?.nombre ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Equipo Local") },
                        isError = equipoLocal == null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedLocal) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Home, null) }
                    )
                    ExposedDropdownMenu(expanded = expandedLocal,
                        onDismissRequest = { expandedLocal = false }) {
                        equipos.forEach { e ->
                            DropdownMenuItem(
                                text = { Text(e.nombre) },
                                leadingIcon = {
                                    Text(e.ciudad, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                onClick = { equipoLocal = e; expandedLocal = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Dropdown equipo visita
                ExposedDropdownMenuBox(expanded = expandedVisita,
                    onExpandedChange = { expandedVisita = it }) {
                    OutlinedTextField(
                        value = equipoVisita?.nombre ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Equipo Visita") },
                        isError = equipoVisita == null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedVisita) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.FlightLand, null) }
                    )
                    ExposedDropdownMenu(expanded = expandedVisita,
                        onDismissRequest = { expandedVisita = false }) {
                        equipos.forEach { e ->
                            DropdownMenuItem(
                                text = { Text(e.nombre) },
                                leadingIcon = {
                                    Text(e.ciudad, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                onClick = { equipoVisita = e; expandedVisita = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Goles
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = golesLocal, onValueChange = { golesLocal = it },
                        label = { Text("Goles ${equipoLocal?.nombre ?: "Local"}") },
                        modifier = Modifier.weight(1f), singleLine = true,
                        leadingIcon = { Icon(Icons.Default.SportsSoccer, null) })
                    OutlinedTextField(value = golesVisita, onValueChange = { golesVisita = it },
                        label = { Text("Goles ${equipoVisita?.nombre ?: "Visita"}") },
                        modifier = Modifier.weight(1f), singleLine = true,
                        leadingIcon = { Icon(Icons.Default.SportsSoccer, null) })
                }
            }

            Spacer(Modifier.height(24.dp))
            errorMsg?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    isSaving = true
                    errorMsg = null
                    onConfirm(Partido(
                        idPartido    = partidoInicial?.idPartido ?: 0,
                        fecha        = fecha,
                        estadio      = estadio,
                        equipoLocal  = EquipoSimple(idEquipo = equipoLocal!!.idEquipo,
                            nombre = equipoLocal!!.nombre, ciudad = equipoLocal!!.ciudad,
                            fundacion = equipoLocal!!.fundacion),
                        equipoVisita = EquipoSimple(idEquipo = equipoVisita!!.idEquipo,
                            nombre = equipoVisita!!.nombre, ciudad = equipoVisita!!.ciudad,
                            fundacion = equipoVisita!!.fundacion),
                        golesLocal   = golesLocal.toIntOrNull() ?: 0,
                        golesVisita  = golesVisita.toIntOrNull() ?: 0
                    )) { success ->
                        isSaving = false
                        if (!success) errorMsg = "No se pudo guardar. Intenta de nuevo."
                    }
                },
                enabled = isValid && !isSaving && !loadingEquipos,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                else Text("Guardar", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { if (!isSaving) onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}