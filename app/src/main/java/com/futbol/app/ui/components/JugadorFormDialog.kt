
package com.futbol.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futbol.app.data.model.Jugador

@Composable
fun JugadorFormDialog(
    title: String,
    jugadorInicial: Jugador?,
    onDismiss: () -> Unit,
    onConfirm: (Jugador, onDone: (Boolean) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf(jugadorInicial?.nombre ?: "") }
    var posicion by remember { mutableStateOf(jugadorInicial?.posicion ?: "") }
    var dorsal by remember { mutableStateOf(jugadorInicial?.dorsal?.toString() ?: "") }
    var nacionalidad by remember { mutableStateOf(jugadorInicial?.nacionalidad ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it; errorMsg = null },
                    label = { Text("Nombre") }, enabled = !isSaving)
                OutlinedTextField(value = posicion, onValueChange = { posicion = it; errorMsg = null },
                    label = { Text("Posición") }, enabled = !isSaving)
                OutlinedTextField(value = dorsal, onValueChange = { dorsal = it; errorMsg = null },
                    label = { Text("Dorsal") }, enabled = !isSaving)
                OutlinedTextField(value = nacionalidad, onValueChange = { nacionalidad = it; errorMsg = null },
                    label = { Text("Nacionalidad") }, enabled = !isSaving)
                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    jugadorInicial?.let {
                        isSaving = true
                        errorMsg = null
                        onConfirm(
                            it.copy(
                                nombre = nombre,
                                posicion = posicion,
                                dorsal = dorsal.toIntOrNull() ?: it.dorsal,
                                nacionalidad = nacionalidad
                            )
                        ) { success ->
                            isSaving = false
                            if (!success) errorMsg = "No se pudo guardar. Intenta de nuevo."
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isSaving) onDismiss() }) { Text("Cancelar") }
        }
    )
}