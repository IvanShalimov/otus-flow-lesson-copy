package ru.otus.flow.presentation.finish

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import ru.otus.flow.domain.CharactersRepository
import kotlinx.coroutines.launch
import ru.otus.flow.presentation.CharactersState
import ru.otus.flow.presentation.models.UiEvent

class FinishCharactersViewModel(
    private val repository: CharactersRepository
) : ViewModel() {

    // Создаем Channel для событий
    private val _dialogEvents = Channel<String>()
    val dialogEvents = _dialogEvents.receiveAsFlow()

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // New code here
    private val _state = MutableStateFlow(loadingState())
    val state: StateFlow<CharactersState> = _state.asStateFlow()

    private fun loadingState() = CharactersState(
        items = listOf(),
        isLoading = true,
        isError = false,
    )

    init {
        requestCharacters()
    }

    fun refresh() {
        requestCharacters()
    }

    fun handleClick(id: Long) {
        viewModelScope.launch {
            repository.getCharacterById(id).catch { e ->
                    _state.update { state ->
                        state.copy(
                            isError = true
                        )
                    }
                }.collect { character ->
                    _dialogEvents.send(
                        character.name
                    )
                }
        }
    }

    private fun requestCharacters() {
        viewModelScope.launch {
            repository.getAllCharacters().onSuccess { characters ->
                    _state.value = CharactersState(
                        items = characters,
                        isLoading = false,
                        isError = false,
                    )
                }.onFailure {
                    _state.value = CharactersState(
                        items = listOf(),
                        isLoading = false,
                        isError = true,
                    )
                }
        }
    }

    private fun requestCharactersWithFlow() {
        viewModelScope.launch {
            repository.getAllCharactersByFlow()
                //.runCatching {  } // Можем и Flow трансформировать в Result
                .catch { e ->
                    _state.value = CharactersState(
                        items = listOf(),
                        isLoading = false,
                        isError = true,
                    )
                }.collect { characters ->
                    _state.value = CharactersState(
                        items = characters,
                        isLoading = false,
                        isError = false,
                    )
                }
        }
    }
}
