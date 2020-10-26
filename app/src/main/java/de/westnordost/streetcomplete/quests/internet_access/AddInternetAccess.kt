package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddInternetAccess : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          amenity ~ library|community_centre|youth_centre
          or tourism ~ hotel|guest_house|motel|hostel|alpine_hut|apartment|resort|camp_site|caravan_site|chalet
        )
        and name
        and (
          !internet_access
          or internet_access = yes
          or internet_access older today -2 years
        )
    """
    /* Asked less often than for example opening hours because this quest is only asked for
       tendentially larger places which are less likely to change often */

    override val commitMessage = "Add internet access"
    override val wikiLink = "Key:internet_access"
    override val icon = R.drawable.ic_quest_wifi
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_internet_access_name_title

    override fun createForm() = AddInternetAccessForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("internet_access", answer)
    }
}
