package de.westnordost.streetcomplete.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.provider.Settings
import android.util.SparseIntArray
import androidx.annotation.RawRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Simple wrapper to enable just playing a sound effect from raw resources */
@Singleton class SoundFx @Inject constructor(private val context: Context) {
    private val soundPool: SoundPool

    private val soundIds: SparseIntArray = SparseIntArray()

    // map of sampleId -> continuation of sampleId
    private val loadCompleteContinuations = mutableMapOf<Int, Continuation<Int>>()

    init {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                .build()
        } else {
            SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            loadCompleteContinuations[soundId]?.resume(soundId)
        }
    }

    // will not return until the loading of the sound is complete
    private suspend fun prepare(@RawRes resId: Int): Int = suspendCoroutine { cont ->
        val soundId = soundPool.load(context, resId, 1)
        loadCompleteContinuations[soundId] = cont
    }

    suspend fun play(@RawRes resId: Int) {
        if (soundIds[resId] == 0) soundIds.put(resId, prepare(resId))
        val isTouchSoundsEnabled = Settings.System.getInt(context.contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0
        if (isTouchSoundsEnabled) soundPool.play(soundIds[resId], 1f, 1f, 1, 0, 1f)
    }
}