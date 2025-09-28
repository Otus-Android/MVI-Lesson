package ru.otus.mvi.presentation.orbitmvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.otus.mvi.domain.CharactersRepository
import ru.otus.mvi.domain.RaMCharacter

class CharactersViewModel(
    private val repository: CharactersRepository
) : ViewModel(), ContainerHost<CharactersViewModel.State, CharactersViewModel.SideEffect> {

    override val container = container<State, SideEffect>(State()) {
        loadCharacters()
    }

    data class State(
        val characters: List<RaMCharacter> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed interface SideEffect {
        data class ShowError(val throwable: Throwable) : SideEffect
    }

    fun loadCharacters() = intent {
        reduce { state.copy(isLoading = true) }

        viewModelScope.launch {
            repository.getAllCharacters()
                .onSuccess { characters ->
                    reduce {
                        state.copy(
                            characters = characters,
                            isLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(SideEffect.ShowError(throwable))
                }
        }
    }

    fun refresh() = loadCharacters()
}