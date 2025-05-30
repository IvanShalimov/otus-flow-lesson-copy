package ru.otus.flow.presentation.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import ru.otus.flow.domain.CharactersRepository
import kotlinx.coroutines.launch
import ru.otus.flow.presentation.CharactersState

class FinishCharactersViewModel(
    private val repository: CharactersRepository
) : ViewModel() {

    private val _state = MutableStateFlow(loadingState())
    val state: StateFlow<CharactersState> = _state.asStateFlow()

    private fun loadingState() = CharactersState(
        items = listOf(),
        isLoading = true,
        isError = false,
        showDialog = false,
        dialogMessage = ""
    )

    init {
        requestCharacters()
    }

    fun refresh() {
        requestCharacters()
    }

    fun handleClick(id: Long) {
        viewModelScope.launch {
            repository.getCharacterById(id)
                .catch { e ->
                    _state.update { state ->
                        state.copy(isError = true)
                    }
                }
                .collect { character ->
                    _state.update {
                        it.copy(
                            showDialog = true,
                            dialogMessage = character.name
                        )
                    }
                }
        }
    }

    private fun requestCharacters() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAllCharacters()
                .onSuccess { characters ->
                    _state.update {
                        CharactersState(
                            items = characters,
                            isLoading = false,
                            isError = false,
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        CharactersState(
                            items = listOf(),
                            isLoading = false,
                            isError = true,
                        )
                    }
                }
        }
    }

    private fun requestCharactersWithFlow() {
        viewModelScope.launch {
            repository.getAllCharactersByFlow()
                .catch { e ->
                    _state.update {
                        CharactersState(
                            items = listOf(),
                            isLoading = false,
                            isError = true,
                        )
                    }
                }
                .collect { characters ->
                    _state.update {
                        CharactersState(
                            items = characters,
                            isLoading = false,
                            isError = false,
                        )
                    }
                }
        }
    }

    fun dialogDismoss() {
        _state.update {
            it.copy(
                showDialog = false,
                dialogMessage = ""
            )
        }
    }
}
