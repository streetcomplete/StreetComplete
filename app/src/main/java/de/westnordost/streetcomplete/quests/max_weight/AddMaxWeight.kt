package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.AllCountriesExcept

class AddMaxWeight(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<MaxWeightAnswer>(o) {

    override val commitMessage = "Add maximum allowed weight"
    override val icon = R.drawable.ic_quest_max_weight
    override val hasMarkersAtEnds = true

    override val enabledInCountries = AllCountriesExcept(
            "CA", // requires special icons
            "US", // requires special icons - single weight sign is available, but it is a rare variant
            "AU"  // requires special icons - single weight sign is available, but it is a rare variant
    )

    override val tagFilters = """
        ways with highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|service
         and service != driveway
         and !maxweight and maxweight:signed != no
         and !maxaxleload and !maxbogieweight
         and !maxweight:hgv and !maxweight:bus and !maxweight:hgv_articulated and !maxweight:tourist_bus
         and !maxweight:coach
         and !maxweightrating
         and !maxweightrating:hgv and !maxweightrating:bus and !hgv
         and !maxunladenweight
         and !maxunladenweight:hgv and !maxunladenweight:bus
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
                changes.add("maxweight", answer.value.toString())
            }
            is NoMaxWeightSign -> {
                changes.addOrModify("maxweight:signed", "no")
            }
        }
    }
}
