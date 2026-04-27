package com.futbol.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.futbol.app.data.model.EstadisticasJugador
import com.futbol.app.data.repository.EstadisticasRepository
import com.futbol.app.ui.components.*
import com.futbol.app.viewmodel.JugadoresUiState
import com.futbol.app.viewmodel.JugadoresViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugadorDetailScreen(
    jugadorId: Int, navController: NavController, viewModel: JugadoresViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val jugador by viewModel.selectedJugador.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var estadisticas by remember { mutableStateOf<List<EstadisticasJugador>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(jugadorId) {
        viewModel.cargarJugadorById(jugadorId)
        scope.launch {
            val repo = EstadisticasRepository()
            val r = repo.getEstadisticasByJugador(jugadorId)
            if (r is com.futbol.app.data.repository.Result.Success) estadisticas = r.data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(jugador?.nombre ?: "Detalle", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        when (detailState) {
            is JugadoresUiState.Loading -> LoadingScreen(modifier = Modifier.padding(padding))
            is JugadoresUiState.Error -> ErrorScreen(
                message = (detailState as JugadoresUiState.Error).message,
                onRetry = { viewModel.cargarJugadorById(jugadorId) },
                modifier = Modifier.padding(padding)
            )
            else -> {
                jugador?.let { j ->
                    // Generar URL de avatar si no hay foto real
                    val avatarUrl = j.fotoUrl?.takeIf { it.isNotBlank() }
                        ?: "https://ui-avatars.com/api/?name=${j.nombre.replace(" ", "+")}&background=1565C0&color=fff&size=256&bold=true"

                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())) {
                        // Hero con foto/avatar
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = j.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Dorsal overlay
                            Box(modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center) {
                                Text("#${j.dorsal}", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(j.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Text(j.equipo?.nombre ?: "Sin equipo asignado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoChipDetail(j.posicion, Icons.Default.Person)
                                InfoChipDetail(j.nacionalidad, Icons.Default.Flag)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoChipDetail("Dorsal: ${j.dorsal}", Icons.Default.Tag)
                                InfoChipDetail("Nac.: ${j.fechaNac}", Icons.Default.CalendarToday)
                            }

                            // Estadísticas totales de carrera
                            if (estadisticas.isNotEmpty()) {
                                Spacer(Modifier.height(24.dp))
                                Text("Estadísticas de Carrera", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("${estadisticas.size} partidos disputados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))

                                val totalGoles = estadisticas.sumOf { it.goles }
                                val totalAsistencias = estadisticas.sumOf { it.asistencias }
                                val totalMinutos = estadisticas.sumOf { it.minutosJugados }
                                val totalAmarillas = estadisticas.sumOf { it.tarjetasAmarillas }
                                val totalRojas = estadisticas.sumOf { it.tarjetasRojas }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatCard("⚽ Goles", totalGoles.toString(), Modifier.weight(1f))
                                    StatCard("🅰 Asistencias", totalAsistencias.toString(), Modifier.weight(1f))
                                    StatCard("⏱ Minutos", totalMinutos.toString(), Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatCard("🟨 Amarillas", totalAmarillas.toString(), Modifier.weight(1f),
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                    StatCard("🟥 Rojas", totalRojas.toString(), Modifier.weight(1f),
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                    StatCard("📋 Partidos", estadisticas.size.toString(), Modifier.weight(1f),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            } else {
                                Spacer(Modifier.height(20.dp))
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Sin estadísticas registradas aún",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Eliminar Jugador",
            message = "¿Estás seguro de que deseas eliminar a ${jugador?.nombre}? Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.eliminarJugador(jugadorId, onSuccess = { navController.popBackStack() }, onError = {})
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }, isDestructive = true
        )
    }
    if (showEditDialog && jugador != null) {
        JugadorFormDialog(
            title = "Editar Jugador", jugadorInicial = jugador,
            onDismiss = { showEditDialog = false },
            onConfirm = { actualizado, onDone ->
                viewModel.actualizarJugador(jugadorId, actualizado,
                    onSuccess = { onDone(true); showEditDialog = false; viewModel.cargarJugadorById(jugadorId) },
                    onError   = { onDone(false) })
            }
        )
    }
}

@Composable
private fun InfoChipDetail(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FilterChip(selected = false, onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(14.dp)) })
}