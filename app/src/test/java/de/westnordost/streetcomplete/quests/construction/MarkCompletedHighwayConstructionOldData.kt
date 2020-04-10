package de.westnordost.streetcomplete.quests.construction

import java.util.Date

import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.DateUtil

class MarkCompletedHighwayConstructionOldData
internal constructor(overpassApi: OverpassMapDataAndGeometryApi, dateString: String) :
    MarkCompletedHighwayConstruction(overpassApi) {

    private val date: Date = DateUtil.basicISO8601().parse(dateString)

    override fun getCurrentDateString(): String {
        return DateUtil.getOffsetDateStringFromDate(0, date) + "T00:00:00Z"
    }

    override fun getOffsetDateString(offset: Int): String {
        return DateUtil.getOffsetDateStringFromDate(offset, date) + "T00:00:00Z"
    }

}
