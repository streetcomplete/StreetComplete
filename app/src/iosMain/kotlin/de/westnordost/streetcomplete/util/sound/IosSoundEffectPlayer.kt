package de.westnordost.streetcomplete.util.sound

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import platform.AudioToolbox.AudioServicesCreateSystemSoundID
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.SystemSoundID
import platform.CoreFoundation.CFURLRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSURL

class IosSoundEffectPlayer(
    val baseDir: String
) : SoundEffectPlayer {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // map of path -> sound id
    private val soundMap = HashMap<String, SystemSoundID>()

    // make sure sounds with the same path are not loaded twice
    private val mutex = Mutex()

    @OptIn(ExperimentalForeignApi::class)
    private fun load(resourcePath: String): SystemSoundID {
        val url = NSURL.fileURLWithPath("$baseDir/$resourcePath")
        memScoped {
            val cfurl: CFURLRef = CFBridgingRetain(url)!!.reinterpret()
            val soundIdVar = alloc<UIntVarOf<SystemSoundID>>()
            val result = AudioServicesCreateSystemSoundID(cfurl, soundIdVar.ptr)
            CFBridgingRelease(cfurl)

            if (result == 0) {
                return soundIdVar.value
            } else {
                throw IOException("Failed to decode sound")
            }
        }
    }

    override fun play(resourcePath: String) {
        coroutineScope.launch {
            val soundId = mutex.withLock {
                if (!soundMap.containsKey(resourcePath)) {
                    soundMap[resourcePath] = load(resourcePath)
                }
                soundMap.getValue(resourcePath)
            }
            AudioServicesPlaySystemSound(soundId)
        }
    }
}
