package ru.otus.flow.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.viewModels
import ru.otus.flow.databinding.FragmentCharactersBinding
import ru.otus.flow.di.InjectorProvider

@Suppress("UNCHECKED_CAST")
class CharactersFragment : Fragment() {

    private val viewModel: CharactersViewModel by viewModels(
        factoryProducer = {
            (requireContext().applicationContext as InjectorProvider)
                .injector
                .provideViewModelFactory()
        }
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // Устанавливаем Compose-контент
                FinishCharactersScreen(
                    state = viewModel.state.observeAsState() as State<CharactersState>,
                    onRefresh = { viewModel.refresh() },
                    onItemClick = { viewModel.handleClick(it)},
                    onDialogDismiss = {viewModel.dialogDismoss()},
                    onSearchQueryChanged = { query -> viewModel.search(query) }
                )
            }
        }
    }
}