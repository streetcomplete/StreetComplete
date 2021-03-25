package de.westnordost.streetcomplete.quests.police_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.quests.police_type.AddPostboxRoyalCypherForm
import de.westnordost.streetcomplete.quests.police_type.PostboxRoyalCypher

class AddPoliceType : OsmFilterQuestType<PostboxRoyalCypher>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = police
          and !operator
    """
    override val commitMessage = "Add police type"
    override val wikiLink = "Tag:amenity=police"
    override val icon = R.drawable.ic_quest_crown
    override val enabledInCountries = NoCountriesExcept(
        "IT"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxRoyalCypher_title

    override fun createForm() = AddPostboxRoyalCypherForm()

    override fun applyAnswerTo(answer: PostboxRoyalCypher, changes: StringMapChangesBuilder) {
        changes.add("royal_cypher", answer.osmValue)
    }
}
