package ru.otus.flow.presentation.models


sealed class UiEvent {
    data class ShowDialog(val messageInfo: String) : UiEvent()
}