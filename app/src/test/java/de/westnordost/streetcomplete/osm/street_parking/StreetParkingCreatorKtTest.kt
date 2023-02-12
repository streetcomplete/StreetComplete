package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*
import org.assertj.core.api.Assertions
import org.junit.Test

class StreetParkingCreatorKtTest {

    @Test fun `apply nothing applies nothing`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(null, null),
            arrayOf()
        )
    }

    @Test fun `apply no parking`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(StringMapEntryAdd("parking:both", "no"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(NoStreetParking, null),
            arrayOf(StringMapEntryAdd("parking:left", "no"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(null, NoStreetParking),
            arrayOf(StringMapEntryAdd("parking:right", "no"))
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
                verifyAnswer(
                    mapOf(),
                    LeftAndRightStreetParking(parking, parking),
                    arrayOf(
                        StringMapEntryAdd("parking:both", positionStr),
                        StringMapEntryAdd("parking:both:orientation", orientationStr)
                    )
                )

                verifyAnswer(
                    mapOf(),
                    LeftAndRightStreetParking(parking, null),
                    arrayOf(
                        StringMapEntryAdd("parking:left", positionStr),
                        StringMapEntryAdd("parking:left:orientation", orientationStr)
                    )
                )

                verifyAnswer(
                    mapOf(),
                    LeftAndRightStreetParking(null, parking),
                    arrayOf(
                        StringMapEntryAdd("parking:right", positionStr),
                        StringMapEntryAdd("parking:right:orientation", orientationStr)
                    )
                )

                // complement tags
                verifyAnswer(
                    mapOf(
                        "parking:right" to positionStr,
                        "parking:right:orientation" to orientationStr
                    ),
                    LeftAndRightStreetParking(parking, null),
                    arrayOf(
                        StringMapEntryAdd("parking:both", positionStr),
                        StringMapEntryAdd("parking:both:orientation", orientationStr),
                        StringMapEntryDelete("parking:right", positionStr),
                        StringMapEntryDelete("parking:right:orientation", orientationStr)
                    )
                )

                verifyAnswer(
                    mapOf(
                        "parking:left" to positionStr,
                        "parking:left:orientation" to orientationStr
                    ),
                    LeftAndRightStreetParking(null, parking),
                    arrayOf(
                        StringMapEntryAdd("parking:both", positionStr),
                        StringMapEntryAdd("parking:both:orientation", orientationStr),
                        StringMapEntryDelete("parking:left", positionStr),
                        StringMapEntryDelete("parking:left:orientation", orientationStr)
                    )
                )
            }
        }
    }

    @Test fun `apply separate parking`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate),
            arrayOf(StringMapEntryAdd("parking:both", "separate"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(StreetParkingSeparate, null),
            arrayOf(StringMapEntryAdd("parking:left", "separate"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(null, StreetParkingSeparate),
            arrayOf(StringMapEntryAdd("parking:right", "separate"))
        )
    }

    @Test fun `apply painted area only`() {
        val parking = StreetParkingPositionAndOrientation(PARALLEL, PAINTED_AREA_ONLY)
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(parking, parking),
            arrayOf(
                StringMapEntryAdd("parking:both", "lane"),
                StringMapEntryAdd("parking:both:orientation", "parallel"),
                StringMapEntryAdd("parking:both:markings", "yes"),
                StringMapEntryAdd("parking:both:staggered", "yes"),
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(parking, null),
            arrayOf(
                StringMapEntryAdd("parking:left", "lane"),
                StringMapEntryAdd("parking:left:orientation", "parallel"),
                StringMapEntryAdd("parking:left:markings", "yes"),
                StringMapEntryAdd("parking:left:staggered", "yes"),
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(null, parking),
            arrayOf(
                StringMapEntryAdd("parking:right", "lane"),
                StringMapEntryAdd("parking:right:orientation", "parallel"),
                StringMapEntryAdd("parking:right:markings", "yes"),
                StringMapEntryAdd("parking:right:staggered", "yes"),
            )
        )
    }

    @Test fun `apply on_kerb parking does not replace shoulder parking`() {
        val parking = StreetParkingPositionAndOrientation(PARALLEL, OFF_STREET)

        verifyAnswer(
            mapOf("parking:both" to "shoulder"),
            LeftAndRightStreetParking(parking, parking),
            arrayOf(
                StringMapEntryModify("parking:both", "shoulder", "shoulder"),
                StringMapEntryAdd("parking:both:orientation", "parallel"),
            )
        )
        verifyAnswer(
            mapOf("parking:left" to "shoulder"),
            LeftAndRightStreetParking(parking, null),
            arrayOf(
                StringMapEntryModify("parking:left", "shoulder", "shoulder"),
                StringMapEntryAdd("parking:left:orientation", "parallel"),
            )
        )
        verifyAnswer(
            mapOf("parking:right" to "shoulder"),
            LeftAndRightStreetParking(null, parking),
            arrayOf(
                StringMapEntryModify("parking:right", "shoulder", "shoulder"),
                StringMapEntryAdd("parking:right:orientation", "parallel"),
            )
        )
    }

    @Test fun `apply staggered parking on road`() {

        val positions = listOf(
            "lane" to STAGGERED_ON_STREET,
            "half_on_kerb" to STAGGERED_HALF_ON_STREET
        )
        for ((positionStr, positionValue) in positions) {
            val parking = StreetParkingPositionAndOrientation(PARALLEL, positionValue)

            verifyAnswer(
                mapOf(),
                LeftAndRightStreetParking(parking, parking),
                arrayOf(
                    StringMapEntryAdd("parking:both", positionStr),
                    StringMapEntryAdd("parking:both:orientation", "parallel"),
                    StringMapEntryAdd("parking:both:staggered", "yes"),
                )
            )
            verifyAnswer(
                mapOf(),
                LeftAndRightStreetParking(parking, null),
                arrayOf(
                    StringMapEntryAdd("parking:left", positionStr),
                    StringMapEntryAdd("parking:left:orientation", "parallel"),
                    StringMapEntryAdd("parking:left:staggered", "yes"),
                )
            )
            verifyAnswer(
                mapOf(),
                LeftAndRightStreetParking(null, parking),
                arrayOf(
                    StringMapEntryAdd("parking:right", positionStr),
                    StringMapEntryAdd("parking:right:orientation", "parallel"),
                    StringMapEntryAdd("parking:right:staggered", "yes"),
                )
            )
        }
    }

    @Test fun `apply parallel parking answer on both sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET),
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:both", "half_on_kerb"),
                StringMapEntryAdd("parking:both:orientation", "parallel"),
            )
        )
    }

    @Test fun `apply parallel parking answer with different positions on sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, ON_STREET),
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:both:orientation", "parallel"),
                StringMapEntryAdd("parking:left", "lane"),
                StringMapEntryAdd("parking:right", "half_on_kerb"),
            )
        )
    }

    @Test fun `apply street side parking answer with different orientations on sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PARALLEL, STREET_SIDE)
            ),
            arrayOf(
                StringMapEntryAdd("parking:both", "street_side"),
                StringMapEntryAdd("parking:left:orientation", "perpendicular"),
                StringMapEntryAdd("parking:right:orientation", "parallel"),
            )
        )
    }

    @Test fun `apply different parking positions and orientations on sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(DIAGONAL, STREET_SIDE),
                StreetParkingPositionAndOrientation(PERPENDICULAR, OFF_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:left", "street_side"),
                StringMapEntryAdd("parking:left:orientation", "diagonal"),
                StringMapEntryAdd("parking:right", "on_kerb"),
                StringMapEntryAdd("parking:right:orientation", "perpendicular"),
            )
        )
    }

    @Test fun `applying one side combines with previous tagging of the other side to both-tag`() {
        verifyAnswer(
            mapOf("parking:left" to "separate"),
            LeftAndRightStreetParking(null, StreetParkingSeparate),
            arrayOf(
                StringMapEntryAdd("parking:both", "separate"),
                StringMapEntryDelete("parking:left", "separate"),
            )
        )
        verifyAnswer(
            mapOf("parking:right" to "separate"),
            LeftAndRightStreetParking(StreetParkingSeparate, null),
            arrayOf(
                StringMapEntryAdd("parking:both", "separate"),
                StringMapEntryDelete("parking:right", "separate"),
            )
        )
    }

    @Test fun `apply for one side does not touch the other side`() {
        verifyAnswer(
            mapOf("parking:left" to "separate"),
            LeftAndRightStreetParking(null, NoStreetParking),
            arrayOf(
                StringMapEntryAdd("parking:right", "no")
            )
        )
        verifyAnswer(
            mapOf("parking:right" to "no"),
            LeftAndRightStreetParking(StreetParkingSeparate, null),
            arrayOf(
                StringMapEntryAdd("parking:left", "separate")
            )
        )
    }

    @Test fun `apply for one side does not touch the other side even if it is invalid`() {
        verifyAnswer(
            mapOf("parking:left" to "narrow"),
            LeftAndRightStreetParking(null, NoStreetParking),
            arrayOf(
                StringMapEntryAdd("parking:right", "no")
            )
        )
        verifyAnswer(
            mapOf("parking:right" to "narrow"),
            LeftAndRightStreetParking(StreetParkingSeparate, null),
            arrayOf(
                StringMapEntryAdd("parking:left", "separate")
            )
        )
    }

    @Test fun `apply for one side does not change values for the other side even if it was defined for both sides before and invalid`() {
        verifyAnswer(
            mapOf(
                "parking:both" to "on_kerb",
                "parking:both:orientation" to "hexagonal",
            ),
            LeftAndRightStreetParking(null, NoStreetParking),
            arrayOf(
                StringMapEntryAdd("parking:right", "no"),
                StringMapEntryDelete("parking:both", "on_kerb"),
                StringMapEntryDelete("parking:both:orientation", "hexagonal"),
                StringMapEntryAdd("parking:left", "on_kerb"),
                StringMapEntryAdd("parking:left:orientation", "hexagonal"),
            )
        )
    }

    @Test fun `updates check date`() {
        verifyAnswer(
            mapOf("parking:both" to "no"),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(
                StringMapEntryModify("parking:both", "no", "no"),
                StringMapEntryAdd("check_date:parking", nowAsCheckDateString())
            )
        )
        verifyAnswer(
            mapOf(
                "parking:both:orientation" to "parallel",
                "parking:left" to "half_on_kerb",
                "parking:right" to "on_kerb",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET),
                StreetParkingPositionAndOrientation(PARALLEL, OFF_STREET)
            ),
            arrayOf(
                StringMapEntryModify("parking:both:orientation", "parallel", "parallel"),
                StringMapEntryModify("parking:left", "half_on_kerb", "half_on_kerb"),
                StringMapEntryModify("parking:right", "on_kerb", "on_kerb"),
                StringMapEntryAdd("check_date:parking", nowAsCheckDateString())
            )
        )
    }

    @Test fun `clean up previous tagging when applying value for each side`() {
        verifyAnswer(
            mapOf(
                "parking:both:orientation" to "parallel",
                "parking:left" to "half_on_kerb",
                "parking:right" to "on_kerb",
            ),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, ON_STREET),
                StreetParkingPositionAndOrientation(DIAGONAL, ON_STREET)
            ),
            arrayOf(
                StringMapEntryDelete("parking:both:orientation", "parallel"),
                StringMapEntryAdd("parking:left:orientation", "parallel"),
                StringMapEntryAdd("parking:right:orientation", "diagonal"),
                StringMapEntryDelete("parking:left", "half_on_kerb"),
                StringMapEntryDelete("parking:right", "on_kerb"),
                StringMapEntryAdd("parking:both", "lane"),
            )
        )
    }

    @Test fun `clean up parking orientation if new parking does not have orientation`() {
        verifyAnswer(
            mapOf(
                "parking:both:orientation" to "parallel",
                "parking:both" to "half_on_kerb"
            ),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(
                StringMapEntryDelete("parking:both:orientation", "parallel"),
                StringMapEntryModify("parking:both", "half_on_kerb", "no"),
            )
        )
    }

    @Test fun `clean up markings tagging if new parking does not have markings`() {
        verifyAnswer(
            mapOf(
                "parking:both:orientation" to "parallel",
                "parking:both" to "half_on_kerb",
                "parking:both:markings" to "yes"
            ),
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            arrayOf(
                StringMapEntryDelete("parking:both:orientation", "parallel"),
                StringMapEntryDelete("parking:both:markings", "yes"),
                StringMapEntryModify("parking:both", "half_on_kerb", "no"),
            )
        )
    }

    @Test fun `clean up staggered tagging if new parking is not staggered`() {
        val parking = StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
        verifyAnswer(
            mapOf(
                "parking:both:orientation" to "parallel",
                "parking:both" to "half_on_kerb",
                "parking:both:staggered" to "yes"
            ),
            LeftAndRightStreetParking(parking, parking),
            arrayOf(
                StringMapEntryModify("parking:both:orientation", "parallel", "parallel"),
                StringMapEntryModify("parking:both", "half_on_kerb", "half_on_kerb"),

                StringMapEntryDelete("parking:both:staggered", "yes"),
            )
        )
    }

    @Test fun `clean up any old schema tagging when changing any tags`() {
        verifyAnswer(
            mapOf(
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
            ),
            LeftAndRightStreetParking(null, NoStreetParking),
            arrayOf(
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
            )
        )
    }

    @Test fun `tag only on one side`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                null,
                StreetParkingPositionAndOrientation(DIAGONAL, ON_STREET)
            ),
            arrayOf(
                StringMapEntryAdd("parking:right", "lane"),
                StringMapEntryAdd("parking:right:orientation", "diagonal")
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(DIAGONAL, ON_STREET),
                null
            ),
            arrayOf(
                StringMapEntryAdd("parking:left", "lane"),
                StringMapEntryAdd("parking:left:orientation", "diagonal")
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
}

private fun verifyAnswer(tags: Map<String, String>, answer: LeftAndRightStreetParking, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
