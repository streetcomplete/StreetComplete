package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept

class AddPostboxRoyalCypher : OsmFilterQuestType<PostboxRoyalCypher>() {

    override val elementFilter = """
        nodes with
          amenity = post_box
          and !royal_cypher
    """
    override val commitMessage = "Add postbox royal cypher"
    override val wikiLink = "Key:royal_cypher"
    override val icon = R.drawable.ic_quest_crown
    override val enabledInCountries = NoCountriesExcept(
        "GB"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxRoyalCypher_title

    override fun createForm() = AddPostboxRoyalCypherForm()

    override fun applyAnswerTo(answer: PostboxRoyalCypher, changes: StringMapChangesBuilder) {
        changes.add("royal_cypher", answer.osmValue)
    }
}
