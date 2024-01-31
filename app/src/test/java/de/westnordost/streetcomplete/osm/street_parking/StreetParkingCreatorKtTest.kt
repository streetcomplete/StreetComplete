package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StreetParkingCreatorKtTest {

    @Test fun `apply nothing applies nothing`() {
        assertEquals(
            setOf(),
            LeftAndRightStreetParking(null, null).appliedTo(mapOf())
        )
    }

    @Test fun `apply no parking`() {
        assertEquals(
            setOf(StringMapEntryAdd("parking:both", "no")),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("parking:left", "no")),
            LeftAndRightStreetParking(NoStreetParking, null).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("parking:right", "no")),
            LeftAndRightStreetParking(null, NoStreetParking).appliedTo(mapOf())
        )
    }

    @Test fun `apply parking`() {
        val orientations = listOf(
            "parallel" to PARALLEL,
            "diagonal" to DIAGONAL,
            "perpendicular" to PERPENDICULAR
        )

        val positions = listOf(
            "lane" to ON_STREET,
            "half_on_kerb" to HALF_ON_STREET,
            "on_kerb" to OFF_STREET,
            "street_side" to STREET_SIDE
        )

        for ((orientationStr, orientationValue) in orientations) {
            for ((positionStr, positionValue) in positions) {
                val parking = StreetParkingPositionAndOrientation(orientationValue, positionValue)

                // tag new
                assertEquals(
                    setOf(
                        StringMapEntryAdd("parking:both", positionStr),
                        StringMapEntryAdd("parking:both:orientation", orientationStr)
                    ),
                    LeftAndRightStreetParking(parking, parking).appliedTo(mapOf())
                )

                assertEquals(
                    setOf(
                        StringMapEntryAdd("parking:left", positionStr),
                        StringMapEntryAdd("parking:left:orientation", orientationStr)
                    ),
                    LeftAndRightStreetParking(parking, null).appliedTo(mapOf())
                )

                assertEquals(
                    setOf(
                        StringMapEntryAdd("parking:right", positionStr),
                        StringMapEntryAdd("parking:right:orientation", orientationStr)
                    ),
                    LeftAndRightStreetParking(null, parking).appliedTo(mapOf())
                )

                // complement tags
                assertEquals(
                    setOf(
                        StringMapEntryAdd("parking:both", positionStr),
                        StringMapEntryAdd("parking:both:orientation", orientationStr),
                        StringMapEntryDelete("parking:right", positionStr),
                        StringMapEntryDelete("parking:right:orientation", orientationStr)
                    ),
                    LeftAndRightStreetParking(parking, null).appliedTo(mapOf(
                        "parking:right" to positionStr,
                        "parking:right:orientation" to orientationStr
                    ))
                )

                assertEquals(
                    setOf(
                        StringMapEntryAdd("parking:both", positionStr),
                        StringMapEntryAdd("parking:both:orientation", orientationStr),
                        StringMapEntryDelete("parking:left", positionStr),
                        StringMapEntryDelete("parking:left:orientation", orientationStr)
                    ),
                    LeftAndRightStreetParking(null, parking).appliedTo(mapOf(
                        "parking:left" to positionStr,
                        "parking:left:orientation" to orientationStr
                    ))
                )
            }
        }
    }

    @Test fun `apply separate parking`() {
        assertEquals(
            setOf(StringMapEntryAdd("parking:both", "separate")),
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("parking:left", "separate")),
            LeftAndRightStreetParking(StreetParkingSeparate, null).appliedTo(mapOf()),
        )
        assertEquals(
            setOf(StringMapEntryAdd("parking:right", "separate")),
            LeftAndRightStreetParking(null, StreetParkingSeparate).appliedTo(mapOf()),
        )
    }

    @Test fun `apply painted area only`() {
        val parking = StreetParkingPositionAndOrientation(PARALLEL, PAINTED_AREA_ONLY)
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:both", "lane"),
                StringMapEntryAdd("parking:both:orientation", "parallel"),
                StringMapEntryAdd("parking:both:markings", "yes"),
                StringMapEntryAdd("parking:both:staggered", "yes"),
            ),
            LeftAndRightStreetParking(parking, parking).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:left", "lane"),
                StringMapEntryAdd("parking:left:orientation", "parallel"),
                StringMapEntryAdd("parking:left:markings", "yes"),
                StringMapEntryAdd("parking:left:staggered", "yes"),
            ),
            LeftAndRightStreetParking(parking, null).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:right", "lane"),
                StringMapEntryAdd("parking:right:orientation", "parallel"),
                StringMapEntryAdd("parking:right:markings", "yes"),
                StringMapEntryAdd("parking:right:staggered", "yes"),
            ),
            LeftAndRightStreetParking(null, parking).appliedTo(mapOf())
        )
    }

    @Test fun `apply on_kerb parking does not replace shoulder parking`() {
        val parking = StreetParkingPositionAndOrientation(PARALLEL, OFF_STREET)

        assertEquals(
            setOf(
                StringMapEntryModify("parking:both", "shoulder", "shoulder"),
                StringMapEntryAdd("parking:both:orientation", "parallel"),
            ),
            LeftAndRightStreetParking(parking, parking).appliedTo(mapOf(
                "parking:both" to "shoulder"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("parking:left", "shoulder", "shoulder"),
                StringMapEntryAdd("parking:left:orientation", "parallel"),
            ),
            LeftAndRightStreetParking(parking, null).appliedTo(mapOf(
                "parking:left" to "shoulder"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("parking:right", "shoulder", "shoulder"),
                StringMapEntryAdd("parking:right:orientation", "parallel"),
            ),
            LeftAndRightStreetParking(null, parking).appliedTo(mapOf(
                "parking:right" to "shoulder"
            ))
        )
    }

    @Test fun `apply staggered parking on road`() {
        val positions = listOf(
            "lane" to STAGGERED_ON_STREET,
            "half_on_kerb" to STAGGERED_HALF_ON_STREET
        )
        for ((positionStr, positionValue) in positions) {
            val parking = StreetParkingPositionAndOrientation(PARALLEL, positionValue)

            assertEquals(
                setOf(
                    StringMapEntryAdd("parking:both", positionStr),
                    StringMapEntryAdd("parking:both:orientation", "parallel"),
                    StringMapEntryAdd("parking:both:staggered", "yes"),
                ),
                LeftAndRightStreetParking(parking, parking).appliedTo(mapOf())
            )
            assertEquals(
                setOf(
                    StringMapEntryAdd("parking:left", positionStr),
                    StringMapEntryAdd("parking:left:orientation", "parallel"),
                    StringMapEntryAdd("parking:left:staggered", "yes"),
                ),
                LeftAndRightStreetParking(parking, null).appliedTo(mapOf())
            )
            assertEquals(
                setOf(
                    StringMapEntryAdd("parking:right", positionStr),
                    StringMapEntryAdd("parking:right:orientation", "parallel"),
                    StringMapEntryAdd("parking:right:staggered", "yes"),
                ),
                LeftAndRightStreetParking(null, parking).appliedTo(mapOf())
            )
        }
    }

    @Test fun `apply parallel parking answer on both sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:both", "half_on_kerb"),
                StringMapEntryAdd("parking:both:orientation", "parallel"),
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET),
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
            ).appliedTo(mapOf())
        )
    }

    @Test fun `apply parallel parking answer with different positions on sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:both:orientation", "parallel"),
                StringMapEntryAdd("parking:left", "lane"),
                StringMapEntryAdd("parking:right", "half_on_kerb"),
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, ON_STREET),
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
            ).appliedTo(mapOf())
        )
    }

    @Test fun `apply street side parking answer with different orientations on sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:both", "street_side"),
                StringMapEntryAdd("parking:left:orientation", "perpendicular"),
                StringMapEntryAdd("parking:right:orientation", "parallel"),
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PARALLEL, STREET_SIDE)
            ).appliedTo(mapOf())
        )
    }

    @Test fun `apply different parking positions and orientations on sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:left", "street_side"),
                StringMapEntryAdd("parking:left:orientation", "diagonal"),
                StringMapEntryAdd("parking:right", "on_kerb"),
                StringMapEntryAdd("parking:right:orientation", "perpendicular"),
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(DIAGONAL, STREET_SIDE),
                StreetParkingPositionAndOrientation(PERPENDICULAR, OFF_STREET)
            ).appliedTo(mapOf())
        )
    }

    @Test fun `applying one side combines with previous tagging of the other side to both-tag`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:both", "separate"),
                StringMapEntryDelete("parking:left", "separate"),
            ),
            LeftAndRightStreetParking(null, StreetParkingSeparate).appliedTo(mapOf(
                "parking:left" to "separate"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:both", "separate"),
                StringMapEntryDelete("parking:right", "separate"),
            ),
            LeftAndRightStreetParking(StreetParkingSeparate, null).appliedTo(mapOf(
                "parking:right" to "separate"
            ))
        )
    }

    @Test fun `apply for one side does not touch the other side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:right", "no")
            ),
            LeftAndRightStreetParking(null, NoStreetParking).appliedTo(mapOf(
                "parking:left" to "separate"
            )),
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:left", "separate")
            ),
            LeftAndRightStreetParking(StreetParkingSeparate, null).appliedTo(mapOf(
                "parking:right" to "no"
            ))
        )
    }

    @Test fun `apply for one side does not touch the other side even if it is invalid`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:right", "no")
            ),
            LeftAndRightStreetParking(null, NoStreetParking).appliedTo(mapOf(
                "parking:left" to "narrow"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:left", "separate")
            ),
            LeftAndRightStreetParking(StreetParkingSeparate, null).appliedTo(
                mapOf("parking:right" to "narrow")
            )
        )
    }

    @Test fun `apply for one side does not change values for the other side even if it was defined for both sides before and invalid`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:right", "no"),
                StringMapEntryDelete("parking:both", "on_kerb"),
                StringMapEntryDelete("parking:both:orientation", "hexagonal"),
                StringMapEntryAdd("parking:left", "on_kerb"),
                StringMapEntryAdd("parking:left:orientation", "hexagonal"),
            ),
            LeftAndRightStreetParking(null, NoStreetParking).appliedTo(mapOf(
                "parking:both" to "on_kerb",
                "parking:both:orientation" to "hexagonal",
            ))
        )
    }

    @Test fun `updates check date`() {
        assertEquals(
            setOf(
                StringMapEntryModify("parking:both", "no", "no"),
                StringMapEntryAdd("check_date:parking", nowAsCheckDateString())
            ),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking).appliedTo(mapOf(
                "parking:both" to "no"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("parking:both:orientation", "parallel", "parallel"),
                StringMapEntryModify("parking:left", "half_on_kerb", "half_on_kerb"),
                StringMapEntryModify("parking:right", "on_kerb", "on_kerb"),
                StringMapEntryAdd("check_date:parking", nowAsCheckDateString())
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET),
                StreetParkingPositionAndOrientation(PARALLEL, OFF_STREET)
            ).appliedTo(mapOf(
                "parking:both:orientation" to "parallel",
                "parking:left" to "half_on_kerb",
                "parking:right" to "on_kerb",
            ))
        )
    }

    @Test fun `clean up previous tagging when applying value for each side`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("parking:both:orientation", "parallel"),
                StringMapEntryAdd("parking:left:orientation", "parallel"),
                StringMapEntryAdd("parking:right:orientation", "diagonal"),
                StringMapEntryDelete("parking:left", "half_on_kerb"),
                StringMapEntryDelete("parking:right", "on_kerb"),
                StringMapEntryAdd("parking:both", "lane"),
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, ON_STREET),
                StreetParkingPositionAndOrientation(DIAGONAL, ON_STREET)
            ).appliedTo(mapOf(
                "parking:both:orientation" to "parallel",
                "parking:left" to "half_on_kerb",
                "parking:right" to "on_kerb",
            ))
        )
    }

    @Test fun `clean up parking orientation if new parking does not have orientation`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("parking:both:orientation", "parallel"),
                StringMapEntryModify("parking:both", "half_on_kerb", "no"),
            ),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking).appliedTo(mapOf(
                "parking:both:orientation" to "parallel",
                "parking:both" to "half_on_kerb"
            ))
        )
    }

    @Test fun `clean up markings tagging if new parking does not have markings`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("parking:both:orientation", "parallel"),
                StringMapEntryDelete("parking:both:markings", "yes"),
                StringMapEntryModify("parking:both", "half_on_kerb", "no"),
            ),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking).appliedTo(mapOf(
                "parking:both:orientation" to "parallel",
                "parking:both" to "half_on_kerb",
                "parking:both:markings" to "yes"
            ))
        )
    }

    @Test fun `clean up staggered tagging if new parking is not staggered`() {
        val parking = StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
        assertEquals(
            setOf(
                StringMapEntryModify("parking:both:orientation", "parallel", "parallel"),
                StringMapEntryModify("parking:both", "half_on_kerb", "half_on_kerb"),
                StringMapEntryDelete("parking:both:staggered", "yes"),
            ),
            LeftAndRightStreetParking(parking, parking).appliedTo(mapOf(
                "parking:both:orientation" to "parallel",
                "parking:both" to "half_on_kerb",
                "parking:both:staggered" to "yes"
            ))
        )
    }

    @Test fun `clean up any old schema tagging when changing any tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:right", "no"),
                StringMapEntryDelete("parking:lane", "parallel"),
                StringMapEntryDelete("parking:lane:both", "perpendicular"),
                StringMapEntryDelete("parking:lane:left", "diagonal"),
                StringMapEntryDelete("parking:lane:right", "hexagonal"),
                StringMapEntryDelete("parking:lane:parallel", "1"),
                StringMapEntryDelete("parking:lane:both:parallel", "2"),
                StringMapEntryDelete("parking:lane:left:parallel", "3"),
                StringMapEntryDelete("parking:lane:right:parallel", "4"),
                StringMapEntryDelete("parking:lane:diagonal", "5"),
                StringMapEntryDelete("parking:lane:both:diagonal", "6"),
                StringMapEntryDelete("parking:lane:left:diagonal", "7"),
                StringMapEntryDelete("parking:lane:right:diagonal", "8"),
                StringMapEntryDelete("parking:lane:perpendicular", "9"),
                StringMapEntryDelete("parking:lane:both:perpendicular", "10"),
                StringMapEntryDelete("parking:lane:left:perpendicular", "11"),
                StringMapEntryDelete("parking:lane:right:perpendicular", "12"),
                StringMapEntryDelete("check_date:parking:lane", "123"),
                StringMapEntryDelete("parking:lane:check_date", "123"),
                StringMapEntryDelete("lastcheck:parking:lane", "123"),
                StringMapEntryDelete("parking:lane:lastcheck", "123"),
                StringMapEntryDelete("last_checked:parking:lane", "123"),
                StringMapEntryDelete("parking:lane:last_checked", "123"),
            ),
            LeftAndRightStreetParking(null, NoStreetParking).appliedTo(mapOf(
                "parking:lane" to "parallel",
                "parking:lane:both" to "perpendicular",
                "parking:lane:left" to "diagonal",
                "parking:lane:right" to "hexagonal",
                "parking:lane:parallel" to "1",
                "parking:lane:both:parallel" to "2",
                "parking:lane:left:parallel" to "3",
                "parking:lane:right:parallel" to "4",
                "parking:lane:diagonal" to "5",
                "parking:lane:both:diagonal" to "6",
                "parking:lane:left:diagonal" to "7",
                "parking:lane:right:diagonal" to "8",
                "parking:lane:perpendicular" to "9",
                "parking:lane:both:perpendicular" to "10",
                "parking:lane:left:perpendicular" to "11",
                "parking:lane:right:perpendicular" to "12",
                "check_date:parking:lane" to "123",
                "parking:lane:check_date" to "123",
                "lastcheck:parking:lane" to "123",
                "parking:lane:lastcheck" to "123",
                "last_checked:parking:lane" to "123",
                "parking:lane:last_checked" to "123",
            ))
        )
    }

    @Test fun `tag only on one side`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:right", "lane"),
                StringMapEntryAdd("parking:right:orientation", "diagonal")
            ),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(DIAGONAL, ON_STREET)
            ).appliedTo(mapOf()),

        )
        assertEquals(
            setOf(
                StringMapEntryAdd("parking:left", "lane"),
                StringMapEntryAdd("parking:left:orientation", "diagonal")
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(DIAGONAL, ON_STREET),
                null
            ).appliedTo(mapOf()),
        )
    }

    @Test
    fun `applying incomplete left throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            LeftAndRightStreetParking(IncompleteStreetParking, null)
                .applyTo(StringMapChangesBuilder(mapOf()))
        }
    }

    @Test
    fun `applying incomplete right throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            LeftAndRightStreetParking(null, IncompleteStreetParking)
                .applyTo(StringMapChangesBuilder(mapOf()))
        }
    }

    @Test
    fun `applying unknown left throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            LeftAndRightStreetParking(UnknownStreetParking, null)
                .applyTo(StringMapChangesBuilder(mapOf()))
        }
    }

    @Test
    fun `applying unknown right throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            LeftAndRightStreetParking(null, UnknownStreetParking)
                .applyTo(StringMapChangesBuilder(mapOf()))
        }
    }
}

private fun LeftAndRightStreetParking.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
