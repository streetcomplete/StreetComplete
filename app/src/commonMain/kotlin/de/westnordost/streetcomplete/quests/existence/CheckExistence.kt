package de.westnordost.streetcomplete.quests.existence

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.containsAll
import org.jetbrains.compose.resources.stringResource

class CheckExistence(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<Unit> {

    private val nodesFilter by lazy { """
        nodes with
          ((
            (
              ${RESURVEY_FILTERS_2_YEARS.joinToString("\n              or ")}
            )
            and (${lastChecked(2.0)})
          ) or (
            (
              ${RESURVEY_FILTERS_4_YEARS.joinToString("\n              or ")}
            )
            and (${lastChecked(4.0)})
          ) or (
            (
              ${RESURVEY_FILTERS_6_YEARS.joinToString("\n              or ")}
            )
            and (${lastChecked(6.0)})
          ) or (
            (
              ${RESURVEY_FILTERS_10_YEARS.joinToString("\n              or ")}
            )
            and (${lastChecked(10.0)})
          ) or (
            (
              ${RESURVEY_FILTERS_14_YEARS.joinToString("\n              or ")}
            )
            and (${lastChecked(14.0)})
          ))
          and access !~ no|private
          and (!seasonal or seasonal = no)
          and (!intermittent or intermittent = no)
          and (!permanent or permanent != yes)
          and (!heritage or heritage = no)
          and !listed_status
    """.toElementFilterExpression() }
    // - traffic_calming = table is often used as a property of a crossing: we don't want the app
    //    to delete the crossing if the table is not there anymore, so exclude that
    // - postboxes are in 4 years category so that postbox collection times is asked instead more often
    // - bicycle parkings, motorcycle parkings have capacity quests asked every
    //    few years already, so if it's gone now, it will be noticed that way.
    //    But some users disable this quests as spammy or boring or unimportant,
    //    so asking about this anyway would be a good idea.

    override val changesetComment = "Survey if places still exist"
    override val wikiLink: String? = null
    override val icon = Res.drawable.quest_check
    override val title = Res.string.quest_existence_title2
    override val achievements = listOf(CITIZEN, OUTDOORS)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) =
        nodesFilter.matches(element) && getFeature(element) != null

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry): Sequence<Element> {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = getFeature(element) ?: return emptySequence()
        return mapData.filter { it.tags.containsAll(feature.tags) }.asSequence()
    }

    @Composable
    override fun Form(on: (QuestAction<Unit>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Action.DeletePoi) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(Unit)) }
            ),
        )
    }

    override fun applyAnswerTo(answer: Unit, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateCheckDate()
    }

    private fun lastChecked(yearsAgo: Double): String = """
        older today -$yearsAgo years
        or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -$yearsAgo years" }}
    """.trimIndent()
}
