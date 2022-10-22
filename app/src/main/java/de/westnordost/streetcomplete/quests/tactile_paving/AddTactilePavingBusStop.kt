package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddTactilePavingBusStop : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        (
          public_transport = platform
          or (highway = bus_stop and public_transport != stop_position)
        )
        and physically_present != no and naptan:BusStopType != HAR
        and (
          !tactile_paving
          or tactile_paving = unknown
          or tactile_paving = no and tactile_paving older today -4 years
          or tactile_paving = yes and tactile_paving older today -8 years
        )
    """
    override val changesetComment = "Specify whether public transport stops have tactile paving"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_blind_bus
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON
    override val achievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_busStopTactilePaving_title

    override fun createForm() = TactilePavingForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.toYesNo())
    }
}
