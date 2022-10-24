package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags

class AddParkingAccess : OsmFilterQuestType<ParkingAccess>() {

    // Exclude parking=street_side lacking any access tags, because most of
    // these are found alongside public access roads, and likely will be
    // access=yes by default. Leaving these in makes this quest repetitive and
    // leads to users adding lots of redundant access=yes tags to satisfy the
    // quest. parking=street_side with access=unknown seems like a valid target
    // though.
    //
    // Cf. #2408: Parking access might omit parking=street_side
    // Cf. #4538: should skip elements with more specific access tag already mapped
    override val elementFilter = """
        nodes, ways, relations with amenity = parking
        and (
            access = unknown
            or (!access and parking !~ street_side|lane) and
            !trailer and !caravan and !double_tracked_motor_vehicle and !motorcar and
            !motorhome and !tourist_bus and !coach and !goods and !hgv and !hgv_articulated and
            !bdouble and !agricultural and !auto_rickshaw and !nev and !golf_cart and !atv and
            !ohv and !snowmobile and !psv and !bus and !taxi and !minibus and !share_taxi and
            !hov and !carpool and !car_sharing and !emergency and !hazmat and !water and
            !disabled and !4wd_only and !roadtrain and !lhv and !tank
        )
    """
    override val changesetComment = "Specify parking access"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking_access
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_access_title2

    override fun createForm() = AddParkingAccessForm()

    override fun applyAnswerTo(answer: ParkingAccess, tags: Tags, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
