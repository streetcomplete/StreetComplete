package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddBuildingType : OsmFilterQuestType<String>() {

    // in the case of man_made, historic, military and power, these tags already contain
    // information about the purpose of the building, so no need to force asking it
    // same goes (more or less) for tourism, amenity, leisure. See #1854, #1891
    override val elementFilter = """
        ways, relations with building = yes
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
         and location != underground
         and abandoned != yes
         and abandoned != building
         and abandoned:building != yes
         and ruins != yes and ruined != yes
    """
    override val commitMessage = "Add building types"
    override val wikiLink = "Key:building"
    override val icon = R.drawable.ic_quest_building

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_title

    override fun createForm() = AddBuildingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        val (key, value) = answer.split("=")

        if (key == "man_made") {
            changes.delete("building")
            changes.add("man_made", value)
        } else if (key != "building") {
            changes.addOrModify(key, value)
        } else {
            changes.modify("building", value)
        }
    }
}
