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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.futbol.app.data.model.*
import com.futbol.app.data.repository.*
import com.futbol.app.ui.components.*
import com.futbol.app.viewmodel.*

data class EstadisticaTotal(
    val idJugador: Int,
    val nombreJugador: String,
    val goles: Int,
    val asistencias: Int,
    val tarjetasAmarillas: Int,
    val tarjetasRojas: Int,
    val minutosJugados: Int,
    val partidosJugados: Int,
    val fotoUrl: String?,
    // Guardamos los registros originales para poder eliminar
    val registros: List<EstadisticasJugador>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: EstadisticasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeFilter by remember { mutableStateOf("Todos") }
    var showAddSheet by remember { mutableStateOf(false) }
    var estToDelete by remember { mutableStateOf<EstadisticasJugador?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary) },
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
            FloatingActionButton(onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filtros
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Goles", "Asistencias").forEach { f ->
                    FilterChip(selected = activeFilter == f, onClick = { activeFilter = f },
                        label = { Text(f) })
                }
            }

            when (val state = uiState) {
                is EstadisticasUiState.Loading -> LoadingScreen()
                is EstadisticasUiState.Error   -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.cargarEstadisticas() }
                )
                is EstadisticasUiState.Success -> {

                    // Agrupar por jugador y sumar totales
                    val totales = state.estadisticas
                        .groupBy { it.jugador?.idJugador ?: it.idJugador }
                        .map { (_, lista) ->
                            val primero = lista.first()
                            val nombre  = primero.jugador?.nombre ?: "Jugador #${primero.idJugador}"
                            EstadisticaTotal(
                                idJugador         = primero.jugador?.idJugador ?: primero.idJugador,
                                nombreJugador     = nombre,
                                goles             = lista.sumOf { it.goles },
                                asistencias       = lista.sumOf { it.asistencias },
                                tarjetasAmarillas = lista.sumOf { it.tarjetasAmarillas },
                                tarjetasRojas     = lista.sumOf { it.tarjetasRojas },
                                minutosJugados    = lista.sumOf { it.minutosJugados },
                                partidosJugados   = lista.size,
                                fotoUrl = "https://ui-avatars.com/api/?name=${nombre.replace(" ", "+")}&background=1565C0&color=fff&size=128",
                                registros         = lista
                            )
                        }

                    val sorted = when (activeFilter) {
                        "Goles"       -> totales.sortedByDescending { it.goles }
                        "Asistencias" -> totales.sortedByDescending { it.asistencias }
                        else          -> totales.sortedByDescending { it.goles }
                    }

                    if (sorted.isEmpty()) {
                        EmptyScreen("No hay estadísticas registradas")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                            if (activeFilter == "Goles" && sorted.isNotEmpty()) {
                                item { GolesLeaderboardTotal(sorted.take(3)) }
                            }
                            itemsIndexed(sorted, key = { _, it -> it.idJugador }) { index, est ->
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(index * 60L); visible = true
                                }
                                AnimatedVisibility(visible,
                                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 40 }) {
                                    EstadisticaTotalCard(
                                        estadistica = est,
                                        onDeleteRegistro = { reg -> estToDelete = reg }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        EstadisticaBottomSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { est, onDone ->
                viewModel.crearEstadistica(est,
                    onSuccess = { onDone(true); showAddSheet = false }, onError = { onDone(false) })
            }
        )
    }

    estToDelete?.let { est ->
        ConfirmDialog(
            title = "Eliminar Registro",
            message = "¿Eliminar el registro de ${est.jugador?.nombre ?: "este jugador"} en el partido ${est.partido?.estadio ?: "#${est.idPartido}"}?",
            onConfirm = {
                viewModel.eliminarEstadistica(est.idEstadistica,
                    onSuccess = { viewModel.cargarEstadisticas() }, onError = {})
                estToDelete = null
            },
            onDismiss = { estToDelete = null },
            isDestructive = true
        )
    }
}

