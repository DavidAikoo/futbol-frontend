package com.futbol.app.ui.forms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futbol.app.data.model.Equipo
import com.futbol.app.data.model.EquipoSimple
import com.futbol.app.data.model.Jugador
import com.futbol.app.data.repository.EquipoRepository
import com.futbol.app.data.repository.Result
import com.futbol.app.ui.components.FotoUrlInput
import kotlinx.coroutines.launch

// onConfirm recibe el jugador y un callback onDone(success) para resetear isSaving
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarJugadorBottomSheet(
    jugadorInicial: Jugador? = null,
    onDismiss: () -> Unit,
    onConfirm: (Jugador, onDone: (Boolean) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf(jugadorInicial?.nombre ?: "") }
    var posicion by remember { mutableStateOf(jugadorInicial?.posicion ?: "") }
    var dorsal by remember { mutableStateOf(jugadorInicial?.dorsal?.toString() ?: "") }
    var fechaNac by remember { mutableStateOf(jugadorInicial?.fechaNac ?: "") }
    var nacionalidad by remember { mutableStateOf(jugadorInicial?.nacionalidad ?: "") }
    var fotoUrl by remember { mutableStateOf(jugadorInicial?.fotoUrl ?: "") }
    var equipoSeleccionado by remember { mutableStateOf<Equipo?>(null) }
    var equipos by remember { mutableStateOf<List<Equipo>>(emptyList()) }
    var expandedEquipo by remember { mutableStateOf(false) }
    var expandedPosicion by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    var loadingEquipos by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            loadingEquipos = true
            val repo = EquipoRepository()
            when (val r = repo.getEquipos()) {
                is Result.Success -> {
                    equipos = r.data
                    jugadorInicial?.equipo?.idEquipo?.let { id ->
                        equipoSeleccionado = r.data.firstOrNull { it.idEquipo == id }
                    }
                }
                else -> {}
            }
            loadingEquipos = false
        }
    }

    val posiciones = listOf(
        "Portero", "Defensa Central", "Lateral Derecho", "Lateral Izquierdo",
        "Pivote", "Centrocampista", "Mediapunta", "Extremo Derecho", "Extremo Izquierdo",
        "Delantero Centro", "Segunda Punta"
    )

    val nombreError     = nombre.isBlank()
    val posicionError   = posicion.isBlank()
    val dorsalError     = dorsal.toIntOrNull() == null || (dorsal.toIntOrNull() ?: 0) !in 1..99
    val fechaError      = fechaNac.isBlank() || !fechaNac.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    val nacionalidadError = nacionalidad.isBlank()
    val equipoError     = equipoSeleccionado == null

    val isValid = !nombreError && !posicionError && !dorsalError &&
            !fechaError && !nacionalidadError && !equipoError

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                if (jugadorInicial == null) "Nuevo Jugador" else "Editar Jugador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Todos los campos son obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it; touched = true; errorMsg = null },
                label = { Text("Nombre completo") },
                isError = touched && nombreError,
                supportingText = { if (touched && nombreError) Text("El nombre es obligatorio") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(expanded = expandedPosicion, onExpandedChange = { expandedPosicion = it }) {
                OutlinedTextField(
                    value = posicion, onValueChange = {}, readOnly = true,
                    label = { Text("Posición") },
                    isError = touched && posicionError,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPosicion) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.SportsSoccer, null) }
                )
                ExposedDropdownMenu(expanded = expandedPosicion, onDismissRequest = { expandedPosicion = false }) {
                    posiciones.forEach { pos ->
                        DropdownMenuItem(
                            text = { Text(pos) },
                            onClick = { posicion = pos; expandedPosicion = false; touched = true }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = dorsal, onValueChange = { dorsal = it; touched = true; errorMsg = null },
                label = { Text("Dorsal (1-99)") },
                isError = touched && dorsalError,
                supportingText = { if (touched && dorsalError) Text("Número entre 1 y 99") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Tag, null) }
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = fechaNac, onValueChange = { fechaNac = it; touched = true; errorMsg = null },
                label = { Text("Fecha de nacimiento") },
                isError = touched && fechaError,
                supportingText = { if (touched && fechaError) Text("Formato: YYYY-MM-DD") },
                placeholder = { Text("2000-01-15") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = nacionalidad, onValueChange = { nacionalidad = it; touched = true; errorMsg = null },
                label = { Text("Nacionalidad") },
                isError = touched && nacionalidadError,
                supportingText = { if (touched && nacionalidadError) Text("La nacionalidad es obligatoria") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Flag, null) }
            )
            Spacer(Modifier.height(10.dp))

            if (loadingEquipos) {
                OutlinedTextField(
                    value = "Cargando equipos...", onValueChange = {},
                    readOnly = true, enabled = false,
                    label = { Text("Equipo") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) }
                )
            } else {
                ExposedDropdownMenuBox(expanded = expandedEquipo, onExpandedChange = { expandedEquipo = it }) {
                    OutlinedTextField(
                        value = equipoSeleccionado?.nombre ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Equipo") },
                        isError = touched && equipoError,
                        supportingText = { if (touched && equipoError) Text("Selecciona un equipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEquipo) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Groups, null) }
                    )
                    ExposedDropdownMenu(expanded = expandedEquipo, onDismissRequest = { expandedEquipo = false }) {
                        equipos.forEach { equipo ->
                            DropdownMenuItem(
                                text = { Text(equipo.nombre) },
                                leadingIcon = {
                                    Text(equipo.ciudad,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                onClick = { equipoSeleccionado = equipo; expandedEquipo = false; touched = true }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            FotoUrlInput(
                value = fotoUrl,
                onValueChange = { fotoUrl = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(visible = touched && !isValid) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        "Por favor corrige los campos marcados antes de guardar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Error de red
            errorMsg?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = {
                    touched = true
                    if (isValid) {
                        isSaving = true
                        errorMsg = null
                        onConfirm(
                            Jugador(
                                idJugador    = jugadorInicial?.idJugador ?: 0,
                                nombre       = nombre,
                                posicion     = posicion,
                                dorsal       = dorsal.toInt(),
                                fechaNac     = fechaNac,
                                nacionalidad = nacionalidad,
                                fotoUrl      = fotoUrl.trim().takeIf { it.isNotBlank() },
                                equipo       = equipoSeleccionado?.let {
                                    EquipoSimple(idEquipo = it.idEquipo, nombre = it.nombre,
                                        ciudad = it.ciudad, fundacion = it.fundacion)
                                }
                            )
                        ) { success ->
                            isSaving = false
                            if (!success) errorMsg = "No se pudo guardar. Intenta de nuevo."
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Icon(Icons.Default.Save, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { if (!isSaving) onDismiss() },
                modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}