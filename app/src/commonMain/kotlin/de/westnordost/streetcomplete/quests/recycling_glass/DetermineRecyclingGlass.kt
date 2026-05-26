package de.westnordost.streetcomplete.quests.recycling_glass

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.ANY
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.BOTTLES
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class DetermineRecyclingGlass : OsmFilterQuestType<RecyclingGlass>() {

    override val elementFilter = """
        nodes, ways with
          amenity = recycling
          and recycling_type = container
          and recycling:glass = yes
          and !recycling:glass_bottles
          and access !~ private|no
    """
    override val changesetComment = "Determine whether any glass or just glass bottles can be recycled here"
    override val wikiLink = "Key:recycling"
    override val icon = Res.drawable.quest_recycling_glass
    override val title = Res.string.quest_recycling_glass_title
    // see isUsuallyAnyGlassRecyclableInContainers.yml
    override val enabledInCountries = AllCountriesExcept("CZ")
    override val achievements = listOf(CITIZEN)
    override val hint = Res.string.quest_determineRecyclingGlass_description_any_glass

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = recycling")

    @Composable
    override fun Form(onAnswer: (RecyclingGlass) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                Answer(stringResource(Res.string.quest_recycling_type_any_glass)) { onAnswer(ANY) },
                Answer(stringResource(Res.string.quest_recycling_type_glass_bottles_short)) { onAnswer(BOTTLES) }
            )
        )
    }

    override fun applyAnswerTo(answer: RecyclingGlass, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            ANY -> {
                // to mark that it has been checked
                tags["recycling:glass_bottles"] = "yes"
            }
            BOTTLES -> {
                tags["recycling:glass_bottles"] = "yes"
                tags["recycling:glass"] = "no"
            }
        }
    }
}
