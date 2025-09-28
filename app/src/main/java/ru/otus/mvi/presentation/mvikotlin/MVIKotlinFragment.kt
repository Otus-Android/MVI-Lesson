package ru.otus.mvi.presentation.mvikotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.otus.mvi.databinding.FragmentMvikotlinBinding
import ru.otus.mvi.domain.RaMCharacter
import ru.otus.mvi.getInjector
import ru.otus.mvi.presentation.CharactersAdapter
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import androidx.lifecycle.Lifecycle

class MVIKotlinFragment : Fragment() {

    private var _binding: FragmentMvikotlinBinding? = null
    private val binding get() = _binding!!

    private val adapter = CharactersAdapter()

    private lateinit var store: CharactersStore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMvikotlinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = getInjector().provideCharactersRepository()
        val storeFactory: StoreFactory = DefaultStoreFactory()

        store = CharactersStoreFactory(storeFactory, repository).create()

        binding.uiRecyclerView.adapter = adapter
        binding.uiRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.uiSwipeRefreshLayout.setOnRefreshListener {
            store.accept(CharactersStore.Intent.Refresh)
        }

        store.states
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: CharactersStore.State) {
        adapter.submitList(state.characters)

        when {
            state.isLoading -> showLoading()
            state.isError -> {
                showList()
                Toast.makeText(
                    requireContext(),
                    "Error while loading data",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> showList()
        }
    }

    private fun showLoading() {
        hideAll()
        binding.uiProgressBar.visibility = View.VISIBLE
    }

    private fun showList() {
        hideAll()
        binding.uiRecyclerView.visibility = View.VISIBLE
    }

    private fun hideAll() {
        binding.uiRecyclerView.visibility = View.GONE
        binding.uiProgressBar.visibility = View.GONE
        binding.uiMessage.visibility = View.GONE
        binding.uiSwipeRefreshLayout.isRefreshing = false
    }
}