package com.futbol.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futbol.app.data.model.*
import com.futbol.app.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// EquiposViewModel
sealed class EquiposUiState {
    object Loading : EquiposUiState()
    data class Success(val equipos: List<Equipo>) : EquiposUiState()
    data class Error(val message: String) : EquiposUiState()
}

class EquiposViewModel : ViewModel() {
    private val repository = EquipoRepository()

    private val _uiState = MutableStateFlow<EquiposUiState>(EquiposUiState.Loading)
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    private val _selectedEquipo = MutableStateFlow<Equipo?>(null)
    val selectedEquipo: StateFlow<Equipo?> = _selectedEquipo.asStateFlow()

    init { cargarEquipos() }

    fun cargarEquipos() {
        viewModelScope.launch {
            _uiState.value = EquiposUiState.Loading
            when (val r = repository.getEquipos()) {
                is Result.Success -> _uiState.value = EquiposUiState.Success(r.data)
                is Result.Error   -> _uiState.value = EquiposUiState.Error(r.message)
                is Result.Loading -> {}
            }
        }
    }

    fun cargarEquipoById(id: Int) {
        viewModelScope.launch {
            when (val r = repository.getEquipoById(id)) {
                is Result.Success -> _selectedEquipo.value = r.data
                else -> {}
            }
        }
    }

    fun crearEquipo(equipo: Equipo, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.crearEquipo(equipo)) {
                is Result.Success -> { cargarEquipos(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun actualizarEquipo(id: Int, equipo: Equipo, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.actualizarEquipo(id, equipo)) {
                is Result.Success -> { cargarEquipos(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun eliminarEquipo(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.eliminarEquipo(id)) {
                is Result.Success -> { cargarEquipos(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }
}

// EntrenadoresViewModel
sealed class EntrenadoresUiState {
    object Loading : EntrenadoresUiState()
    data class Success(val entrenadores: List<Entrenador>) : EntrenadoresUiState()
    data class Error(val message: String) : EntrenadoresUiState()
}

class EntrenadoresViewModel : ViewModel() {
    private val repository = EntrenadorRepository()

    private val _uiState = MutableStateFlow<EntrenadoresUiState>(EntrenadoresUiState.Loading)
    val uiState: StateFlow<EntrenadoresUiState> = _uiState.asStateFlow()

    init { cargarEntrenadores() }

    fun cargarEntrenadores() {
        viewModelScope.launch {
            _uiState.value = EntrenadoresUiState.Loading
            when (val r = repository.getEntrenadores()) {
                is Result.Success -> _uiState.value = EntrenadoresUiState.Success(r.data)
                is Result.Error   -> _uiState.value = EntrenadoresUiState.Error(r.message)
                else -> {}
            }
        }
    }

    fun crearEntrenador(entrenador: Entrenador, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.crearEntrenador(entrenador)) {
                is Result.Success -> { cargarEntrenadores(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun actualizarEntrenador(id: Int, entrenador: Entrenador, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.actualizarEntrenador(id, entrenador)) {
                is Result.Success -> { cargarEntrenadores(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun eliminarEntrenador(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.eliminarEntrenador(id)) {
                is Result.Success -> { cargarEntrenadores(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }
}

// PartidosViewModel
sealed class PartidosUiState {
    object Loading : PartidosUiState()
    data class Success(val partidos: List<Partido>) : PartidosUiState()
    data class Error(val message: String) : PartidosUiState()
}

class PartidosViewModel : ViewModel() {
    private val repository = PartidoRepository()

    private val _uiState = MutableStateFlow<PartidosUiState>(PartidosUiState.Loading)
    val uiState: StateFlow<PartidosUiState> = _uiState.asStateFlow()

    init { cargarPartidos() }

    fun cargarPartidos() {
        viewModelScope.launch {
            _uiState.value = PartidosUiState.Loading
            when (val r = repository.getPartidos()) {
                is Result.Success -> _uiState.value = PartidosUiState.Success(r.data)
                is Result.Error   -> _uiState.value = PartidosUiState.Error(r.message)
                else -> {}
            }
        }
    }

    fun crearPartido(partido: Partido, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.crearPartido(partido)) {
                is Result.Success -> { cargarPartidos(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun actualizarPartido(id: Int, partido: Partido, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.actualizarPartido(id, partido)) {
                is Result.Success -> { cargarPartidos(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun eliminarPartido(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.eliminarPartido(id)) {
                is Result.Success -> { cargarPartidos(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }
}

// EstadisticasViewModel
sealed class EstadisticasUiState {
    object Loading : EstadisticasUiState()
    data class Success(val estadisticas: List<EstadisticasJugador>) : EstadisticasUiState()
    data class Error(val message: String) : EstadisticasUiState()
}

class EstadisticasViewModel : ViewModel() {
    private val repository = EstadisticasRepository()

    private val _uiState = MutableStateFlow<EstadisticasUiState>(EstadisticasUiState.Loading)
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    init { cargarEstadisticas() }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _uiState.value = EstadisticasUiState.Loading
            when (val r = repository.getEstadisticas()) {
                is Result.Success -> _uiState.value = EstadisticasUiState.Success(r.data)
                is Result.Error   -> _uiState.value = EstadisticasUiState.Error(r.message)
                else -> {}
            }
        }
    }

    fun crearEstadistica(est: EstadisticasJugador, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.crearEstadistica(est)) {
                is Result.Success -> { cargarEstadisticas(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }

    fun eliminarEstadistica(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.eliminarEstadistica(id)) {
                is Result.Success -> { cargarEstadisticas(); onSuccess() }
                is Result.Error   -> onError(r.message)
                else -> {}
            }
        }
    }
}
