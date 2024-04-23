package de.westnordost.streetcomplete.ui.util

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ui.theme.AppTheme

fun Fragment.singleComposable(content: @Composable () -> Unit): View =
    ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycle))
        setContent {
            AppTheme {
                content()
            }
        }
    }
