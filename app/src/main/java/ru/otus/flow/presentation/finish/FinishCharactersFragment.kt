package ru.otus.flow.presentation.finish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
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
                    onDialogDismiss = {viewModel.dialogDismoss()}
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