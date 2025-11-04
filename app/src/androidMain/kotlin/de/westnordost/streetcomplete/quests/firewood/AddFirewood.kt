package de.westnordost.streetcomplete.quests.firewood

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddFirewood : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
            (
                leisure = firepit
                or (amenity = bbq and fuel = wood)
                or (tourism = wilderness_hut and fireplace = yes)
            )
            and access !~ private|no
            and !wood_provided
    """

    // for now only enabled in the nordics / high-trust societies
    override val enabledInCountries = NoCountriesExcept(
        "SE",
        "DK",
        "FI",
        "NO",
        "CH"
    )
    override val changesetComment = "Specified if firewood is provided"
    override val wikiLink = "Tag:leisure=firepit"
    override val icon = R.drawable.quest_firewood
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_firewood_provided_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with leisure = firepit or amenity = bbq or tourism = wilderness_hut")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["wood_provided"] = answer.toYesNo()
    }
}
