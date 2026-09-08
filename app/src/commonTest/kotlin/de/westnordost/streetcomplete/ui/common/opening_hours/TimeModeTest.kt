package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.runtime.saveable.SaverScope
import de.westnordost.streetcomplete.ui.util.SerializableSaver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.serializer

class TimeModeTest {
    @Test
    fun collectionTimesModeCanBeSavedAndRestored() {
        val saver = SerializableSaver<TimeMode>(serializer())
        val scope = SaverScope { true }
        for (mode in TimeMode.entries) {
            val saved = with(saver) { scope.save(mode) }
            assertNotNull(saved)
            assertEquals(mode, saver.restore(saved))
        }
    }
}
