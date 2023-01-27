package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.OFF_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import org.junit.Assert.assertEquals
import org.junit.Test

class StreetParkingParserKtTest {

    @Test fun `new parking schema is preferred over old parking schema`() {
        assertEquals(
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "parallel",
                "parking:both" to "no"
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(NoStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "parallel",
                "parking:left" to "no"
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(null, NoStreetParking),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "parallel",
                "parking:right" to "no"
            ))
        )
    }

    @Test fun `all unknown`() {
        assertEquals(
            null,
            createStreetParkingSides(mapOf()))
    }

    //region new schema

    @Test fun parking() {

        val orientations = listOf(
            "parallel" to PARALLEL,
            "diagonal" to DIAGONAL,
            "perpendicular" to PERPENDICULAR
        )

        val positions = listOf(
            "lane" to ON_STREET,
            "half_on_kerb" to HALF_ON_STREET,
            "on_kerb" to OFF_STREET,
            "street_side" to STREET_SIDE,
        )

        for ((orientationStr, orientationValue) in orientations) {

            for ((positionStr, positionValue) in positions) {

                val parking = StreetParkingPositionAndOrientation(orientationValue, positionValue)

                assertEquals(
                    LeftAndRightStreetParking(parking, null),
                    createStreetParkingSides(mapOf(
                        "parking:left" to positionStr,
                        "parking:left:orientation" to orientationStr
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(null, parking),
                    createStreetParkingSides(mapOf(
                        "parking:right" to positionStr,
                        "parking:right:orientation" to orientationStr
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:both" to positionStr,
                        "parking:both:orientation" to orientationStr
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:both" to positionStr,
                        "parking:left:orientation" to orientationStr,
                        "parking:right:orientation" to orientationStr,
                    ))
                )

                assertEquals(
                    LeftAndRightStreetParking(parking, parking),
                    createStreetParkingSides(mapOf(
                        "parking:left" to positionStr,
                        "parking:right" to positionStr,
                        "parking:both:orientation" to orientationStr,
                    ))
                )

                // does not accept it without explicit sides

                assertEquals(
                    null,
                    createStreetParkingSides(mapOf(
                        "parking" to positionStr,
                        "parking:orientation" to orientationStr
                    ))
                )
            }
        }
    }

    @Test fun `special values for parking`() {

        val values = listOf(
            "no" to NoStreetParking,
            "separate" to StreetParkingSeparate,
            "yes" to IncompleteStreetParking,
            "foobar" to UnknownStreetParking
        )

        for ((str, value) in values) {
            assertEquals(
                LeftAndRightStreetParking(value, null),
                createStreetParkingSides(mapOf("parking:left" to str))
            )

            assertEquals(
                LeftAndRightStreetParking(null, value),
                createStreetParkingSides(mapOf("parking:right" to str))
            )

            assertEquals(
                LeftAndRightStreetParking(value, value),
                createStreetParkingSides(mapOf("parking:both" to str))
            )

            assertEquals(
                LeftAndRightStreetParking(value, value),
                createStreetParkingSides(mapOf(
                    "parking:left" to str,
                    "parking:right" to str,
                ))
            )

            assertEquals(
                null,
                createStreetParkingSides(mapOf("parking" to str))
            )
        }
    }

    @Test fun `unknown orientation leads to unknown`() {
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:left" to "lane",
                "parking:left:orientation" to "something"
            ))
        )
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf("parking:left:orientation" to "something"))
        )
    }

    @Test fun `unknown position leads to unknown`() {
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:left" to "something",
                "parking:left:orientation" to "parallel"
            ))
        )
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf("parking:left" to "something"))
        )
    }

    @Test fun `either position or orientation is unknown leads to incomplete`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:left:orientation" to "parallel"
            ))
        )

        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:left" to "lane"
            ))
        )
    }

    //endregion

    //region old schema

    @Test fun `parking with old schema`() {

        val orientations = listOf(
            "parallel" to PARALLEL,
            "diagonal" to DIAGONAL,
            "perpendicular" to PERPENDICULAR
        )

        val positions = listOf(
            "on_street" to ON_STREET,
            "half_on_kerb" to HALF_ON_STREET,
            "on_kerb" to OFF_STREET,
            "street_side" to STREET_SIDE,
            "lay_by" to STREET_SIDE,
            "painted_area_only" to PAINTED_AREA_ONLY,
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

    @Test fun `parking with old schema where position differs per side`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PERPENDICULAR, HALF_ON_STREET)
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:both" to "perpendicular",
                "parking:lane:left:perpendicular" to "street_side",
                "parking:lane:right:perpendicular" to "half_on_kerb",
            ))
        )
    }

    @Test fun `parking with old schema where orientation differs per side`() {
        assertEquals(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(PERPENDICULAR, STREET_SIDE),
                StreetParkingPositionAndOrientation(PARALLEL, HALF_ON_STREET)
            ),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "perpendicular",
                "parking:lane:right" to "parallel",
                "parking:lane:left:perpendicular" to "street_side",
                "parking:lane:right:parallel" to "half_on_kerb",
            ))
        )
    }

    @Test fun `special values for parking with old schema`() {

        val values = listOf(
            "no" to NoStreetParking,
            "separate" to StreetParkingSeparate,
            "yes" to IncompleteStreetParking,
            "foobar" to UnknownStreetParking
        )

        for ((str, value) in values) {
            assertEquals(
                LeftAndRightStreetParking(value, null),
                createStreetParkingSides(mapOf("parking:lane:left" to str))
            )

            assertEquals(
                LeftAndRightStreetParking(null, value),
                createStreetParkingSides(mapOf("parking:lane:right" to str))
            )

            assertEquals(
                LeftAndRightStreetParking(value, value),
                createStreetParkingSides(mapOf("parking:lane:both" to str))
            )

            assertEquals(
                LeftAndRightStreetParking(value, value),
                createStreetParkingSides(mapOf(
                    "parking:lane:left" to str,
                    "parking:lane:right" to str,
                ))
            )

            assertEquals(
                LeftAndRightStreetParking(value, value),
                createStreetParkingSides(mapOf("parking:lane" to str))
            )
        }
    }

    @Test fun `unknown orientation leads to unknown with old schema`() {
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "something"))
        )
    }

    @Test fun `unknown position leads to unknown with old schema`() {
        assertEquals(
            LeftAndRightStreetParking(UnknownStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:left:parallel" to "something"
            ))
        )
    }

    @Test fun `marked is interpreted as incomplete with old schema`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf(
                "parking:lane:left" to "marked",
                "parking:lane:left:marked" to "on_kerb"
            )))
    }

    @Test fun `orientation without position is interpreted as incomplete with old schema`() {
        assertEquals(
            LeftAndRightStreetParking(IncompleteStreetParking, null),
            createStreetParkingSides(mapOf("parking:lane:left" to "diagonal")))
    }

    //endregion
}
