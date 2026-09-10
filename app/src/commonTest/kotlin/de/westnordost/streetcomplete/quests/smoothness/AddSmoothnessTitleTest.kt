package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_smoothness_title
import kotlin.test.Test
import kotlin.test.assertEquals

class AddSmoothnessTitleTest {

    @Test fun `path and road smoothness use the generic title`() {
        assertEquals(Res.string.quest_smoothness_title, AddPathSmoothness().title)
        assertEquals(Res.string.quest_smoothness_title, AddRoadSmoothness().title)
    }
}
