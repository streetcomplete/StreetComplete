package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.*
import org.koin.android.ext.android.inject

class AddCrossingForm : AListQuestForm<CrossingAnswer>() {
    private val mapDataSource: MapDataWithEditsSource by inject()

    override val items get() = listOfNotNull(
        TextItem(YES, R.string.quest_crossing_yes),
        TextItem(NO, R.string.quest_crossing_no),
        if (isOnSidewalkOrCrossing()) null else TextItem(PROHIBITED, R.string.quest_crossing_prohibited)
    )

    /* PROHIBITED is neither available for sidewalks nor crossings (=separately mapped sidewalk infrastructure)
    *  because a "no" answer would require to also delete/adapt the crossing ways, rather than just
    *  tagging crossing=no on the vertex.
    *  See https://github.com/streetcomplete/StreetComplete/pull/2999#discussion_r681516203
    *  and https://github.com/streetcomplete/StreetComplete/issues/5160 */

    private fun isOnSidewalkOrCrossing(): Boolean {
        val ways = mapDataSource.getWaysForNode(element.id)
        return ways.any {
            val footway = it.tags["footway"]
            footway == "sidewalk" || footway == "crossing"
        }
    }
}
