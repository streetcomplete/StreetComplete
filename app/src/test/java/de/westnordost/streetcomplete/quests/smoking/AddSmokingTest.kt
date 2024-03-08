package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddSmokingTest {
    private val questType = AddSmoking()

    @Test fun `not applicable to empty tags`() {
        assertFalse(questType.isApplicableTo(node()))
    }

    @Test fun `not applicable to disused pub`() {
        assertFalse(questType.isApplicableTo(
            node(tags = mapOf("disused:amenity" to "pub"))
        ))
    }

    @Test fun `applicable to places offering also takeaways`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "restaurant",
            "takeaway" to "yes",
        ))))
    }

    @Test fun `not applicable to places offering only takeaways`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "restaurant",
            "takeaway" to "only",
        ))))
    }

    @Test fun `not applicable to recently solved smoking quest`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "smoking" to "isolated",
        ))))
    }

    @Test fun `applicable to old places tagged with old smoking date`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "food_court",
            "smoking" to "yes",
            "check_date:smoking" to "2004-10-10"
        ))))
    }

    // we assume that seating is not present if not indicated for bakery and similar
    @Test fun `not applicable to bakery without indicated seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
        ))))

        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "no",
        ))))

        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `not applicable to bakery without any seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "no",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to bakery with any indicated seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "bar_table",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "yes",
            "outdoor_seating" to "no",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "outdoor_seating" to "terrace",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "no",
            "outdoor_seating" to "yes",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "bar_table",
            "outdoor_seating" to "yes",
        ))))
    }

    // nighclubs etc. may have outdoor smoking areas even if no seating is present
    @Test fun `applicable to nightclub without any seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "nightclub",
            "indoor_seating" to "no",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to outdoor seatings`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "leisure" to "outdoor_seating",
        ))))
    }

    // we assume that seating is present if not indicated for cafe and similar
    @Test fun `applicable to cafe without indicated seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "no",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `not applicable to cafe without any seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "no",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to cafe with any indicated seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "bar_table",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "yes",
            "outdoor_seating" to "no",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "outdoor_seating" to "terrace",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "no",
            "outdoor_seating" to "yes",
        ))))

        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "bar_table",
            "outdoor_seating" to "patio",
        ))))
    }

    @Test fun `outside answer sets correct answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("smoking", "outside")),
            questType.answerApplied(SmokingAllowed.OUTSIDE)
        )
    }
}
