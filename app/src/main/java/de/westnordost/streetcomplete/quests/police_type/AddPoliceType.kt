package de.westnordost.streetcomplete.quests.police_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept

class AddPoliceType : OsmFilterQuestType<PoliceType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = police
          and !operator
    """
    override val commitMessage = "Add police type"
    override val wikiLink = "Tag:amenity=police"
    override val icon = R.drawable.ic_quest_police
    override val enabledInCountries = NoCountriesExcept("IT")

    override fun getTitle(tags: Map<String, String>) = R.string.quest_policeType_title

    override fun createForm() = AddPoliceTypeForm()

    override fun applyAnswerTo(answer: PoliceType, changes: StringMapChangesBuilder) {
        changes.add("operator", answer.operatorName);
        changes.addOrModify("operator:wikidata", answer.wikidata);
    }
}
