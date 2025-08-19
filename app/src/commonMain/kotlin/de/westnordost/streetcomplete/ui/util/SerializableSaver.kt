package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class SerializableSaver<T>(private val serializer: KSerializer<T>) : Saver<T, String> {
    override fun restore(value: String): T? =
        try {
            Json.decodeFromString(serializer, value)
        } catch (_: Exception) {
            null
        }

    override fun SaverScope.save(value: T): String? {
        if (value == null) return null
        return try {
            Json.encodeToString(serializer, value)
        } catch (_: Exception) {
            null
        }
    }
}

/** Remember the `@Serializable` value produced by [init].
 *
 *  It behaves similarly to [remember][androidx.compose.runtime.remember], but the stored value will
 *  survive the activity or process recreation using the saved instance state mechanism (for example
 *  it happens when the screen is rotated in the Android application).*/
@Composable
inline fun <reified T> rememberSerializable(
    vararg inputs: Any?,
    key: String? = null,
    noinline init: () -> MutableState<T>
): MutableState<T> {
    val saver = SerializableSaver(serializer<T>())
    return rememberSaveable(
        inputs = inputs,
        stateSaver = saver,
        key = key,
        init = init
    )
}
