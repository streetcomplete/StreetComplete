package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.staticCompositionLocalOf
import de.westnordost.streetcomplete.util.SoundFx

val LocalSoundFx = staticCompositionLocalOf<SoundFx> {
    noLocalProvidedFor("LocalSoundFx")
}

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
