package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuelAnswer.Fuels
import de.westnordost.streetcomplete.resources.*

class AddBbqFuel : OsmFilterQuestType<BbqFuelAnswer>() {

    override val elementFilter = """
        nodes, ways with
          (
              (
                  amenity = bbq
                  and !fuel
              )
              or
              (
                  amenity = baking_oven
                  and (!oven or oven = yes)
              )
          )
          and access !~ no|private
    """

    override val changesetComment = "Specify cooking fuel"
    override val wikiLink = "Key:amenity=bbq"
    override val icon = Res.drawable.quest_fire
    override val title = Res.string.quest_bbq_fuel_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = bbq")

    @Composable
    override fun Form(on: (QuestAction<BbqFuelAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddBbqFuelForm(on)
    }

    override fun applyAnswerTo(answer: BbqFuelAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == BbqFuelAnswer.IsFirePit) {
            tags.remove("amenity")
            tags["leisure"] = "firepit"
        } else if (answer is Fuels) {
            // BBQs and ovens use slightly different tags for fuel
            if (tags["amenity"] == "bbq") {
                tags["fuel"] = answer.fuels.joinToString(";") { it.bbqValue }
            } else {
                tags["oven"] = answer.fuels.joinToString(";") { it.ovenValue }
            }
        }
    }
}
