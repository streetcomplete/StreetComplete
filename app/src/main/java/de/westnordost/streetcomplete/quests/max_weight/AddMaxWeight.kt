package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign.*

class AddMaxWeight : OsmFilterQuestType<MaxWeightAnswer>() {

    override val commitMessage = "Add maximum allowed weight"
    override val wikiLink = "Key:maxweight"
    override val icon = R.drawable.ic_quest_max_weight
    override val hasMarkersAtEnds = true

    override val elementFilter = """
        ways with highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|service
         and service != driveway
         and !maxweight and maxweight:signed != no
         and !maxaxleload
         and !maxbogieweight
         and !maxweight:hgv and !maxweight:bus and !maxweight:hgv_articulated and !maxweight:tourist_bus and !maxweight:coach
         and !maxweightrating and !maxweightrating:hgv and !maxweightrating:bus and !hgv
         and !maxunladenweight and !maxunladenweight:hgv and !maxunladenweight:bus
         and bridge and bridge != no
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and (access !~ private|no or (foot and foot !~ private|no))
         and area != yes
    """

    override fun getTitle(tags: Map<String, String>) = R.string.quest_maxweight_title

    override fun createForm() = AddMaxWeightForm()

    override fun applyAnswerTo(answer: MaxWeightAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is MaxWeight -> {
                changes.add(answer.sign.osmKey, answer.weight.toString())
            }
            is NoMaxWeightSign -> {
                changes.addOrModify("maxweight:signed", "no")
            }
        }
    }
}

private val MaxWeightSign.osmKey get() = when(this) {
    MAX_WEIGHT             -> "maxweight"
    MAX_GROSS_VEHICLE_MASS -> "maxweightrating"
    MAX_AXLE_LOAD          -> "maxaxleload"
    MAX_TANDEM_AXLE_LOAD   -> "maxbogieweight"
}
