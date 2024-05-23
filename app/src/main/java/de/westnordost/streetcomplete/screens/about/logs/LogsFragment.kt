package de.westnordost.streetcomplete.screens.about.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ui.util.composableContent
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the app logs */
class LogsFragment : Fragment() {

    private val viewModel by viewModel<LogsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent { Surface {
            LogsScreen(
                viewModel = viewModel,
                onClickBack = { /* TODO */ }
            )
        } }
}
