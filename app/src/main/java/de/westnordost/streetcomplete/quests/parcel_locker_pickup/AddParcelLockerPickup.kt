package de.westnordost.streetcomplete.quests.parcel_locker_pickup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddParcelLockerPickup : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes with amenity = parcel_locker and !parcel_pickup"
    override val changesetComment = "Specify if it's possible to pickup parcels with this locker"
    override val wikiLink = "Tag:amenity=parcel_locker"
    override val icon = R.drawable.ic_quest_parcel_locker_pickup
    override val isDeleteElementEnabled = true
    override val achievements = listOf(POSTMAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parcel_locker_pickup

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = parcel_locker")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["parcel_pickup"] = answer.toYesNo()
    }
}
