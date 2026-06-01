package de.westnordost.streetcomplete.quests.bus_stop_shelter

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.COVERED
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.NO_SHELTER
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.SHELTER
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddBusStopShelter : OsmFilterQuestType<BusStopShelterAnswer>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          public_transport = platform
          or (highway = bus_stop and public_transport != stop_position)
          or highway = hitchhiking
        )
        and physically_present != no and naptan:BusStopType != HAR
        and access !~ no|private
        and !covered
        and location !~ underground|indoor
        and indoor != yes
        and tunnel != yes
        and (!level or level >= 0)
        and (!shelter or shelter older today -4 years)
    """
    /* Not asking again if it is covered because it means the stop itself is under a large
       building or roof building so this won't usually change */

    override val changesetComment = "Specify whether public transport stops have shelters"
    override val wikiLink = "Key:shelter"
    override val icon = Res.drawable.quest_bus_stop_shelter
    override val title = Res.string.quest_busStopShelter_title2
    override val achievements = listOf(PEDESTRIAN)

    @Composable
    override fun Form(on: (QuestAction<BusStopShelterAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO_SHELTER)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(SHELTER)) }
            ),
            on = on,
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_busStopShelter_covered)) { on(Answer(COVERED)) }
            ) }
        )
    }

    override fun applyAnswerTo(answer: BusStopShelterAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            SHELTER -> tags.updateWithCheckDate("shelter", "yes")
            NO_SHELTER -> tags.updateWithCheckDate("shelter", "no")
            COVERED -> {
                tags.remove("shelter")
                tags["covered"] = "yes"
            }
        }
    }
}
