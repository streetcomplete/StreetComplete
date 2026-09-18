package de.westnordost.streetcomplete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.Theme
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppViewModelTest {
    private val settings: ObservableSettings = mock()
    private val listener: SettingsListener = mock()
    private val stringListeners = mutableMapOf<String, (String?) -> Unit>()
    private var keepScreenOnChanged: (Boolean) -> Unit = {}

    init {
        every { settings.getStringOrNull(any()) } returns null
        every { settings.addStringOrNullListener(any(), any()) } calls { (key: String, callback: (String?) -> Unit) ->
            stringListeners[key] = callback
            listener
        }
        every { settings.addBooleanListener(any(), any(), any()) } calls { args ->
            keepScreenOnChanged = args.arg(2)
            listener
        }
    }

    @Test fun `preferences update the existing app and listeners are released with its owner`() {
        val viewModel = AppViewModel(Preferences(settings), SavedStateHandle())
        val store = ViewModelStore().apply { put("app", viewModel) }

        stringListeners.getValue("theme.select")("DARK")
        stringListeners.getValue("language.select")("sr-Latn")
        keepScreenOnChanged(true)
        assertEquals(Theme.DARK, viewModel.theme.value)
        assertEquals("sr-Latn", viewModel.language.value)
        assertTrue(viewModel.keepScreenOn.value)

        stringListeners.getValue("theme.select")(null)
        stringListeners.getValue("language.select")(null)
        assertEquals(Theme.SYSTEM, viewModel.theme.value)
        assertNull(viewModel.language.value)

        store.clear()
        verify(exactly(3)) { listener.deactivate() }
    }

    @Test fun `pending entry requests survive restoration and are consumed once`() {
        val savedState = SavedStateHandle(mapOf("uri" to "geo:52,13", "settings" to true))
        val viewModel = AppViewModel(Preferences(settings), savedState)
        assertEquals("geo:52,13", viewModel.pendingUri.value)
        assertTrue(viewModel.openSettings.value)

        viewModel.consumeUri()
        viewModel.consumeSettingsRequest()
        assertNull(savedState.get<String>("uri"))
        assertFalse(savedState.get<Boolean>("settings")!!)

        // The same URL is a new request after the earlier one has been consumed.
        viewModel.openUri("geo:52,13")
        assertEquals("geo:52,13", viewModel.pendingUri.value)
    }
}
