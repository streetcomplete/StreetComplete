package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddBBQFuel : OsmFilterQuestType<BBQFuel>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity = bbq
          and !fuel
        )
        and access !~ no|private
    """


    override val changesetComment = "Specify what grill is powered by"
    override val wikiLink = "Key:amenity=bbq"
    override val icon = R.drawable.ic_quest_bbq_fuel
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bbq_fuel_title

    override fun createForm() = AddBBQFuelForm()

    override fun applyAnswerTo(answer: BBQFuel, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fuel"] = answer.osmValue
    }
}
