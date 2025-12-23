package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.answerAppliedTo
import kotlin.test.Test
import kotlin.test.assertEquals

class AddRoadNameTest {

    private val questType = AddRoadName()

    private val tags = mapOf("highway" to "residential")

    @Test fun `apply no name answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("noname", "yes")),
            questType.answerAppliedTo(RoadNameAnswer.NoName, tags)
        )
    }

    @Test fun `apply name answer with one name`() {
        assertEquals(
            setOf(StringMapEntryAdd("name", "my name")),
            questType.answerAppliedTo(roadName(LocalizedName("", "my name")), tags)
        )
    }

    @Test fun `apply ref answer`() {
        val refs = listOf("9", "A", "A2", "L 3211", "US-9", "MEX 25", "1234", "G9321")
        for (ref in refs) {
            assertEquals(
                setOf(StringMapEntryAdd("ref", ref)),
                questType.answerAppliedTo(roadName(LocalizedName("", ref)), tags)
            )
        }
    }

    @Test fun `do not apply ref answer if it is a localized name`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "A1"),
                StringMapEntryAdd("name:de", "A1")
            ),
            questType.answerAppliedTo(roadName(
                LocalizedName("", "A1"),
                LocalizedName("de", "A1")
            ), tags)
        )
    }

    @Test fun `apply name answer with multiple names`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("name", "Altona / All-Too-Close"),
                StringMapEntryAdd("name:en", "All-Too-Close"),
                StringMapEntryAdd("name:de", "Altona")
            ),
            questType.answerAppliedTo(roadName(
                LocalizedName("", "Altona / All-Too-Close"),
                LocalizedName("de", "Altona"),
                LocalizedName("en", "All-Too-Close")
            ), tags)
        )
    }

    // convenience method
    private fun roadName(vararg names: LocalizedName): RoadName {
        return RoadName(names.toList())
    }
}
