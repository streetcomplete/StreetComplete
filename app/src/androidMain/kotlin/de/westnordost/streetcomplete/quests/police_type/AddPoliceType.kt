package de.westnordost.streetcomplete.quests.police_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddPoliceType : OsmFilterQuestType<PoliceType>() {

    override val elementFilter = "nodes, ways with amenity = police and !operator"
    override val changesetComment = "Specify Italian police types"
    override val wikiLink = "Tag:amenity=police"
    override val icon = R.drawable.ic_quest_police
    override val enabledInCountries = NoCountriesExcept("IT")
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_policeType_title

    override fun createForm() = AddPoliceTypeForm()

    override fun applyAnswerTo(answer: PoliceType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer.operatorName
        tags["operator:wikidata"] = answer.wikidata
    }
}
