package de.westnordost.streetcomplete.quests.beer

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*

class AddBeer : OsmFilterQuestType<BeerServed>(), AndroidQuest {

    override val elementFilter = """
         nodes, ways with
         amenity ~ restaurant|bar|biergarten|pub|cafe|nightclub
         and (
             !drink:beer 
             or drink:beer ~ yes|served
             or drink:beer older today -4 years
         )
         and !microbrewery
         and (!brewery or brewery = no)
    """
    override val changesetComment = "Survey whether beer is served and how"
    override val wikiLink = "Key:drink:beer"
    override val icon = R.drawable.preset_fas_beer
    override val title = Res.string.quest_drink_beer_title
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) =
        if (tags["amenity"] in listOf("pub", "bar", "biergarten")) {
            Res.string.quest_drink_beer_title_pub
        } else {
            Res.string.quest_drink_beer_title
        }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = AddBeerForm()

    override fun applyAnswerTo(answer: BeerServed, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("drink:beer", answer.osmValue)
    }
}