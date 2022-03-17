package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer

import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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

    @Test fun `applicable to old places tagged with smoking-date`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "food_court",
            "smoking" to "yes",
            "smoking:date" to "2014-10-10"
        ))))
    }

    /* we assume that seating is not present if not indicated for bakery and similar */
    @Test fun `not applicable to bakery without indicated seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
        ))))
    }

    @Test fun `not applicable to bakery without any seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "no",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to bakery with indoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "bar_table",
        ))))
    }

    @Test fun `applicable to bakery with only indoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "yes",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `not applicable to bakery without indoor seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to bakery with outdoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "outdoor_seating" to "terrace",
        ))))
    }

    @Test fun `applicable to bakery with only outdoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "no",
            "outdoor_seating" to "yes",
        ))))
    }

    @Test fun `not applicable to bakery without outdoor seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "outdoor_seating" to "no",
        ))))
    }


    @Test fun `applicable to bakery with both seatings`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "shop" to "bakery",
            "indoor_seating" to "bar_table",
            "outdoor_seating" to "yes",
        ))))
    }

    /* nighclubs etc. may have outdoor smoking areas even if no seating is present */
    @Test fun `applicable to nightclub without any seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
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



    /* we assume that seating is present if not indicated for cafe and similar */
    @Test fun `applicable to cafe without indicated seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
        ))))
    }

    @Test fun `not applicable to cafe without any seating`() {
        assertFalse(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "no",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to cafe with indoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "bar_table",
        ))))
    }

    @Test fun `applicable to cafe with only indoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "yes",
            "outdoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to cafe without indoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "no",
        ))))
    }

    @Test fun `applicable to cafe with outdoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "outdoor_seating" to "terrace",
        ))))
    }

    @Test fun `applicable to cafe with only outdoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "no",
            "outdoor_seating" to "yes",
        ))))
    }

    @Test fun `applicable to cafe without outdoor seating`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "outdoor_seating" to "no",
        ))))
    }


    @Test fun `applicable to cafe with both seatings`() {
        assertTrue(questType.isApplicableTo(node(tags = mapOf(
            "amenity" to "cafe",
            "indoor_seating" to "bar_table",
            "outdoor_seating" to "patio",
        ))))
    }




    @Test fun `outside answer sets correct answer`() {
        questType.verifyAnswer(
            SmokingAllowed.OUTSIDE,
            StringMapEntryAdd("smoking", "outside"),
        )
    }

}
