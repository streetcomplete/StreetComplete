package de.westnordost.streetcomplete.screens.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ui.util.composableContent

class OverlaysTutorialFragment : Fragment() {

    interface Listener {
        fun onOverlaysTutorialFinished()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent {
            Surface {
                OverlaysTutorialScreen(
                    onDismissRequest = {},
                    onFinished = { listener?.onOverlaysTutorialFinished() },
                    dismissOnBackPress = false
                )
            }
        }
}
