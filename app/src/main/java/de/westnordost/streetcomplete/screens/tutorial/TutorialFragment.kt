package de.westnordost.streetcomplete.screens.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ui.util.composableContent

class TutorialFragment : Fragment() {

    interface Listener {
        fun onTutorialFinished()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent {
            Surface {
                IntroTutorialScreen(
                    onDismissRequest = {},
                    onFinished = { listener?.onTutorialFinished() },
                    dismissOnBackPress = false,
                )
            }
        }
}
