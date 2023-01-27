package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddInternetAccess : OsmFilterQuestType<InternetAccess>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ library|community_centre|youth_centre
          or tourism ~ hotel|guest_house|motel|hostel|alpine_hut|apartment|resort|caravan_site|chalet
          or tourism = camp_site and backcountry != yes and camp_site != basic
        )
        and access !~ no|private
        and (
          !internet_access
          or internet_access = yes
          or internet_access older today -2 years
        )
    """
    /* Asked less often than for example opening hours because this quest is only asked for
       tendentially larger places which are less likely to change often */

    override val changesetComment = "Specify whether place provides internet access"
    override val wikiLink = "Key:internet_access"
    override val icon = R.drawable.ic_quest_wifi
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_internet_access_title

    override fun createForm() = AddInternetAccessForm()

    override fun applyAnswerTo(answer: InternetAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("internet_access", answer.osmValue)
    }
}
