package com.futbol.app.data.repository

import com.futbol.app.data.model.*
import com.futbol.app.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// EquipoRepository
class EquipoRepository {
    private val api = ApiClient.apiService

    suspend fun getEquipos(): Result<List<Equipo>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEquipos()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun getEquipoById(id: Int): Result<Equipo> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEquipoById(id)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun crearEquipo(equipo: Equipo): Result<Equipo> = withContext(Dispatchers.IO) {
        try {
            val request = EquipoRequest(
                nombre    = equipo.nombre,
                ciudad    = equipo.ciudad,
                fundacion = equipo.fundacion,
                fotoUrl   = equipo.fotoUrl?.takeIf { it.isNotBlank() }
            )
            val response = api.crearEquipoRequest(request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al crear equipo: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun actualizarEquipo(id: Int, equipo: Equipo): Result<Equipo> = withContext(Dispatchers.IO) {
        try {
            val request = EquipoRequest(
                nombre    = equipo.nombre,
                ciudad    = equipo.ciudad,
                fundacion = equipo.fundacion,
                fotoUrl   = equipo.fotoUrl?.takeIf { it.isNotBlank() }
            )
            val response = api.actualizarEquipoRequest(id, request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar equipo: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun eliminarEquipo(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarEquipo(id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Error al eliminar: ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }
}

//JugadorRepository
class JugadorRepository {
    private val api = ApiClient.apiService

    suspend fun getJugadores(): Result<List<Jugador>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getJugadores()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun getJugadorById(id: Int): Result<Jugador> = withContext(Dispatchers.IO) {
        try {
            val response = api.getJugadorById(id)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun getJugadoresByEquipo(idEquipo: Int): Result<List<Jugador>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getJugadoresByEquipo(idEquipo)
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun crearJugador(jugador: Jugador): Result<Jugador> = withContext(Dispatchers.IO) {
        try {
            val request = JugadorRequest(
                nombre       = jugador.nombre,
                posicion     = jugador.posicion,
                dorsal       = jugador.dorsal,
                fechaNac     = jugador.fechaNac,
                nacionalidad = jugador.nacionalidad,
                fotoUrl      = jugador.fotoUrl?.takeIf { it.isNotBlank() },
                equipo       = EquipoIdOnly(idEquipo = jugador.equipo?.idEquipo ?: jugador.idEquipo)
            )
            val response = api.crearJugadorRequest(request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al crear jugador: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun actualizarJugador(id: Int, jugador: Jugador): Result<Jugador> = withContext(Dispatchers.IO) {
        try {
            val request = JugadorRequest(
                nombre       = jugador.nombre,
                posicion     = jugador.posicion,
                dorsal       = jugador.dorsal,
                fechaNac     = jugador.fechaNac,
                nacionalidad = jugador.nacionalidad,
                fotoUrl      = jugador.fotoUrl?.takeIf { it.isNotBlank() },
                equipo       = EquipoIdOnly(idEquipo = jugador.equipo?.idEquipo ?: jugador.idEquipo)
            )
            val response = api.actualizarJugadorRequest(id, request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar jugador: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun eliminarJugador(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarJugador(id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Error al eliminar: ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }
}

//EntrenadorRepository
class EntrenadorRepository {
    private val api = ApiClient.apiService

    suspend fun getEntrenadores(): Result<List<Entrenador>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEntrenadores()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun crearEntrenador(entrenador: Entrenador): Result<Entrenador> = withContext(Dispatchers.IO) {
        try {
            val request = EntrenadorRequest(
                nombre       = entrenador.nombre,
                especialidad = entrenador.especialidad,
                fotoUrl      = entrenador.fotoUrl?.takeIf { it.isNotBlank() },
                equipo       = EquipoIdOnly(idEquipo = entrenador.equipo?.idEquipo ?: 0)
            )
            val response = api.crearEntrenadorRequest(request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al crear entrenador: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun actualizarEntrenador(id: Int, entrenador: Entrenador): Result<Entrenador> = withContext(Dispatchers.IO) {
        try {
            val request = EntrenadorRequest(
                nombre       = entrenador.nombre,
                especialidad = entrenador.especialidad,
                fotoUrl      = entrenador.fotoUrl?.takeIf { it.isNotBlank() },
                equipo       = EquipoIdOnly(idEquipo = entrenador.equipo?.idEquipo ?: 0)
            )
            val response = api.actualizarEntrenadorRequest(id, request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar entrenador: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun eliminarEntrenador(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarEntrenador(id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Error al eliminar: ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }
}

//PartidoRepository
class PartidoRepository {
    private val api = ApiClient.apiService

    suspend fun getPartidos(): Result<List<Partido>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPartidos()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun getResultados(): Result<List<Partido>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getResultados()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun crearPartido(partido: Partido): Result<Partido> = withContext(Dispatchers.IO) {
        try {
            val request = PartidoRequest(
                fecha        = partido.fecha,
                estadio      = partido.estadio,
                equipoLocal  = EquipoIdOnly(idEquipo = partido.equipoLocal?.idEquipo ?: 0),
                equipoVisita = EquipoIdOnly(idEquipo = partido.equipoVisita?.idEquipo ?: 0),
                golesLocal   = partido.golesLocal,
                golesVisita  = partido.golesVisita
            )
            val response = api.crearPartidoRequest(request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al crear partido: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun actualizarPartido(id: Int, partido: Partido): Result<Partido> = withContext(Dispatchers.IO) {
        try {
            val request = PartidoRequest(
                fecha        = partido.fecha,
                estadio      = partido.estadio,
                equipoLocal  = EquipoIdOnly(idEquipo = partido.equipoLocal?.idEquipo ?: 0),
                equipoVisita = EquipoIdOnly(idEquipo = partido.equipoVisita?.idEquipo ?: 0),
                golesLocal   = partido.golesLocal,
                golesVisita  = partido.golesVisita
            )
            val response = api.actualizarPartidoRequest(id, request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar partido: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun eliminarPartido(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarPartido(id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Error al eliminar: ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }
}

//EstadisticasRepository
class EstadisticasRepository {
    private val api = ApiClient.apiService

    suspend fun getEstadisticas(): Result<List<EstadisticasJugador>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEstadisticas()
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun getEstadisticasByJugador(idJugador: Int): Result<List<EstadisticasJugador>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEstadisticasByJugador(idJugador)
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun getEstadisticasByPartido(idPartido: Int): Result<List<EstadisticasJugador>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEstadisticasByPartido(idPartido)
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun crearEstadistica(est: EstadisticasJugador): Result<EstadisticasJugador> = withContext(Dispatchers.IO) {
        try {
            val request = EstadisticaRequest(
                jugador           = JugadorIdOnly(idJugador = est.jugador?.idJugador ?: 0),
                partido           = PartidoIdOnly(idPartido = est.partido?.idPartido ?: 0),
                minutosJugados    = est.minutosJugados,
                goles             = est.goles,
                asistencias       = est.asistencias,
                tarjetasAmarillas = est.tarjetasAmarillas,
                tarjetasRojas     = est.tarjetasRojas
            )
            val response = api.crearEstadisticaRequest(request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al crear estadística: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun actualizarEstadistica(id: Int, est: EstadisticasJugador): Result<EstadisticasJugador> = withContext(Dispatchers.IO) {
        try {
            val request = EstadisticaRequest(
                jugador           = JugadorIdOnly(idJugador = est.jugador?.idJugador ?: 0),
                partido           = PartidoIdOnly(idPartido = est.partido?.idPartido ?: 0),
                minutosJugados    = est.minutosJugados,
                goles             = est.goles,
                asistencias       = est.asistencias,
                tarjetasAmarillas = est.tarjetasAmarillas,
                tarjetasRojas     = est.tarjetasRojas
            )
            val response = api.actualizarEstadisticaRequest(id, request)
            if (response.isSuccessful && response.body() != null) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar estadística: ${response.code()} ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }

    suspend fun eliminarEstadistica(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarEstadistica(id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Error al eliminar: ${response.errorBody()?.string()}")
        } catch (e: Exception) { Result.Error(e.message ?: "Error desconocido") }
    }
}