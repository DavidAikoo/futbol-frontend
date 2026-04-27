
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futbol.app.data.network.ApiClient
import kotlinx.coroutines.delay

enum class CheckState { Pendiente, Cargando, Ok, Error }

data class ApiCheck(
    val nombre: String,
    val endpoint: String,
    var estado: CheckState = CheckState.Pendiente,
    var intentos: Int = 0
)

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val checks = remember {
        mutableStateListOf(
            ApiCheck("Equipos",      "equipos"),
            ApiCheck("Jugadores",    "jugadores"),
            ApiCheck("Partidos",     "partidos"),
            ApiCheck("Estadísticas", "estadisticas"),
            ApiCheck("Entrenadores", "entrenadores")
        )
    }

    var progreso by remember { mutableStateOf(0f) }
    var mensajeActual by remember { mutableStateOf("Conectando con el servidor...") }

    // Animación balón
    val rotacion by rememberInfiniteTransition(label = "rot").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "r"
    )
    val pulso by rememberInfiniteTransition(label = "pul").animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "p"
    )

    LaunchedEffect(Unit) {
        val api = ApiClient.apiService

        // reintentar hasta el OK
        checks.forEachIndexed { i, check ->
            checks[i] = checks[i].copy(estado = CheckState.Cargando)
            mensajeActual = "Verificando ${check.nombre}..."

            var ok = false
            var intento = 0

            while (!ok) {
                intento++
                checks[i] = checks[i].copy(intentos = intento)

                // Actualizar mensaje con conteo de intentos
                mensajeActual = if (intento == 1)
                    "Verificando ${check.nombre}..."
                else
                    "Despertando ${check.nombre}... (intento $intento)"

                ok = try {
                    val resp = when (check.endpoint) {
                        "equipos"      -> api.getEquipos()
                        "jugadores"    -> api.getJugadores()
                        "partidos"     -> api.getPartidos()
                        "estadisticas" -> api.getEstadisticas()
                        else           -> api.getEntrenadores()
                    }
                    resp.isSuccessful
                } catch (e: Exception) {
                    false
                }

                if (!ok) {
                    // Marcar como error visualmente mientras reintenta
                    checks[i] = checks[i].copy(estado = CheckState.Error)
                    delay(3000)
                    checks[i] = checks[i].copy(estado = CheckState.Cargando)
                }
            }

            // API OK
            checks[i] = checks[i].copy(estado = CheckState.Ok)

            // Avanzar progreso
            val targetProgreso = (i + 1) / 5f
            val pasos = 20
            val delta = (targetProgreso - progreso) / pasos
            repeat(pasos) {
                delay(25)
                progreso = minOf(progreso + delta, targetProgreso)
            }
        }

        mensajeActual = "¡Todo listo! Iniciando..."

        val pasosFinales = 20
        val deltaFinal = (1f - progreso) / pasosFinales
        repeat(pasosFinales) {
            delay(20)
            progreso = minOf(progreso + deltaFinal, 1f)
        }
        progreso = 1f

        delay(500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1B2A), Color(0xFF0D2B6B), Color(0xFF1565C0))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Balón
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulso)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SportsSoccer,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(rotacion),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Gestión Fútbol",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                "Iniciando servicios...",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(36.dp))

            // APIs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.10f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    checks.forEach { check ->
                        ApiCheckRow(check)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Progreso
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        mensajeActual,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${(progreso * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.20f)
                )
            }

            // Nota si alguna API está fallando
            val hayFallo = checks.any { it.estado == CheckState.Error }
            if (hayFallo) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "⏳ El servidor está tardando en despertar.\nEsto puede tomar hasta 1 minuto.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.60f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ApiCheckRow(check: ApiCheck) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icono de estado
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            when (check.estado) {
                CheckState.Pendiente -> Icon(
                    Icons.Default.SportsSoccer, null,
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(16.dp)
                )
                CheckState.Cargando -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                CheckState.Ok -> Icon(
                    Icons.Default.CheckCircle, null,
                    tint = Color(0xFF69F0AE),
                    modifier = Modifier.size(20.dp)
                )
                CheckState.Error -> Icon(
                    Icons.Default.Error, null,
                    tint = Color(0xFFFF7043),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Nombre
        Text(
            check.nombre,
            color = when (check.estado) {
                CheckState.Ok      -> Color(0xFF69F0AE)
                CheckState.Error   -> Color(0xFFFF7043)
                CheckState.Cargando -> Color.White
                else               -> Color.White.copy(alpha = 0.40f)
            },
            fontSize = 14.sp,
            fontWeight = if (check.estado == CheckState.Cargando) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        // Estado textual + intentos
        val estadoTexto = when (check.estado) {
            CheckState.Ok       -> "✓ Listo"
            CheckState.Error    -> "Reintentando..."
            CheckState.Cargando -> if (check.intentos > 1) "Intento ${check.intentos}" else "Verificando"
            else                -> ""
        }
        Text(
            estadoTexto,
            fontSize = 11.sp,
            color = when (check.estado) {
                CheckState.Ok    -> Color(0xFF69F0AE).copy(alpha = 0.8f)
                CheckState.Error -> Color(0xFFFF7043).copy(alpha = 0.9f)
                else             -> Color.White.copy(alpha = 0.45f)
            }
        )
    }
}
