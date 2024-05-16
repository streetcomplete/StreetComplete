package de.westnordost.streetcomplete.quests.parcel_locker_brand

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags

class AddParcelLockerBrand : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes with amenity = parcel_locker and !brand and !name and !operator"
    override val changesetComment = "Specify parcel locker brand"
    override val wikiLink = "Tag:amenity=parcel_locker"
    override val icon = R.drawable.ic_quest_parcel_locker_brand
    override val isDeleteElementEnabled = true
    override val achievements = listOf(POSTMAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parcel_locker_brand

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = parcel_locker")

    override fun createForm() = AddParcelLockerBrandForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["brand"] = answer
    }
}
