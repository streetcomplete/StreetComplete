package de.westnordost.streetcomplete.quests.toilets_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletsFee : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          amenity = toilets
          and access !~ private|customers
          and !fee
    """
    override val commitMessage = "Add toilets fee"
    override val wikiLink = "Key:fee"
    override val icon = R.drawable.ic_quest_toilet_fee
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_toiletsFee_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("fee", answer.toYesNo())
    }
}
