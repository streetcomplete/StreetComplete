package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.data.atp.AtpDao
import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.atp.ReportType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.*
import de.westnordost.streetcomplete.testutils.on

class AddOpeningHoursAtpTest {
    val atpDb: AtpDao = mock()
    private val questType = AddOpeningHoursAtp(mock(), atpDb)

    @Test fun `isApplicableTo returns false for known places with recently edited opening hours`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("shop" to "sports", "name" to "Atze's Angelladen", "opening_hours" to "Mo-Fr 10:00-20:00"), timestamp = nowAsEpochMilliseconds())
        ))
    }

    @Test fun `isApplicableTo returns true for known places with recently edited opening hours reported as outdated by ATP`() {
        val osmTags = mapOf("shop" to "sports", "name" to "Chrabąszcz", "opening_hours" to "Mo-Fr 10:00-20:00")
        val specialAtpDb: AtpDao = mock()
        on(specialAtpDb.getAllWithMatchingOsmElement(
            ElementKey(ElementType.NODE, 2))
        ).thenReturn(listOf(AtpEntry(
            position = LatLon(1.0, 1.0),
            id = 100,
            osmMatch = ElementKey(ElementType.NODE, 2),
            tagsInATP = mapOf("shop" to "sports", "name" to "Chrabąszcz", "opening_hours" to "Mo-Sa 10:00-20:00"),
            tagsInOSM = osmTags,
            reportType = ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP
        )))
        val specialQuestType = AddOpeningHoursAtp(mock(), specialAtpDb)
        assertTrue(specialQuestType.isApplicableTo(
            node(id = 2, tags = osmTags, timestamp = nowAsEpochMilliseconds())
        ))
    }
}
