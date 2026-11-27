package de.westnordost.streetcomplete.quests.grit_bin_seasonal

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.location.CurrentSeason
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

class AddGritBinSeasonal(
    private val currentSeason: CurrentSeason
) : OsmElementQuestType<Boolean> {

    val filter by lazy { """
        nodes with
          amenity = grit_bin
          and !seasonal
          and access !~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether grit bins are seasonal"
    override val wikiLink = "Key:seasonal"
    override val icon = Res.drawable.quest_calendar
    override val title = Res.string.quest_gritBinSeasonal_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_seasonal

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("seasonal", if (answer) "no" else "winter")
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = grit_bin")

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.filter(filter).asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean {
        return filter.matches(element) && currentSeason.getSeason(element) == CurrentSeason.Season.SUMMER
    }
}
