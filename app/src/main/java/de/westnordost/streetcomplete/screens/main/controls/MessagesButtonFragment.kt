package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Handles showing a button with a little counter that shows how many unread messages there are */
class MessagesButtonFragment : Fragment(R.layout.fragment_messages_button) {

    interface Listener {
        fun onClickShowMessage(message: Message)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val viewModel by viewModel<MessagesButtonViewModel>()
    private val messagesButton get() = view as MessagesButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messagesButton.setOnClickListener {
            viewLifecycleScope.launch {
                val message = viewModel.popMessage()
                if (message != null) {
                    listener?.onClickShowMessage(message)
                }
            }
        }

        observe(viewModel.messagesCount) { messagesCount ->
            messagesButton.messagesCount = messagesCount
            messagesButton.isGone = messagesCount <= 0
        }
    }
}
