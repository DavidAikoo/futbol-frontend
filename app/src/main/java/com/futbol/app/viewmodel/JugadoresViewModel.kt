package com.futbol.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futbol.app.data.model.Jugador
import com.futbol.app.data.repository.JugadorRepository
import com.futbol.app.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class JugadoresUiState {
    object Loading : JugadoresUiState()
    data class Success(val jugadores: List<Jugador>) : JugadoresUiState()
    data class Error(val message: String) : JugadoresUiState()
}

class JugadoresViewModel : ViewModel() {

    private val repository = JugadorRepository()

    private val _uiState = MutableStateFlow<JugadoresUiState>(JugadoresUiState.Loading)
    val uiState: StateFlow<JugadoresUiState> = _uiState.asStateFlow()

    private val _selectedJugador = MutableStateFlow<Jugador?>(null)
    val selectedJugador: StateFlow<Jugador?> = _selectedJugador.asStateFlow()

    private val _detailState = MutableStateFlow<JugadoresUiState>(JugadoresUiState.Loading)
    val detailState: StateFlow<JugadoresUiState> = _detailState.asStateFlow()

    init {
        cargarJugadores()
    }

    fun cargarJugadores() {
        viewModelScope.launch {
            _uiState.value = JugadoresUiState.Loading
            when (val result = repository.getJugadores()) {
                is Result.Success -> _uiState.value = JugadoresUiState.Success(result.data)
                is Result.Error   -> _uiState.value = JugadoresUiState.Error(result.message)
                is Result.Loading -> _uiState.value = JugadoresUiState.Loading
            }
        }
    }

    fun cargarJugadorById(id: Int) {
        viewModelScope.launch {
            _detailState.value = JugadoresUiState.Loading
            when (val result = repository.getJugadorById(id)) {
                is Result.Success -> {
                    _selectedJugador.value = result.data
                    _detailState.value = JugadoresUiState.Success(listOf(result.data))
                }
                is Result.Error   -> _detailState.value = JugadoresUiState.Error(result.message)
                is Result.Loading -> _detailState.value = JugadoresUiState.Loading
            }
        }
    }

    fun crearJugador(jugador: Jugador, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.crearJugador(jugador)) {
                is Result.Success -> { cargarJugadores(); onSuccess() }
                is Result.Error   -> onError(result.message)
                is Result.Loading -> {}
            }
        }
    }

    fun actualizarJugador(id: Int, jugador: Jugador, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.actualizarJugador(id, jugador)) {
                is Result.Success -> { cargarJugadores(); onSuccess() }
                is Result.Error   -> onError(result.message)
                is Result.Loading -> {}
            }
        }
    }

    fun eliminarJugador(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.eliminarJugador(id)) {
                is Result.Success -> { cargarJugadores(); onSuccess() }
                is Result.Error   -> onError(result.message)
                is Result.Loading -> {}
            }
        }
    }
}
