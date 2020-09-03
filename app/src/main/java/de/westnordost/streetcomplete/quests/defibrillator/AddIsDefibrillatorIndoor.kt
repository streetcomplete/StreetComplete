package de.westnordost.streetcomplete.quests.defibrillator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddIsDefibrillatorIndoor(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes with
         emergency=defibrillator
         and access !~ private|no
         and !indoor
    """
    override val commitMessage = "Add whether defibrillator is inside building"
    override val wikiLink = "Key:indoor"
    override val icon = R.drawable.ic_quest_defibrillator

    override fun getTitle(tags: Map<String, String>) = R.string.quest_is_defibrillator_inside_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("indoor", if (answer) "yes" else "no")
    }
}
