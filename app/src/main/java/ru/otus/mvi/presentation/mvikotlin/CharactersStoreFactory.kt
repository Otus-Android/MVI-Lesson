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
        object : CharactersStore,
            Store<CharactersStore.Intent, CharactersStore.State, CharactersStore.Label> by storeFactory.create(
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
        data object LoadingFinished : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadCharacters)
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<CharactersStore.Intent, Action, CharactersStore.State, Msg, CharactersStore.Label>() {
        override fun executeIntent(intent: CharactersStore.Intent) {
            when (intent) {
                CharactersStore.Intent.LoadCharacters -> loadCharacters()
                CharactersStore.Intent.Refresh -> loadCharacters()
            }
        }

        override fun executeAction(action: Action) {
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
                    .onFailure { throwable ->
                        dispatch(Msg.LoadingFinished)
                        publish(CharactersStore.Label.ErrorLoadingCharacters(throwable))
                    }
            }
        }
    }

    private object ReducerImpl : Reducer<CharactersStore.State, Msg> {
        override fun CharactersStore.State.reduce(msg: Msg): CharactersStore.State =
            when (msg) {
                Msg.Loading -> copy(isLoading = true)
                is Msg.CharactersLoaded -> copy(
                    characters = msg.characters,
                    isLoading = false
                )
                Msg.LoadingFinished -> copy(isLoading = false)
            }
    }
}