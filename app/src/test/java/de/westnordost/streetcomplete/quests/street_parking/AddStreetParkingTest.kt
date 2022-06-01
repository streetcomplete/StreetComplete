package de.westnordost.streetcomplete.quests.street_parking

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParking
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSeparate
import de.westnordost.streetcomplete.osm.street_parking.StreetStandingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStoppingProhibited
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddStreetParkingTest {

    private val questType = AddStreetParking()

    @Test fun `apply no parking on both sides`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(NoStreetParking, NoStreetParking),
            StringMapEntryAdd("parking:lane:both", "no")
        )
    }

    @Test fun `apply different no parking on different sides`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(StreetStoppingProhibited, StreetStandingProhibited),
            StringMapEntryAdd("parking:lane:both", "no"),
            StringMapEntryAdd("parking:condition:left", "no_stopping"),
            StringMapEntryAdd("parking:condition:right", "no_standing"),
        )
    }

    @Test fun `apply separate parking answer`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(StreetParkingSeparate, StreetParkingSeparate),
            StringMapEntryAdd("parking:lane:both", "separate")
        )
    }

    @Test fun `apply parallel parking answer on both sides`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB)
            ),
            StringMapEntryAdd("parking:lane:both", "parallel"),
            StringMapEntryAdd("parking:lane:both:parallel", "half_on_kerb"),
        )
    }

    @Test fun `apply parallel parking answer with different positions on sides`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.ON_STREET),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.HALF_ON_KERB)
            ),
            StringMapEntryAdd("parking:lane:both", "parallel"),
            StringMapEntryAdd("parking:lane:left:parallel", "on_street"),
            StringMapEntryAdd("parking:lane:right:parallel", "half_on_kerb"),
        )
    }

    @Test fun `apply street side parking answer with different orientations on sides`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.PERPENDICULAR, ParkingPosition.STREET_SIDE),
                StreetParkingPositionAndOrientation(ParkingOrientation.PARALLEL, ParkingPosition.STREET_SIDE)
            ),
            StringMapEntryAdd("parking:lane:left", "perpendicular"),
            StringMapEntryAdd("parking:lane:right", "parallel"),
            StringMapEntryAdd("parking:lane:left:perpendicular", "street_side"),
            StringMapEntryAdd("parking:lane:right:parallel", "street_side"),
        )
    }

    @Test fun `apply different parking positions and orientations on sides`() {
        questType.verifyAnswer(
            LeftAndRightStreetParking(
                StreetParkingPositionAndOrientation(ParkingOrientation.DIAGONAL, ParkingPosition.STREET_SIDE),
                StreetParkingPositionAndOrientation(ParkingOrientation.PERPENDICULAR, ParkingPosition.PAINTED_AREA_ONLY)
            ),
            StringMapEntryAdd("parking:lane:left", "diagonal"),
            StringMapEntryAdd("parking:lane:left:diagonal", "street_side"),
            StringMapEntryAdd("parking:lane:right", "perpendicular"),
            StringMapEntryAdd("parking:lane:right:perpendicular", "painted_area_only"),
        )
    }
}
