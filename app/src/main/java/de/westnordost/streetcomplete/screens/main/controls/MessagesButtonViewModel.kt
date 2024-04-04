package de.westnordost.streetcomplete.screens.main.controls

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.messages.MessagesSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

abstract class MessagesButtonViewModel : ViewModel() {
    abstract val messagesCount: StateFlow<Int>

    abstract suspend fun popMessage(): Message?
}

class MessagesButtonViewModelImpl(
    private val messagesSource: MessagesSource
) : MessagesButtonViewModel() {

    override val messagesCount = MutableStateFlow(0)

    private val messagesSourceUpdateListener = object : MessagesSource.UpdateListener {
        override fun onNumberOfMessagesUpdated(numberOfMessages: Int) {
            messagesCount.value = numberOfMessages
        }
    }

    init {
        launch(IO) { messagesCount.value = messagesSource.getNumberOfMessages() }
        messagesSource.addListener(messagesSourceUpdateListener)
    }

    override suspend fun popMessage(): Message? = withContext(IO) {
        messagesSource.popNextMessage()
    }

    override fun onCleared() {
        messagesSource.removeListener(messagesSourceUpdateListener)
    }
}
