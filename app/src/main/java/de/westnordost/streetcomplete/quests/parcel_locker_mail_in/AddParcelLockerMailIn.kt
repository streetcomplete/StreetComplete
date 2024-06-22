package de.westnordost.streetcomplete.quests.parcel_locker_mail_in

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags

class AddParcelLockerMailIn : OsmFilterQuestType<ParcelLockerMailIn>() {

    override val elementFilter = "nodes with amenity = parcel_locker and !parcel_mail_in"
    override val changesetComment = "Specify if it's possible to drop off parcels with this locker"
    override val wikiLink = "Tag:amenity=parcel_locker"
    override val icon = R.drawable.ic_quest_parcel_locker_deposit
    override val isDeleteElementEnabled = true
    override val achievements = listOf(POSTMAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parcel_locker_mail_in

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = parcel_locker")

    override fun createForm() = AddParcelLockerMailInForm()

    override fun applyAnswerTo(answer: ParcelLockerMailIn, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["parcel_mail_in"] = answer.osmValue
    }
}
