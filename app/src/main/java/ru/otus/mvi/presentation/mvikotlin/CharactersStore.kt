package ru.otus.mvi.presentation.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import ru.otus.mvi.domain.RaMCharacter

interface CharactersStore : Store<CharactersStore.Intent, CharactersStore.State, Nothing> {

    sealed interface Intent {
        data object LoadCharacters : Intent
        data object Refresh : Intent
    }

    data class State(
        val characters: List<RaMCharacter> = emptyList(),
        val isLoading: Boolean = false,
        val isError: Boolean = false
    )
}