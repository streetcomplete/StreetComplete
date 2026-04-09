package de.westnordost.streetcomplete.screens.settings.messages

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.preferences.Preferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KClass

@Stable
abstract class MessageSelectionViewModel : ViewModel() {
    abstract val disabledMessageTypes: StateFlow<Set<KClass<out Message>>>
    abstract fun toggleDisableMessageType(messageType: KClass<out Message>, disable: Boolean)
}

@Stable
class MessageSelectionViewModelImpl(
    private val prefs: Preferences,
) : MessageSelectionViewModel() {

    override val disabledMessageTypes: StateFlow<Set<KClass<out Message>>> = callbackFlow {
        val listener = prefs.onDisabledMessageTypesChanged { trySend(prefs.disabledMessageTypes) }
        awaitClose { listener.deactivate() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, prefs.disabledMessageTypes)

    override fun toggleDisableMessageType(messageType: KClass<out Message>, disable: Boolean) {
        prefs.disabledMessageTypes = if (disable) {
            prefs.disabledMessageTypes + messageType
        } else {
            prefs.disabledMessageTypes - messageType
        }
    }
}
