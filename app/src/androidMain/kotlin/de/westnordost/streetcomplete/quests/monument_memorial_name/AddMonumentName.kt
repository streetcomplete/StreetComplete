package de.westnordost.streetcomplete.quests.monument_memorial_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.applyTo

class AddMonumentName : OsmFilterQuestType<MonumentNameAnswer>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with
        historic = monument and !name and noname != yes and name:signed != no
    """


    override val changesetComment = "Determine monument names"
    override val wikiLink = "Tag:historic=monument"
    override val icon = R.drawable.ic_quest_monument
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_monument_name_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways, relations with historic = monument and !name")

    override fun createForm() = AddMonumentNameForm()

    override fun applyAnswerTo(answer: MonumentNameAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is MonumentNameAnswer.NoName -> {
                tags["noname"] = "yes"
            }
            is MonumentName -> {
                answer.localizedNames.applyTo(tags)
            }
        }
    }
}
