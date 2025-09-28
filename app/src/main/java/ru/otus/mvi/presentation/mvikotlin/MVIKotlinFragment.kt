package ru.otus.mvi.presentation.mvikotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.otus.mvi.getInjector

class MVIKotlinFragment : Fragment() {

    private lateinit var store: CharactersStore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = getInjector().provideCharactersRepository()
        val storeFactory: StoreFactory = DefaultStoreFactory()
        store = CharactersStoreFactory(storeFactory, repository).create()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CharactersScreen(store = store)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store.labels
            .onEach { label ->
                when (label) {
                    is CharactersStore.Label.ErrorLoadingCharacters -> {
                        Toast.makeText(
                            requireContext(),
                            "Error while loading data: ${label.throwable.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
