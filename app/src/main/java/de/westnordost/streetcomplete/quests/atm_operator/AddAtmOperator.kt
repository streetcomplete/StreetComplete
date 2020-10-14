package de.westnordost.streetcomplete.quests.atm_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType

class AddAtmOperator(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "nodes with amenity = atm and !operator and !name and !brand"
    override val commitMessage = "Add ATM operator"
    override val wikiLink = "Tag:amenity=atm"
    override val icon = R.drawable.ic_quest_money

    override fun getTitle(tags: Map<String, String>) = R.string.quest_atm_operator_title

    override fun createForm() = AddAtmOperatorForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("operator", answer)
    }
}