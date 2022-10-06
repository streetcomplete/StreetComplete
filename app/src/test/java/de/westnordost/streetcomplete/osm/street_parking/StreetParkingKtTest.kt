package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.toCheckDateString
import org.assertj.core.api.Assertions
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreetParkingTest {

    @Test fun `apply no parking on both sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "no")
            )
        )
    }

    @Test fun `apply different no parking on different sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(StreetStoppingProhibited, StreetStandingProhibited),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "no"),
                StringMapEntryAdd("parking:condition:left", "no_stopping"),
                StringMapEntryAdd("parking:condition:right", "no_standing"),
            )
        )
    }

    @Test fun `apply separate parking answer`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "separate")
            )
        )
    }

    @Test fun `apply parallel parking answer on both sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "parallel"),
                StringMapEntryAdd("parking:lane:both:parallel", "half_on_kerb"),
            )
        )
    }

    @Test fun `apply parallel parking answer with different positions on sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "parallel"),
                StringMapEntryAdd("parking:lane:left:parallel", "on_street"),
                StringMapEntryAdd("parking:lane:right:parallel", "half_on_kerb"),
            )
        )
    }

    @Test fun `apply street side parking answer with different orientations on sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PERPENDICULAR, ParkingPosition.STREET_SIDE),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.STREET_SIDE)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "perpendicular"),
                StringMapEntryAdd("parking:lane:right", "parallel"),
                StringMapEntryAdd("parking:lane:left:perpendicular", "street_side"),
                StringMapEntryAdd("parking:lane:right:parallel", "street_side"),
            )
        )
    }

    @Test fun `apply different parking positions and orientations on sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.STREET_SIDE),
                StreetParkingPositionAndOrientation(ParkingOrientation.PERPENDICULAR, ParkingPosition.PAINTED_AREA_ONLY)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "diagonal"),
                StringMapEntryAdd("parking:lane:left:diagonal", "street_side"),
                StringMapEntryAdd("parking:lane:right", "perpendicular"),
                StringMapEntryAdd("parking:lane:right:perpendicular", "painted_area_only"),
            )
        )
    }

    @Test fun `updates check date`() {
        verifyAnswer(
            mapOf("parking:lane:both" to "no"),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "no", "no"),
                StringMapEntryAdd("check_date:parking:lane", LocalDate.now().toCheckDateString())
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:lane:left:parallel" to "half_on_kerb",
                "parking:lane:right:parallel" to "on_kerb",
                "parking:condition:left" to "free",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "parallel", "parallel"),
                StringMapEntryModify("parking:lane:left:parallel", "half_on_kerb", "half_on_kerb"),
                StringMapEntryModify("parking:lane:right:parallel", "on_kerb", "on_kerb"),
                StringMapEntryAdd("check_date:parking:lane", LocalDate.now().toCheckDateString())
            )
        )
    }

    @Test fun `clean up previous tagging when applying value for each side`() {
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:lane:left:parallel" to "half_on_kerb",
                "parking:lane:right:parallel" to "on_kerb",
                "parking:condition:left" to "free",
                "parking:condition:right" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET)
            ),
            arrayOf(
                StringMapEntryDelete("parking:lane:both", "parallel"),
                StringMapEntryAdd("parking:lane:left", "parallel"),
                StringMapEntryAdd("parking:lane:right", "diagonal"),
                StringMapEntryModify("parking:lane:left:parallel", "half_on_kerb", "on_street"),
                StringMapEntryDelete("parking:lane:right:parallel", "on_kerb"),
                StringMapEntryAdd("parking:lane:right:diagonal", "on_street"),
                StringMapEntryDelete("parking:condition:left", "free"),
                StringMapEntryDelete("parking:condition:right", "customers"),
            )
        )
    }

    @Test fun `tag only on one side`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:right", "diagonal"),
                StringMapEntryAdd("parking:lane:right:diagonal", "on_street")
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET),
                null
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "diagonal"),
                StringMapEntryAdd("parking:lane:left:diagonal", "on_street")
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applying incomplete left throws exception`() {
        LeftAndRightStreetParking(IncompleteStreetParking, null).applyTo(StringMapChangesBuilder(mapOf()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applying incomplete right throws exception`() {
        LeftAndRightStreetParking(null, IncompleteStreetParking).applyTo(StringMapChangesBuilder(mapOf()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applying unknown left throws exception`() {
        LeftAndRightStreetParking(UnknownStreetParking, null).applyTo(StringMapChangesBuilder(mapOf()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applying unknown right throws exception`() {
        LeftAndRightStreetParking(null, UnknownStreetParking).applyTo(StringMapChangesBuilder(mapOf()))
    }

    @Test fun validOrNullValues() {
        for (invalidParking in listOf(IncompleteStreetParking, UnknownStreetParking)) {
            assertEquals(
                LeftAndRightStreetParking(null, null),
                LeftAndRightStreetParking(invalidParking, invalidParking).validOrNullValues()
            )
            assertEquals(
                LeftAndRightStreetParking(StreetParkingProhibited, null),
                LeftAndRightStreetParking(StreetParkingProhibited, invalidParking).validOrNullValues()
            )
            assertEquals(
                LeftAndRightStreetParking(null, StreetParkingProhibited),
                LeftAndRightStreetParking(invalidParking, StreetParkingProhibited).validOrNullValues()
            )
        }
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: LeftAndRightStreetParking, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
