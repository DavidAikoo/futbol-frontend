package com.futbol.app.data.model

import com.google.gson.annotations.SerializedName

// ─── EquipoSimple (anidado en Jugador, Entrenador, Partido) ───────────────────
data class EquipoSimple(
    @SerializedName("idEquipo") val idEquipo: Int = 0,
    @SerializedName("nombre")   val nombre: String = "",
    @SerializedName("ciudad")   val ciudad: String = "",
    @SerializedName("fundacion") val fundacion: String = "",
    @SerializedName("fotoUrl")  val fotoUrl: String? = null
)

// ─── JugadorSimple (anidado en EstadisticasJugador) ──────────────────────────
data class JugadorSimple(
    @SerializedName("idJugador")    val idJugador: Int = 0,
    @SerializedName("nombre")       val nombre: String = "",
    @SerializedName("posicion")     val posicion: String = "",
    @SerializedName("dorsal")       val dorsal: Int = 0,
    @SerializedName("fechaNac")     val fechaNac: String = "",
    @SerializedName("nacionalidad") val nacionalidad: String = ""
)

// ─── PartidoSimple (anidado en EstadisticasJugador) ──────────────────────────
data class PartidoSimple(
    @SerializedName("idPartido") val idPartido: Int = 0,
    @SerializedName("estadio")   val estadio: String = "",
    @SerializedName("fecha")     val fecha: String = ""
)

// ─── Equipo ───────────────────────────────────────────────────────────────────
data class Equipo(
    @SerializedName("idEquipo")   val idEquipo: Int = 0,
    @SerializedName("nombre")     val nombre: String = "",
    @SerializedName("ciudad")     val ciudad: String = "",
    @SerializedName("fundacion")  val fundacion: String = "",
    @SerializedName("fotoUrl")    val fotoUrl: String? = null
)

// ─── Jugador ──────────────────────────────────────────────────────────────────
data class Jugador(
    @SerializedName("idJugador")    val idJugador: Int = 0,
    @SerializedName("nombre")       val nombre: String = "",
    @SerializedName("posicion")     val posicion: String = "",
    @SerializedName("dorsal")       val dorsal: Int = 0,
    @SerializedName("fechaNac")     val fechaNac: String = "",
    @SerializedName("nacionalidad") val nacionalidad: String = "",
    @SerializedName("equipo")       val equipo: EquipoSimple? = null,
    val fotoUrl: String? = null
) {
    // Para mantener compatibilidad con el código existente que usa idEquipo
    val idEquipo: Int get() = equipo?.idEquipo ?: 0
}

// ─── Entrenador ───────────────────────────────────────────────────────────────
data class Entrenador(
    @SerializedName("idEntrenador") val idEntrenador: Int = 0,
    @SerializedName("nombre")       val nombre: String = "",
    @SerializedName("especialidad") val especialidad: String = "",
    @SerializedName("equipo")       val equipo: EquipoSimple? = null,
    @SerializedName("fotoUrl")      val fotoUrl: String? = null
) {
    // Para mantener compatibilidad con el código existente que usa idEquipo
    val idEquipo: Int get() = equipo?.idEquipo ?: 0
}

// ─── Partido ──────────────────────────────────────────────────────────────────
data class Partido(
    @SerializedName("idPartido")    val idPartido: Int = 0,
    @SerializedName("fecha")        val fecha: String = "",
    @SerializedName("estadio")      val estadio: String = "",
    @SerializedName("golesLocal")   val golesLocal: Int = 0,
    @SerializedName("golesVisita")  val golesVisita: Int = 0,
    @SerializedName("equipoLocal")  val equipoLocal: EquipoSimple? = null,
    @SerializedName("equipoVisita") val equipoVisita: EquipoSimple? = null
) {
    // Para compatibilidad con código que accede a nombre de equipos
    val nombreLocal: String get() = equipoLocal?.nombre ?: ""
    val nombreVisita: String get() = equipoVisita?.nombre ?: ""
}