//  Leaderboard
@Composable
fun GolesLeaderboardTotal(estadisticas: List<EstadisticaTotal>) {
    val medals = listOf("🥇", "🥈", "🥉")
    val medalColors = listOf(Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFCD7F32))

    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🏆 Líderes de Goles", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            estadisticas.forEachIndexed { i, est ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(medalColors[i].copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) {
                        Text(medals[i], style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(est.nombreJugador, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Box(modifier = Modifier.clip(CircleShape)
                        .background(medalColors[i]).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("${est.goles} ⚽", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}

// Tarjeta de estadística agrupada
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticaTotalCard(
    estadistica: EstadisticaTotal,
    onDeleteRegistro: (EstadisticasJugador) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .animateContentSize(tween(250)),
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center) {
                    AsyncImage(model = estadistica.fotoUrl, contentDescription = estadistica.nombreJugador,
                        modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(estadistica.nombreJugador, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Text("${estadistica.partidosJugados} partidos · ${estadistica.minutosJugados} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatPillEst("⚽", estadistica.goles.toString(),
                    MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                StatPillEst("🅰", estadistica.asistencias.toString(),
                    MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                StatPillEst("🟨", estadistica.tarjetasAmarillas.toString(),
                    Color(0xFFFFF3CD), Color(0xFF856404))
                StatPillEst("🟥", estadistica.tarjetasRojas.toString(),
                    MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
            }

            // Panel expandido: registros individuales con botón eliminar
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Text("Registros por partido", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))

                estadistica.registros.forEach { reg ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reg.partido?.estadio ?: "Partido #${reg.idPartido}",
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                "⚽${reg.goles}  🅰${reg.asistencias}  🟨${reg.tarjetasAmarillas}  🟥${reg.tarjetasRojas}  ⏱${reg.minutosJugados}min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Botón eliminar registro individual
                        IconButton(
                            onClick = { onDeleteRegistro(reg) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, "Eliminar registro",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                    if (reg != estadistica.registros.last()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPillEst(emoji: String, value: String, bg: Color, fg: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg)
        .padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text("$emoji $value", style = MaterialTheme.typography.labelMedium,
            color = fg, fontWeight = FontWeight.Bold)
    }
}

// Bottom Sheet nueva estadística con dropdowns
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticaBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (EstadisticasJugador, onDone: (Boolean) -> Unit) -> Unit
) {
    var jugadores by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var partidos  by remember { mutableStateOf<List<Partido>>(emptyList()) }
    var jugadorSeleccionado by remember { mutableStateOf<Jugador?>(null) }
    var partidoSeleccionado by remember { mutableStateOf<Partido?>(null) }
    var expandedJugador by remember { mutableStateOf(false) }
    var expandedPartido by remember { mutableStateOf(false) }
    var minutos    by remember { mutableStateOf("90") }
    var goles      by remember { mutableStateOf(0f) }
    var asistencias by remember { mutableStateOf(0f) }
    var amarillas  by remember { mutableStateOf(0f) }
    var rojas      by remember { mutableStateOf(0f) }
    var isSaving   by remember { mutableStateOf(false) }
    var loading    by remember { mutableStateOf(true) }
    var errorMsg   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val jRepo  = JugadorRepository()
        val pRepo  = PartidoRepository()
        when (val r = jRepo.getJugadores())  { is Result.Success -> jugadores = r.data; else -> {} }
        when (val r = pRepo.getPartidos())   { is Result.Success -> partidos  = r.data; else -> {} }
        loading = false
    }

    val isValid = jugadorSeleccionado != null && partidoSeleccionado != null

    ModalBottomSheet(onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Nueva Estadística", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Dropdown Jugador
                ExposedDropdownMenuBox(expanded = expandedJugador,
                    onExpandedChange = { expandedJugador = it }) {
                    OutlinedTextField(
                        value = jugadorSeleccionado?.let { "${it.nombre} (#${it.dorsal})" } ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Jugador") },
                        isError = !isValid && jugadorSeleccionado == null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedJugador) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                    ExposedDropdownMenu(expanded = expandedJugador,
                        onDismissRequest = { expandedJugador = false }) {
                        jugadores.forEach { j ->
                            DropdownMenuItem(
                                text = { Text("${j.nombre} · ${j.posicion}") },
                                leadingIcon = {
                                    Text("#${j.dorsal}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold)
                                },
                                onClick = { jugadorSeleccionado = j; expandedJugador = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Dropdown Partido
                ExposedDropdownMenuBox(expanded = expandedPartido,
                    onExpandedChange = { expandedPartido = it }) {
                    OutlinedTextField(
                        value = partidoSeleccionado?.let {
                            "${it.equipoLocal?.nombre ?: "Local"} vs ${it.equipoVisita?.nombre ?: "Visita"} · ${it.fecha}"
                        } ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Partido") },
                        isError = !isValid && partidoSeleccionado == null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPartido) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.SportsSoccer, null) }
                    )
                    ExposedDropdownMenu(expanded = expandedPartido,
                        onDismissRequest = { expandedPartido = false }) {
                        partidos.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${p.equipoLocal?.nombre ?: "Local"} ${p.golesLocal} – ${p.golesVisita} ${p.equipoVisita?.nombre ?: "Visita"}",
                                            fontWeight = FontWeight.SemiBold)
                                        Text(p.fecha,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = { partidoSeleccionado = p; expandedPartido = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(value = minutos, onValueChange = { minutos = it },
                    label = { Text("Minutos Jugados") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, leadingIcon = { Icon(Icons.Default.Timer, null) })
                Spacer(Modifier.height(14.dp))

                SliderStatEst("⚽ Goles", goles, 0f..10f) { goles = it }
                SliderStatEst("🅰 Asistencias", asistencias, 0f..10f) { asistencias = it }
                SliderStatEst("🟨 Amarillas", amarillas, 0f..3f) { amarillas = it }
                SliderStatEst("🟥 Rojas", rojas, 0f..2f) { rojas = it }
            }

            Spacer(Modifier.height(20.dp))
            errorMsg?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    isSaving = true
                    errorMsg = null
                    val j = jugadorSeleccionado!!
                    val p = partidoSeleccionado!!
                    onConfirm(EstadisticasJugador(
                        jugador = JugadorSimple(idJugador = j.idJugador, nombre = j.nombre,
                            posicion = j.posicion, dorsal = j.dorsal,
                            fechaNac = j.fechaNac, nacionalidad = j.nacionalidad),
                        partido = PartidoSimple(idPartido = p.idPartido,
                            estadio = p.estadio, fecha = p.fecha),
                        minutosJugados    = minutos.toIntOrNull() ?: 90,
                        goles             = goles.toInt(),
                        asistencias       = asistencias.toInt(),
                        tarjetasAmarillas = amarillas.toInt(),
                        tarjetasRojas     = rojas.toInt()
                    )) { success ->
                        isSaving = false
                        if (!success) errorMsg = "No se pudo guardar. Intenta de nuevo."
                    }
                },
                enabled = isValid && !isSaving && !loading,
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

@Composable
private fun SliderStatEst(label: String, value: Float, range: ClosedFloatingPointRange<Float>,
                          onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(130.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1, modifier = Modifier.weight(1f))
        Text(value.toInt().toString(), style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
    }
}