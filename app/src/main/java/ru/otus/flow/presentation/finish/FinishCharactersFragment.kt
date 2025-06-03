package ru.otus.flow.presentation.finish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.otus.flow.di.InjectorProvider
import ru.otus.flow.presentation.FinishCharactersScreen


class FinishCharactersFragment : Fragment() {

    private val viewModel: FinishCharactersViewModel by viewModels(
        factoryProducer = {
            (requireContext().applicationContext as InjectorProvider)
                .injector
                .provideFinishViewModelFactory()
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
                    state = viewModel.state.collectAsState(),
                    onRefresh = { viewModel.refresh() },
                    onItemClick = { viewModel.handleClick(it)},
                    onDialogDismiss = {viewModel.dialogDismiss()},
                    onSearchQueryChanged = { query ->
                        viewModel.getCharactersWithDebounce(query)
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}