package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

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

    @Test fun `apply same tags to side not specified before as already specified side`() {
        verifyAnswer(
            mapOf(
                "parking:lane:right" to "no",
                "parking:condition:right" to "no_stopping",
            ),
            LeftAndRightStreetParking(
                StreetStoppingProhibited,
                StreetStoppingProhibited
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "no"),
                StringMapEntryAdd("parking:condition:both", "no_stopping"),
                StringMapEntryDelete("parking:lane:right", "no"),
                StringMapEntryDelete("parking:condition:right", "no_stopping"),
            )
        )
    }

    @Test fun `updates check date`() {
        verifyAnswer(
            mapOf("parking:lane:both" to "no"),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "no", "no"),
                StringMapEntryAdd("check_date:parking:lane", nowAsCheckDateString())
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
                StringMapEntryAdd("check_date:parking:lane", nowAsCheckDateString())
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

    @Test fun `applying does not remove properties of the side that did not change`() {
        verifyAnswer(
            mapOf(
                "parking:lane:right" to "parallel",
                "parking:lane:right:parallel" to "on_kerb",
                "parking:condition:right" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "diagonal"),
                StringMapEntryAdd("parking:lane:left:diagonal", "on_street"),
                StringMapEntryModify("parking:lane:right", "parallel", "parallel"),
                StringMapEntryModify("parking:lane:right:parallel", "on_kerb", "on_kerb"),
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:right" to "parallel",
                "parking:lane:right:parallel" to "on_kerb",
                "parking:condition:right" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:both", "parallel"),
                StringMapEntryAdd("parking:lane:both:parallel", "on_kerb"),
                StringMapEntryDelete("parking:lane:right", "parallel"),
                StringMapEntryDelete("parking:lane:right:parallel", "on_kerb"),
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:lane:left:parallel" to "on_kerb",
                "parking:lane:right:parallel" to "on_street",
                "parking:condition:right" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "diagonal"),
                StringMapEntryAdd("parking:lane:right", "parallel"),
                StringMapEntryDelete("parking:lane:both", "parallel"),
                StringMapEntryDelete("parking:lane:left:parallel", "on_kerb"),
                StringMapEntryModify("parking:lane:right:parallel", "on_street", "on_street"),
                StringMapEntryAdd("parking:lane:left:diagonal", "on_street"),
            )
        )
    }

    @Test fun `applying removes properties specified for both sides if one side changed`() {
        verifyAnswer(
            mapOf(
                "parking:lane:right" to "parallel",
                "parking:lane:right:parallel" to "on_kerb",
                "parking:condition:both" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingProhibited,
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "no"),
                StringMapEntryAdd("parking:condition:left", "no_parking"),
                StringMapEntryModify("parking:lane:right", "parallel", "parallel"),
                StringMapEntryModify("parking:lane:right:parallel", "on_kerb", "on_kerb"),
                StringMapEntryDelete("parking:condition:both", "customers"),
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:lane:both:parallel" to "on_street",
                "parking:condition:both" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:left:parallel", "on_street"),
                StringMapEntryAdd("parking:lane:right:parallel", "on_kerb"),
                StringMapEntryDelete("parking:lane:both:parallel", "on_street"),
                StringMapEntryDelete("parking:condition:both", "customers"),
            )
        )
    }

    @Test fun `applying removes properties specified for side that changed`() {
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:lane:left:parallel" to "on_street",
                "parking:lane:right:parallel" to "on_kerb",
                "parking:condition:right" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:both:parallel", "on_street"),
                StringMapEntryDelete("parking:lane:left:parallel", "on_street"),
                StringMapEntryDelete("parking:lane:right:parallel", "on_kerb"),
                StringMapEntryDelete("parking:condition:right", "customers")
            )
        )
    }

    @Test fun `supplementing one side does not remove properties specified for that side`() {
        verifyAnswer(
            mapOf(
                "parking:lane:right" to "parallel",
                "parking:condition:right" to "customers",
            ),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:right", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:right:parallel", "on_kerb")
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:condition:left" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB),
                null
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:left", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:left:parallel", "on_kerb")
            )
        )
    }

    @Test fun `supplementing one side does not remove properties specified for both sides`() {
        verifyAnswer(
            mapOf(
                "parking:lane:right" to "parallel",
                "parking:condition:both" to "customers",
            ),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:right", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:right:parallel", "on_kerb")
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:condition:both" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB),
                null
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:left", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:left:parallel", "on_kerb")
            )
        )
    }

    @Test fun `supplementing both sides does not remove properties specified for both sides`() {
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:condition:both" to "customers",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:left:parallel", "on_street"),
                StringMapEntryAdd("parking:lane:right:parallel", "on_kerb"),
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "parallel",
                "parking:condition:left" to "customers",
                "parking:condition:right" to "residents",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_KERB)
            ),
            arrayOf(
                StringMapEntryModify("parking:lane:both", "parallel", "parallel"),
                StringMapEntryAdd("parking:lane:left:parallel", "on_street"),
                StringMapEntryAdd("parking:lane:right:parallel", "on_kerb"),
            )
        )
    }

    @Test fun `applying only on one side`() {
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

    @Test fun `applying only one side does not touch properties specified for the other side`() {
        verifyAnswer(
            mapOf(
                "parking:condition:right" to "unhandled_value"
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET),
                null
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "diagonal"),
                StringMapEntryAdd("parking:lane:left:diagonal", "on_street")
            )
        )
        verifyAnswer(
            mapOf(
                "parking:condition:left" to "unhandled_value"
            ),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:right", "diagonal"),
                StringMapEntryAdd("parking:lane:right:diagonal", "on_street")
            )
        )
    }

    @Test fun `applying only one side removes properties specified for both sides`() {
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "diagonal",
                "parking:condition:both" to "no_parking",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET),
                null
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:left", "diagonal"),
                StringMapEntryAdd("parking:lane:left:diagonal", "on_street"),
                StringMapEntryDelete( "parking:condition:both", "no_parking"),
                StringMapEntryDelete( "parking:lane:both", "diagonal"),
            )
        )
        verifyAnswer(
            mapOf(
                "parking:lane:both" to "diagonal",
                "parking:condition:both" to "no_parking",
            ),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.ON_STREET),
            ),
            arrayOf(
                StringMapEntryAdd("parking:lane:right", "diagonal"),
                StringMapEntryAdd("parking:lane:right:diagonal", "on_street"),
                StringMapEntryDelete( "parking:condition:both", "no_parking"),
                StringMapEntryDelete( "parking:lane:both", "diagonal"),
            )
        )
    }

    @Test fun `applying incomplete throws exception`() {
        for (incompleteParking in listOf(
            IncompleteStreetParking,
            StreetParkingPositionAndOrientation(null, null),
            StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, null),
            StreetParkingPositionAndOrientation(null, ParkingPosition.ON_STREET),
        )) {
            assertFails {
                LeftAndRightStreetParking(incompleteParking, null).applyTo(StringMapChangesBuilder(mapOf()))
            }
            assertFails {
                LeftAndRightStreetParking(null, incompleteParking).applyTo(StringMapChangesBuilder(mapOf()))
            }
        }
    }

    @Test fun `applying unknown throws exception`() {
        for (unknownParking in listOf(
            UnknownStreetParking,
            StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.UNKNOWN_POSITION),
            StreetParkingPositionAndOrientation(ParkingOrientation.UNKNOWN_ORIENTATION, ParkingPosition.ON_STREET),
        )) {
            assertFails {
                LeftAndRightStreetParking(unknownParking, null).applyTo(StringMapChangesBuilder(mapOf()))
            }
            assertFails {
                LeftAndRightStreetParking(null, unknownParking).applyTo(StringMapChangesBuilder(mapOf()))
            }
        }
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

    @Test fun isValid() {
        assertFalse(IncompleteStreetParking.isValid)
        assertFalse(UnknownStreetParking.isValid)
        assertFalse(StreetParkingPositionAndOrientation(null, null).isValid)
        assertFalse(StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, null).isValid)
        assertFalse(StreetParkingPositionAndOrientation(null, ParkingPosition.ON_KERB).isValid)
        assertFalse(StreetParkingPositionAndOrientation(
            ParkingOrientation.UNKNOWN_ORIENTATION,
            ParkingPosition.ON_KERB
        ).isValid)
        assertFalse(StreetParkingPositionAndOrientation(
            ParkingOrientation.DIAGONAL,
            ParkingPosition.UNKNOWN_POSITION
        ).isValid)

        assertTrue(NoStreetParking.isValid)
        assertTrue(StreetParkingPositionAndOrientation(
            ParkingOrientation.PARALLEL,
            ParkingPosition.ON_KERB
        ).isValid)
    }
}

private fun assertFails(block: () -> Unit) {
    try { block() } catch (e: Exception) { return }
    fail()
}

private fun verifyAnswer(tags: Map<String, String>, answer: LeftAndRightStreetParking, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
