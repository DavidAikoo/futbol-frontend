package com.futbol.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.futbol.app.data.model.Partido
import com.futbol.app.data.repository.PartidoRepository
import com.futbol.app.data.repository.Result
import com.futbol.app.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ModuloItem(val titulo: String, val icon: ImageVector, val route: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var partidos by remember { mutableStateOf<List<Partido>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            when (val r = PartidoRepository().getPartidos()) {
                is Result.Success -> partidos = r.data
                else -> {}
            }
        }
    }

    val modulos = listOf(
        ModuloItem("Equipos",      Icons.Default.Groups,         Screen.Equipos.route,      Color(0xFF1565C0)),
        ModuloItem("Jugadores",    Icons.Default.Person,         Screen.Jugadores.route,    Color(0xFF0288D1)),
        ModuloItem("Partidos",     Icons.Default.SportsSoccer,   Screen.Partidos.route,     Color(0xFF00838F)),
        ModuloItem("Estadísticas", Icons.Default.BarChart,       Screen.Estadisticas.route, Color(0xFF1976D2)),
        ModuloItem("Entrenadores", Icons.Default.RecordVoiceOver,Screen.Entrenadores.route, Color(0xFF283593))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestión Fútbol", fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp)
                        Text("Panel de control", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (darkTheme) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                            null, tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Carrusel de partidos ──────────────────────────────────────────
            if (partidos.isNotEmpty()) {
                PartidosCarrusel(partidos = partidos)
            } else {
                PartidosCarruselPlaceholder()
            }

            Spacer(Modifier.height(24.dp))

            // ── Título sección módulos ────────────────────────────────────────
            Text(
                "Módulos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))

            // ── Grid 2 columnas ───────────────────────────────────────────────
            val filas = modulos.chunked(2)
            filas.forEach { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { modulo ->
                        ModuloCard(
                            modulo = modulo,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(modulo.route) }
                        )
                    }
                    // Si la fila tiene sólo 1 elemento, añadir spacer para alinear
                    if (fila.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PartidosCarrusel(partidos: List<Partido>) {
    val pageCount = partidos.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Auto-scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            val next = (pagerState.currentPage + 1) % pageCount
            pagerState.animateScrollToPage(next)
        }
    }

    Column {
        // Header carrusel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    )
                )
                .padding(vertical = 6.dp, horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SportsSoccer, null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Partidos Recientes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            PartidoCarruselCard(partido = partidos[page])
        }

        // Indicadores de página
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (isSelected) 20.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }
    }
}

@Composable
private fun PartidoCarruselCard(partido: Partido) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Equipo local
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        val fotoLocal = partido.equipoLocal?.fotoUrl
                        if (!fotoLocal.isNullOrBlank()) {
                            AsyncImage(
                                model = fotoLocal,
                                contentDescription = partido.equipoLocal?.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                (partido.equipoLocal?.nombre ?: "L").take(2).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black, fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        partido.equipoLocal?.nombre ?: "Local",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2
                    )
                }

                // Marcador
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "${partido.golesLocal}  –  ${partido.golesVisita}",
                            fontSize = 24.sp, fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        partido.fecha, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                // Equipo visita
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        val fotoVisita = partido.equipoVisita?.fotoUrl
                        if (!fotoVisita.isNullOrBlank()) {
                            AsyncImage(
                                model = fotoVisita,
                                contentDescription = partido.equipoVisita?.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                (partido.equipoVisita?.nombre ?: "V").take(2).uppercase(),
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Black, fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        partido.equipoVisita?.nombre ?: "Visita",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stadium, null, Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                Spacer(Modifier.width(4.dp))
                Text(partido.estadio, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f))
            }
        }
    }
}

@Composable
private fun PartidosCarruselPlaceholder() {
    Box(
        modifier = Modifier.fillMaxWidth().height(160.dp)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Cargando partidos...", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuloCard(modulo: ModuloItem, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = modulo.color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(modulo.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Text(
                modulo.titulo,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}