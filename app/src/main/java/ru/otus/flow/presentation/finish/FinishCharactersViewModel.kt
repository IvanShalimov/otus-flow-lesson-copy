package ru.otus.flow.presentation.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
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

    fun dialogDismiss() {
        _state.update {
            it.copy(
                showDialog = false,
                dialogMessage = ""
            )
        }
    }

    // Добавляем новые методы с различными Flow операторами
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun getCharactersWithDebounce(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.searchCharacters(query)
                .catch { e ->
                    _state.update { state ->
                        state.copy(isError = true, isLoading = false)
                    }
                }
                .collect { characters ->
                    _state.update {
                        it.copy(
                            items = characters,
                            isLoading = false,
                            isError = false
                        )
                    }
                }
        }
    }

    fun getCharactersWithCombine() {
        viewModelScope.launch {
            // Комбинируем несколько потоков данных
            combine(
                repository.getAllCharactersByFlow(),
                repository.getFavoriteCharactersFlow()
            ) { allCharacters, favorites ->
                allCharacters.map { character ->
                    character.copy(isFavorite = favorites.any { it.id == character.id })
                }
            }
                .collect { characters ->
                    _state.update {
                        it.copy(
                            items = characters,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun getCharactersWithRetry() {
        viewModelScope.launch {
            repository.getAllCharactersWithRetry()
                .retry(3) { cause ->
                    // Повторяем запрос до 3 раз при ошибках
                    if (cause is java.net.UnknownHostException) {
                        delay(1000)
                        true
                    } else {
                        false
                    }
                }
                .collect { characters ->
                    _state.update {
                        it.copy(
                            items = characters,
                            isLoading = false
                        )
                    }
                }
        }
    }
}
