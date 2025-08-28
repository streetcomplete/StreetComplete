package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags

class AddMaxWeight : OsmFilterQuestType<MaxWeightAnswer>(), AndroidQuest {

    override val elementFilter = """
        ways with
         highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|service|busway
         and bridge and bridge != no
         and service != driveway
         and !maxweight and maxweight:signed != no
         and !maxaxleload
         and !maxbogieweight
         and !maxweight:hgv and !maxweight:bus and !maxweight:hgv_articulated and !maxweight:tourist_bus and !maxweight:coach
         and !maxweightrating and !maxweightrating:hgv and !maxweightrating:bus and !hgv
         and !maxunladenweight and !maxunladenweight:hgv and !maxunladenweight:bus
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and (access !~ private|no or (foot and foot !~ private|no))
         and area != yes
    """
    override val changesetComment = "Specify maximum allowed weights"
    override val wikiLink = "Key:maxweight"
    override val icon = R.drawable.ic_quest_max_weight
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_maxweight_title

    override fun createForm() = AddMaxWeightForm()

    override fun applyAnswerTo(answer: MaxWeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is MaxWeight -> {
                answer.applyTo(tags)
            }
            is MaxWeightAnswer.NoSign -> {
                tags["maxweight:signed"] = "no"
            }
        }
    }
}
