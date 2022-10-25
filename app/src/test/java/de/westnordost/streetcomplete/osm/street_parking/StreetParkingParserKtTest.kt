package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.UNKNOWN_ORIENTATION
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.UNKNOWN_POSITION
import org.junit.Assert.assertEquals
import org.junit.Test

class StreetParkingParserKtTest {

    @Test fun parking() {

        val orientations = listOf(
            "parallel" to PARALLEL,
            "diagonal" to DIAGONAL,
            "perpendicular" to PERPENDICULAR
        )

        val positions = listOf(
            "on_street" to ON_STREET,
            "half_on_kerb" to HALF_ON_KERB,
            "on_kerb" to ON_KERB,
            "street_side" to STREET_SIDE,
            "lay_by" to STREET_SIDE,
            "bays" to STREET_SIDE,
            "painted_area_only" to PAINTED_AREA_ONLY,
            "marked" to PAINTED_AREA_ONLY,
        )

        for ((orientationStr, orientationValue) in orientations) {

            for ((positionStr, positionValue) in positions) {

                val parking = StreetParkingPositionAndOrientation(orientationValue, positionValue)

                assertEquals(
                    LeftAndRightStreetParking(parking, null),
                    createStreetParkingSides(mapOf(
                        "parking:lane:left" to orientationStr,
                        "parking:lane:left:$orientationStr" to positionStr
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(null, parking),
                    createStreetParkingSides(mapOf(
                        "parking:lane:right" to orientationStr,
                        "parking:lane:right:$orientationStr" to positionStr
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:lane:both" to orientationStr,
                        "parking:lane:both:$orientationStr" to positionStr
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:lane:both" to orientationStr,
                        "parking:lane:left:$orientationStr" to positionStr,
                        "parking:lane:right:$orientationStr" to positionStr,
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:lane:left" to orientationStr,
                        "parking:lane:right" to orientationStr,
                        "parking:lane:both:$orientationStr" to positionStr
                    ))
                )

                // without explicit sides

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:lane" to orientationStr,
                        "parking:lane:$orientationStr" to positionStr
                    ))
                )
            }
        }
    }

    @Test fun `parking where position differs per side`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PERPENDICULAR, HALF_ON_KERB)
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "perpendicular",
                "parking:lane:left:perpendicular" to "street_side",
                "parking:lane:right:perpendicular" to "half_on_kerb",
            ))
        )
    }

    @Test fun `parking where orientation differs per side`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_KERB)
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "perpendicular",
                "parking:lane:right" to "parallel",
                "parking:lane:left:perpendicular" to "street_side",
                "parking:lane:right:parallel" to "half_on_kerb",
            ))
        )
    }

    @Test fun `no parking`() {

        val conditions = listOf(
            "no" to NoStreetParking,
            "no_stopping" to StreetStoppingProhibited,
            "no_standing" to StreetStandingProhibited,
            "no_parking" to StreetParkingProhibited
        )

        for ((conditionStr, conditionValue) in conditions) {

            // old style tagging

            assertEquals(
                LeftAndRightStreetParking(conditionValue, null),
                createStreetParkingSides(mapOf("parking:lane:left" to conditionStr))
            )

            assertEquals(
                LeftAndRightStreetParking(null, conditionValue),
                createStreetParkingSides(mapOf("parking:lane:right" to conditionStr))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf("parking:lane:both" to conditionStr))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:lane:left" to conditionStr,
                    "parking:lane:right" to conditionStr,
                ))
            )

            // old style tagging without explicit sides

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf("parking:lane" to conditionStr))
            )

            // new style tagging (condition only)

            assertEquals(
                LeftAndRightStreetParking(conditionValue, null),
                createStreetParkingSides(mapOf("parking:condition:left" to conditionStr))
            )

            assertEquals(
                LeftAndRightStreetParking(null, conditionValue),
                createStreetParkingSides(mapOf("parking:condition:right" to conditionStr))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf("parking:condition:both" to conditionStr))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:condition:left" to conditionStr,
                    "parking:condition:right" to conditionStr,
                ))
            )

            // new style tagging (condition only) without explicit sides

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf("parking:condition" to conditionStr))
            )

            // new style tagging (full)

            assertEquals(
                LeftAndRightStreetParking(conditionValue, null),
                createStreetParkingSides(mapOf(
                    "parking:lane:left" to "no",
                    "parking:condition:left" to conditionStr
                ))
            )

            assertEquals(
                LeftAndRightStreetParking(null, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:lane:right" to "no",
                    "parking:condition:right" to conditionStr
                ))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:lane:both" to "no",
                    "parking:condition:both" to conditionStr
                ))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:lane:both" to "no",
                    "parking:condition:left" to conditionStr,
                    "parking:condition:right" to conditionStr,
                ))
            )

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:lane:left" to "no",
                    "parking:lane:right" to "no",
                    "parking:condition:both" to conditionStr,
                ))
            )

            // new style tagging (full) without explicit sides

            assertEquals(
                LeftAndRightStreetParking(conditionValue, conditionValue),
                createStreetParkingSides(mapOf(
                    "parking:lane" to "no",
                    "parking:condition" to conditionStr
                ))
            )
        }
    }

    @Test fun `no parking where condition differs per side`() {
        assertEquals(
            LeftAndRightStreetParking(StreetStandingProhibited, StreetParkingProhibited),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "no_standing",
                "parking:lane:right" to "no_parking",
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(StreetStandingProhibited, StreetParkingProhibited),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "no",
                "parking:condition:left" to "no_standing",
                "parking:condition:right" to "no_parking",
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(NoStreetParking, StreetParkingProhibited),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "no",
                "parking:condition:right" to "no_parking",
            ))
        )
    }

    @Test fun `parking separate`() {
        assertEquals(
            LeftAndRightStreetParking(StreetParkingSeparate, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "separate"))
        )

        assertEquals(
            LeftAndRightStreetParking(null, StreetParkingSeparate),
            createStreetParkingSides(mapOf("parking:lane:right" to "separate"))
        )

        assertEquals(
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate),
            createStreetParkingSides(mapOf("parking:lane:both" to "separate"))
        )

        assertEquals(
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "separate",
                "parking:lane:right" to "separate",
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate),
            createStreetParkingSides(mapOf("parking:lane" to "separate"))
        )
    }

    @Test fun `parking yes`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "yes"))
        )

        assertEquals(
            LeftAndRightStreetParking(null, IncompleteStreetParking),
            createStreetParkingSides(mapOf("parking:lane:right" to "yes"))
        )

        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, IncompleteStreetParking),
            createStreetParkingSides(mapOf("parking:lane:both" to "yes"))
        )

        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, IncompleteStreetParking),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "yes",
                "parking:lane:right" to "yes",
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, IncompleteStreetParking),
            createStreetParkingSides(mapOf("parking:lane" to "yes"))
        )
    }

    @Test fun `unknown orientation`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(UNKNOWN_ORIENTATION, null),
                null
            ),
            createStreetParkingSides(mapOf("parking:lane:left" to "something"))
        )
    }

    @Test fun `unknown position`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PARALLEL, UNKNOWN_POSITION),
                null
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "something"
            ))
        )
    }

    @Test fun `marked is interpreted as painted area only`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(UNKNOWN_ORIENTATION, PAINTED_AREA_ONLY),
                null
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "marked"
            )))
    }

    @Test fun `orientation without position`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(DIAGONAL, null),
                null
            ),
            createStreetParkingSides(mapOf("parking:lane:left" to "diagonal")))
    }

    @Test fun `all unknown`() {
        assertEquals(
            null,
            createStreetParkingSides(mapOf()))
    }
}
