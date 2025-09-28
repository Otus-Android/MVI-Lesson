package ru.otus.mvi.presentation.mvikotlin

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import ru.otus.mvi.domain.CharactersRepository
import ru.otus.mvi.domain.RaMCharacter

internal class CharactersStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: CharactersRepository
) {

    fun create(): CharactersStore =
        object : CharactersStore, Store<CharactersStore.Intent, CharactersStore.State, Nothing> by storeFactory.create(
            name = "CharactersStore",
            initialState = CharactersStore.State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data object LoadCharacters : Action
    }

    private sealed interface Msg {
        data object Loading : Msg
        data class CharactersLoaded(val characters: List<RaMCharacter>) : Msg
        data object Error : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadCharacters)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<CharactersStore.Intent, Action, CharactersStore.State, Msg, Nothing>() {
        override fun executeIntent(intent: CharactersStore.Intent, getState: () -> CharactersStore.State) {
            when (intent) {
                CharactersStore.Intent.LoadCharacters -> loadCharacters()
                CharactersStore.Intent.Refresh -> loadCharacters()
            }
        }

        override fun executeAction(action: Action, getState: () -> CharactersStore.State) {
            when (action) {
                Action.LoadCharacters -> loadCharacters()
            }
        }

        private fun loadCharacters() {
            dispatch(Msg.Loading)
            scope.launch {
                repository.getAllCharacters()
                    .onSuccess { characters ->
                        dispatch(Msg.CharactersLoaded(characters))
                    }
                    .onFailure {
                        dispatch(Msg.Error)
                    }
            }
        }
    }

    private object ReducerImpl : Reducer<CharactersStore.State, Msg> {
        override fun CharactersStore.State.reduce(msg: Msg): CharactersStore.State =
            when (msg) {
                Msg.Loading -> copy(isLoading = true, isError = false)
                is Msg.CharactersLoaded -> copy(
                    characters = msg.characters,
                    isLoading = false,
                    isError = false
                )
                Msg.Error -> copy(isLoading = false, isError = true)
            }
    }
}