package de.westnordost.streetcomplete.util.sound

import de.westnordost.streetcomplete.desktopFilesDirectory
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlinx.io.IOException

/** Plays the existing bundled WAV effects through the JVM desktop audio mixer. */
class DesktopSoundEffectPlayer : SoundEffectPlayer, AutoCloseable {
    private val clips = mutableMapOf<String, Clip>()

    @Synchronized
    override fun play(resourcePath: String) {
        val clip = clips.getOrPut(resourcePath) { load(resourcePath) }
        clip.stop()
        clip.framePosition = 0
        clip.start()
    }

    @Synchronized
    override fun close() {
        clips.values.forEach(Clip::close)
        clips.clear()
    }

    private fun load(resourcePath: String): Clip {
        val file = File(desktopFilesDirectory(), resourcePath)
        if (!file.isFile) throw IOException("Missing sound resource: $resourcePath")
        return try {
            AudioSystem.getAudioInputStream(file).use { input ->
                AudioSystem.getClip().apply { open(input) }
            }
        } catch (error: Exception) {
            throw IOException("Could not load sound resource: $resourcePath", error)
        }
    }
}
