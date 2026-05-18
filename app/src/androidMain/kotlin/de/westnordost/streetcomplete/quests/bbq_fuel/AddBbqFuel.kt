package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddBbqFuel : OsmFilterQuestType<BbqFuelAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bbq
          and !fuel
          and access !~ no|private
    """

    override val changesetComment = "Specify barbecue fuel"
    override val wikiLink = "Key:amenity=bbq"
    override val icon = R.drawable.quest_fire
    override val title = Res.string.quest_bbq_fuel_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = bbq")

    @Composable
    override fun Form(onAnswer: (BbqFuelAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddBbqFuelForm(onAnswer)
    }

    override fun applyAnswerTo(answer: BbqFuelAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BbqFuel -> tags["fuel"] = answer.osmValue
            BbqFuelAnswer.IsFirePit -> {
                tags.remove("amenity")
                tags["leisure"] = "firepit"
            }
        }
    }
}
