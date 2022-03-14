package de.westnordost.streetcomplete.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.provider.Settings
import android.util.SparseIntArray
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/** Simple wrapper to enable just playing a sound effect from raw resources */
class SoundFx(private val context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build())
        .build()

    private val soundIds: SparseIntArray = SparseIntArray()

    // map of sampleId -> continuation of sampleId
    private val loadCompleteContinuations = mutableMapOf<Int, Continuation<Int>>()

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, _ ->
            loadCompleteContinuations[soundId]?.resume(soundId)
        }
    }

    // will not return until the loading of the sound is complete
    private suspend fun prepare(@RawRes resId: Int): Int = suspendCancellableCoroutine { cont ->
        val soundId = soundPool.load(context, resId, 1)
        loadCompleteContinuations[soundId] = cont
    }

    suspend fun play(@RawRes resId: Int) = withContext(Dispatchers.IO) {
        val isTouchSoundsEnabled = Settings.System.getInt(context.contentResolver,
            Settings.System.SOUND_EFFECTS_ENABLED,
            1) != 0
        if (isTouchSoundsEnabled) {
            if (soundIds[resId] == 0) soundIds.put(resId, prepare(resId))
            soundPool.play(soundIds[resId], 1f, 1f, 1, 0, 1f)
        }
    }
}
