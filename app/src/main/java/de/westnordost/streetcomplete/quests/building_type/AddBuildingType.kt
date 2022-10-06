package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddBuildingType : OsmFilterQuestType<BuildingType>() {

    // in the case of man_made, historic, military, aeroway and power, these tags already contain
    // information about the purpose of the building, so no need to force asking it
    // or question would be confusing as there is no matching reply in available answers
    // same goes (more or less) for tourism, amenity, leisure. See #1854, #1891, #3233
    override val elementFilter = """
        ways, relations with (building = yes or building = unclassified)
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
         and !aeroway
         and !railway
         and !description
         and location != underground
         and abandoned != yes
         and abandoned != building
         and abandoned:building != yes
         and ruins != yes and ruined != yes
    """
    override val changesetComment = "Specify building types"
    override val wikiLink = "Key:building"
    override val icon = R.drawable.ic_quest_building
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_title

    override fun createForm() = AddBuildingTypeForm()

    override fun applyAnswerTo(answer: BuildingType, tags: Tags, timestampEdited: Long) {
        if (answer.osmKey == "man_made") {
            tags.remove("building")
            tags["man_made"] = answer.osmValue
        } else if (answer.osmKey != "building") {
            tags[answer.osmKey] = answer.osmValue
            if (answer == BuildingType.ABANDONED) {
                tags.remove("disused")
            }
            if (answer == BuildingType.RUINS && tags["disused"] == "no") {
                tags.remove("disused")
            }
            if (answer == BuildingType.RUINS && tags["abandoned"] == "no") {
                tags.remove("abandoned")
            }
        } else {
            tags["building"] = answer.osmValue
        }
    }
}