// ─── EstadisticasJugador ──────────────────────────────────────────────────────
data class EstadisticasJugador(
    @SerializedName("idEstadistica")     val idEstadistica: Int = 0,
    @SerializedName("minutosJugados")    val minutosJugados: Int = 0,
    @SerializedName("goles")            val goles: Int = 0,
    @SerializedName("asistencias")      val asistencias: Int = 0,
    @SerializedName("tarjetasAmarillas") val tarjetasAmarillas: Int = 0,
    @SerializedName("tarjetasRojas")    val tarjetasRojas: Int = 0,
    @SerializedName("jugador")          val jugador: JugadorSimple? = null,
    @SerializedName("partido")          val partido: PartidoSimple? = null
) {
    // Para compatibilidad con código existente
    val idJugador: Int get() = jugador?.idJugador ?: 0
    val idPartido: Int get() = partido?.idPartido ?: 0
}

// ─── Wrappers de respuesta genérica ──────────────────────────────────────────
data class ApiResponse<T>(
    @SerializedName("data")    val data: T? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("success") val success: Boolean = true
)
// ─── DTOs para envío al backend (evita campos extra en serialización) ──────────

/** Solo envía el idEquipo al crear/actualizar un jugador */
data class EquipoIdOnly(
    @SerializedName("idEquipo") val idEquipo: Int
)

/** Payload limpio que el backend espera al POST /jugadores */
data class JugadorRequest(
    @SerializedName("nombre")       val nombre: String,
    @SerializedName("posicion")     val posicion: String,
    @SerializedName("dorsal")       val dorsal: Int,
    @SerializedName("fechaNac")     val fechaNac: String,
    @SerializedName("nacionalidad") val nacionalidad: String,
    @SerializedName("fotoUrl")      val fotoUrl: String?,
    @SerializedName("equipo")       val equipo: EquipoIdOnly
)

/** Payload limpio para POST/PUT /entrenadores */
data class EntrenadorRequest(
    @SerializedName("nombre")       val nombre: String,
    @SerializedName("especialidad") val especialidad: String,
    @SerializedName("fotoUrl")      val fotoUrl: String?,
    @SerializedName("equipo")       val equipo: EquipoIdOnly
)

/** Payload limpio para POST/PUT /partidos */
data class PartidoRequest(
    @SerializedName("fecha")        val fecha: String,
    @SerializedName("estadio")      val estadio: String,
    @SerializedName("equipoLocal")  val equipoLocal: EquipoIdOnly,
    @SerializedName("equipoVisita") val equipoVisita: EquipoIdOnly,
    @SerializedName("golesLocal")   val golesLocal: Int,
    @SerializedName("golesVisita")  val golesVisita: Int
)

data class JugadorIdOnly(
    @SerializedName("idJugador") val idJugador: Int
)

data class PartidoIdOnly(
    @SerializedName("idPartido") val idPartido: Int
)

/** Payload limpio para POST/PUT /estadisticas */
data class EstadisticaRequest(
    @SerializedName("jugador")           val jugador: JugadorIdOnly,
    @SerializedName("partido")           val partido: PartidoIdOnly,
    @SerializedName("minutosJugados")    val minutosJugados: Int,
    @SerializedName("goles")            val goles: Int,
    @SerializedName("asistencias")      val asistencias: Int,
    @SerializedName("tarjetasAmarillas") val tarjetasAmarillas: Int,
    @SerializedName("tarjetasRojas")    val tarjetasRojas: Int
)

/** Payload limpio para POST/PUT /equipos */
data class EquipoRequest(
    @SerializedName("nombre")    val nombre: String,
    @SerializedName("ciudad")    val ciudad: String,
    @SerializedName("fundacion") val fundacion: String,
    @SerializedName("fotoUrl")   val fotoUrl: String?
)