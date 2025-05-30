package ru.otus.flow.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import ru.otus.flow.domain.CharactersRepository
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val repository: CharactersRepository
) : ViewModel() {

    private val _state = MutableLiveData<CharactersState>()
    val state: LiveData<CharactersState> = _state

    init {
        _state.value = CharactersState(
            items = listOf(),
            isLoading = true,
            isError = false,
        )

        requestCharacters()
    }

    fun refresh() {
        requestCharacters()
    }

    fun handleClick(id: Long) {
        viewModelScope.launch {
            repository.getCharacterById(id)
                .catch { e ->
                    _state.value = _state.value.copy(isError = true)
                }
                .collect { character ->
                    _state.value = _state.value.copy(
                        showDialog = true,
                        dialogMessage = character.name
                    )
                }
        }
    }

    private fun requestCharacters() {
        viewModelScope.launch {
            repository.getAllCharacters()
                .onSuccess { characters ->
                    _state.value = CharactersState(
                        items = characters,
                        isLoading = false,
                        isError = false,
                    )
                }
                .onFailure {
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
            // Сделай запрос с использованем Flow
        }
    }

    fun dialogDismoss() {
        _state.value = _state.value.copy(
            showDialog = false,
            dialogMessage = ""
        )
    }
}
