package de.westnordost.streetcomplete.quests.parcel_locker_brand

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm

class AddParcelLockerBrand : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes with amenity = parcel_locker and !brand and !name and !operator"
    override val changesetComment = "Specify parcel locker brand"
    override val wikiLink = "Tag:amenity=parcel_locker"
    override val icon = Res.drawable.quest_parcel_locker_brand
    override val title = Res.string.quest_parcel_locker_brand
    override val achievements = listOf(POSTMAN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = parcel_locker")

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.parcelLockerBrand,
            on = on
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["brand"] = answer
    }
}
