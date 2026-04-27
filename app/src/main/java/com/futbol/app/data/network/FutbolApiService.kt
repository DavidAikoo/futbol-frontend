package com.futbol.app.data.network

import com.futbol.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FutbolApiService {

    // ─── EQUIPOS ──────────────────────────────────────────────────────────────
    @GET("equipos")
    suspend fun getEquipos(): Response<List<Equipo>>

    @GET("equipos/{id}")
    suspend fun getEquipoById(@Path("id") id: Int): Response<Equipo>

    @POST("equipos")
    suspend fun crearEquipo(@Body equipo: Equipo): Response<Equipo>

    @PUT("equipos/{id}")
    suspend fun actualizarEquipo(@Path("id") id: Int, @Body equipo: Equipo): Response<Equipo>

    @DELETE("equipos/{id}")
    suspend fun eliminarEquipo(@Path("id") id: Int): Response<Unit>

    // ─── JUGADORES ────────────────────────────────────────────────────────────
    @GET("jugadores")
    suspend fun getJugadores(): Response<List<Jugador>>

    @GET("jugadores/{id}")
    suspend fun getJugadorById(@Path("id") id: Int): Response<Jugador>

    @GET("jugadores/equipo/{idEquipo}")
    suspend fun getJugadoresByEquipo(@Path("idEquipo") idEquipo: Int): Response<List<Jugador>>

    @GET("jugadores/goles")
    suspend fun getJugadoresConGoles(@Query("min") min: Int = 1): Response<List<Jugador>>

    /** Crear jugador con payload limpio: solo envía {idEquipo} en equipo */
    @POST("jugadores")
    suspend fun crearJugadorRequest(@Body jugador: JugadorRequest): Response<Jugador>

    /** Actualizar jugador con payload limpio */
    @PUT("jugadores/{id}")
    suspend fun actualizarJugadorRequest(@Path("id") id: Int, @Body jugador: JugadorRequest): Response<Jugador>

    // Mantener compatibilidad para otros usos
    @POST("jugadores")
    suspend fun crearJugador(@Body jugador: Jugador): Response<Jugador>

    @PUT("jugadores/{id}")
    suspend fun actualizarJugador(@Path("id") id: Int, @Body jugador: Jugador): Response<Jugador>

    @DELETE("jugadores/{id}")
    suspend fun eliminarJugador(@Path("id") id: Int): Response<Unit>

    // ─── ENTRENADORES ─────────────────────────────────────────────────────────
    @GET("entrenadores")
    suspend fun getEntrenadores(): Response<List<Entrenador>>

    @GET("entrenadores/{id}")
    suspend fun getEntrenadorById(@Path("id") id: Int): Response<Entrenador>

    @GET("entrenadores/equipo/{idEquipo}")
    suspend fun getEntrenadoresByEquipo(@Path("idEquipo") idEquipo: Int): Response<List<Entrenador>>

    @POST("entrenadores")
    suspend fun crearEntrenador(@Body entrenador: Entrenador): Response<Entrenador>

    @PUT("entrenadores/{id}")
    suspend fun actualizarEntrenador(@Path("id") id: Int, @Body entrenador: Entrenador): Response<Entrenador>

    @DELETE("entrenadores/{id}")
    suspend fun eliminarEntrenador(@Path("id") id: Int): Response<Unit>

    // ─── PARTIDOS ─────────────────────────────────────────────────────────────
    @GET("partidos")
    suspend fun getPartidos(): Response<List<Partido>>

    @GET("partidos/{id}")
    suspend fun getPartidoById(@Path("id") id: Int): Response<Partido>

    @GET("partidos/resultados")
    suspend fun getResultados(): Response<List<Partido>>

    @GET("partidos/equipo/{idEquipo}")
    suspend fun getPartidosByEquipo(@Path("idEquipo") idEquipo: Int): Response<List<Partido>>

    @POST("partidos")
    suspend fun crearPartido(@Body partido: Partido): Response<Partido>

    @PUT("partidos/{id}")
    suspend fun actualizarPartido(@Path("id") id: Int, @Body partido: Partido): Response<Partido>

    @DELETE("partidos/{id}")
    suspend fun eliminarPartido(@Path("id") id: Int): Response<Unit>

    // ─── ESTADÍSTICAS ─────────────────────────────────────────────────────────
    @GET("estadisticas")
    suspend fun getEstadisticas(): Response<List<EstadisticasJugador>>

    @GET("estadisticas/{id}")
    suspend fun getEstadisticaById(@Path("id") id: Int): Response<EstadisticasJugador>

    @GET("estadisticas/jugador/{idJugador}")
    suspend fun getEstadisticasByJugador(@Path("idJugador") idJugador: Int): Response<List<EstadisticasJugador>>

    @GET("estadisticas/partido/{idPartido}")
    suspend fun getEstadisticasByPartido(@Path("idPartido") idPartido: Int): Response<List<EstadisticasJugador>>

    @POST("estadisticas")
    suspend fun crearEstadistica(@Body estadistica: EstadisticasJugador): Response<EstadisticasJugador>

    @PUT("estadisticas/{id}")
    suspend fun actualizarEstadistica(
        @Path("id") id: Int,
        @Body estadistica: EstadisticasJugador
    ): Response<EstadisticasJugador>

    @DELETE("estadisticas/{id}")
    suspend fun eliminarEstadistica(@Path("id") id: Int): Response<Unit>
    // ─── ENTRENADORES ─────────────────────────────────────────────────────────────
    @POST("entrenadores")
    suspend fun crearEntrenadorRequest(@Body entrenador: EntrenadorRequest): Response<Entrenador>

    @PUT("entrenadores/{id}")
    suspend fun actualizarEntrenadorRequest(@Path("id") id: Int, @Body entrenador: EntrenadorRequest): Response<Entrenador>

    // ─── PARTIDOS ─────────────────────────────────────────────────────────────────
    @POST("partidos")
    suspend fun crearPartidoRequest(@Body partido: PartidoRequest): Response<Partido>

    @PUT("partidos/{id}")
    suspend fun actualizarPartidoRequest(@Path("id") id: Int, @Body partido: PartidoRequest): Response<Partido>

    // ─── ESTADÍSTICAS ─────────────────────────────────────────────────────────────
    @POST("estadisticas")
    suspend fun crearEstadisticaRequest(@Body estadistica: EstadisticaRequest): Response<EstadisticasJugador>

    @PUT("estadisticas/{id}")
    suspend fun actualizarEstadisticaRequest(@Path("id") id: Int, @Body estadistica: EstadisticaRequest): Response<EstadisticasJugador>

    @POST("equipos")
    suspend fun crearEquipoRequest(@Body equipo: EquipoRequest): Response<Equipo>

    @PUT("equipos/{id}")
    suspend fun actualizarEquipoRequest(@Path("id") id: Int, @Body equipo: EquipoRequest): Response<Equipo>
}