package de.westnordost.streetcomplete.quests.bus_stop_bench

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBenchStatusOnBusStop : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          public_transport = platform
          or (highway = bus_stop and public_transport != stop_position)
          or highway = hitchhiking
        )
        and physically_present != no and naptan:BusStopType != HAR
        and access !~ no|private
        and (!bench or bench older today -4 years)
    """
    override val changesetComment = "Specify whether public transport stops have benches"
    override val wikiLink = "Key:bench"
    override val icon = R.drawable.quest_bench_public_transport
    override val title = Res.string.quest_busStopBench_title2
    override val achievements = listOf(PEDESTRIAN)

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("bench", answer.toYesNo())
    }
}
