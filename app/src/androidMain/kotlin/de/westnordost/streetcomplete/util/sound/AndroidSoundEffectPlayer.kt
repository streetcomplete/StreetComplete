package de.westnordost.streetcomplete.util.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.provider.Settings.System
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidSoundEffectPlayer(
    private val context: Context,
    private val baseDir: String
) : SoundEffectPlayer {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .build()

    // map of path -> sound id
    private val soundMap = HashMap<String, Int>()

    // map of sound id -> continuation of sound id
    private val loadCompleteContinuations = mutableMapOf<Int, CancellableContinuation<Int>>()

    // make sure sounds with the same path are not loaded twice
    private val mutex = Mutex()

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            val continuation = loadCompleteContinuations.remove(soundId)

            if (continuation?.isActive == true) {
                if (status == 0) {
                    continuation.resume(soundId)
                } else {
                    continuation.resumeWithException(IOException("Failed to decode sound"))
                }
            }
        }
    }

    private suspend fun load(resourcePath: String): Int = suspendCancellableCoroutine { cont ->
        // only complete after the sound has actually been loaded
        val afd = context.assets.openFd("$baseDir/$resourcePath")
        val soundId = soundPool.load(afd, 1)
        loadCompleteContinuations[soundId] = cont
    }

    override fun play(resourcePath: String) {
        if (!isTouchSoundsEnabled()) return

        coroutineScope.launch {
            val soundId = mutex.withLock {
                if (!soundMap.containsKey(resourcePath)) {
                    soundMap[resourcePath] = load(resourcePath)
                }
                soundMap.getValue(resourcePath)
            }
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun isTouchSoundsEnabled(): Boolean =
        System.getInt(context.contentResolver, System.SOUND_EFFECTS_ENABLED, 1) != 0
}
