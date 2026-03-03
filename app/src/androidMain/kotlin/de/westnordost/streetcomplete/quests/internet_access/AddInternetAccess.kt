package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_go_inside

class AddInternetAccess : OsmFilterQuestType<Set<InternetAccess>>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ library|community_centre|youth_centre|hospital|ranger_station
          or tourism ~ hotel|guest_house|motel|hostel|alpine_hut|apartment|resort|caravan_site|chalet|wilderness_hut
          or tourism = camp_site and backcountry != yes and camp_site != basic
          or aeroway = terminal
          or shop ~ mall|department_store
          or tourism = information and information ~ office|visitor_center
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
    override val icon = R.drawable.quest_wifi
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_internet_access_title

    override fun createForm() = AddInternetAccessForm()

    override fun applyAnswerTo(answer: Set<InternetAccess>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val osmValue = if (answer.isEmpty()) {
            "no"
        } else {
            answer.joinToString(";") { it.osmValue }
        }
        tags.updateWithCheckDate("internet_access", osmValue)
    }
}
