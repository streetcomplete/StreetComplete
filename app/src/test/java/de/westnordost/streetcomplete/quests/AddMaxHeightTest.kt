package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.max_height.*
import org.junit.Test
import org.mockito.Mockito.mock

class AddMaxHeightTest {

    private val questType = AddMaxHeight(mock(OverpassMapDataDao::class.java))

    @Test fun `apply metric height answer`() {
        questType.verifyAnswer(
            MaxHeight(MetricMeasure(3.5)),
            StringMapEntryAdd("maxheight","3.5")
        )
    }

    @Test fun `apply imperial height answer`() {
        questType.verifyAnswer(
            MaxHeight(ImperialMeasure(10, 6)),
            StringMapEntryAdd("maxheight","10'6\"")
        )
    }

    @Test fun `apply default height answer`() {
        questType.verifyAnswer(
            NoMaxHeightSign(true),
            StringMapEntryAdd("maxheight","default")
        )
    }

    @Test fun `apply below default height answer`() {
        questType.verifyAnswer(
            NoMaxHeightSign(false),
            StringMapEntryAdd("maxheight","below_default")
        )
    }
}
