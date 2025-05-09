package ru.otus.flow.presentation.finish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import ru.otus.flow.databinding.FragmentCharactersBinding
import ru.otus.flow.di.InjectorProvider
import ru.otus.flow.presentation.CharactersAdapter
import ru.otus.flow.presentation.CharactersState
import androidx.lifecycle.Lifecycle.State
import ru.otus.flow.presentation.models.UiEvent


class FinishCharactersFragment : Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinishCharactersViewModel by viewModels(
        factoryProducer = {
            (requireContext().applicationContext as InjectorProvider)
                .injector
                .provideFinishViewModelFactory()
        }
    )

    private val adapter = CharactersAdapter {
        viewModel.handleClick(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uiRecyclerView.adapter = adapter
        binding.uiRecyclerView.layoutManager = LinearLayoutManager(context)

        subscribeUI()

        binding.uiSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeUI() {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // New code here
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(State.STARTED) {
                viewModel.state.collect { state: CharactersState ->
                    when {
                        state.isError -> Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                        state.isLoading -> showUILoading()
                        else -> {
                            adapter.submitList(state.items)
                            showUIContent()
                        }
                    }
                }


            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(State.STARTED) {
                viewModel.dialogEvents.collect { itemInfo ->
                    // Показываем диалог с полученной информацией
                    showInfoDialog(itemInfo)
                }
            }
        }
    }

    private fun showUILoading() {
        binding.uiRecyclerView.visibility = View.GONE
        binding.uiProgressBar.visibility = View.VISIBLE
        binding.uiMessage.visibility = View.GONE
        binding.uiSwipeRefreshLayout.isRefreshing = false
    }

    private fun showUIContent() {
        binding.uiRecyclerView.visibility = View.VISIBLE
        binding.uiProgressBar.visibility = View.GONE
        binding.uiMessage.visibility = View.GONE
        binding.uiSwipeRefreshLayout.isRefreshing = false
    }

    private fun showInfoDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Информация")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}