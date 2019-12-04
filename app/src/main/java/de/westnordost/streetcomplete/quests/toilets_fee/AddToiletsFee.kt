package de.westnordost.streetcomplete.quests.toilets_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletsFee(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "nodes, ways with amenity = toilets and access !~ private|customers and !fee"
    override val commitMessage = "Add toilets fee"
    override val icon = R.drawable.ic_quest_toilet_fee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_toiletsFee_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("fee", if (answer) "yes" else "no")
    }
}
