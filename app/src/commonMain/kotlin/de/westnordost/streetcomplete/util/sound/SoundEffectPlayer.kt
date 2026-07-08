package de.westnordost.streetcomplete.util.sound

import kotlinx.io.IOException

/** Plays short sound effects in response to user actions */
interface SoundEffectPlayer {
    /** Play the sound at the given [resourcePath]. If it hasn't been loaded yet, it will load now.
     *
     *  @throws IOException if the file cannot be loaded */
    fun play(resourcePath: String)
}
