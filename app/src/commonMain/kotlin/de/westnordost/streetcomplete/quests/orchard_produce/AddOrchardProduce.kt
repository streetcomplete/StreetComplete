package de.westnordost.streetcomplete.quests.orchard_produce

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddOrchardProduce : OsmFilterQuestType<Set<OrchardProduce>>() {

    override val elementFilter = """
        ways, relations with landuse = orchard
        and !trees and !produce and !crop
        and orchard != meadow_orchard
    """
    override val changesetComment = "Specify orchard produces"
    override val wikiLink = "Tag:landuse=orchard"
    override val icon = Res.drawable.quest_apple
    override val title = Res.string.quest_orchard_produce_title
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    @Composable
    override fun Form(onAnswer: (Set<OrchardProduce>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddOrchardProduceForm(onAnswer, countryInfo)
    }

    override fun applyAnswerTo(answer: Set<OrchardProduce>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["produce"] = answer.joinToString(";") { it.osmValue }

        val landuse = answer.singleOrNull()?.osmLanduseValue
        if (landuse != null) {
            tags["landuse"] = landuse
        }
    }
}
