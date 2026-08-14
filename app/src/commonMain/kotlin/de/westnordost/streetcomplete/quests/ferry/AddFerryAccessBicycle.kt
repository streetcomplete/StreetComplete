package de.westnordost.streetcomplete.quests.ferry

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.quests.ferry.FerryBicycleAccess.NO
import de.westnordost.streetcomplete.quests.ferry.FerryBicycleAccess.NOT_SIGNED
import de.westnordost.streetcomplete.quests.ferry.FerryBicycleAccess.YES
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddFerryAccessBicycle : OsmElementQuestType<FerryBicycleAccess> {

    private val filter by lazy {
        "ways, relations with route = ferry and !bicycle and !bicycle:signed"
            .toElementFilterExpression()
    }

    override val changesetComment = "Specify ferry access for bicycles"
    override val wikiLink = "Tag:route=ferry"
    override val icon = Res.drawable.quest_ferry_bicycle
    override val title = Res.string.quest_ferry_bicycle_title
    override val hasMarkersAtEnds = true
    override val achievements = listOf(RARE, BICYCLIST)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val wayIdsInFerryRoutes = wayIdsInFerryRoutes(mapData.relations)
        return mapData
            .filter(filter)
            .filter { it !is Way || it.id !in wayIdsInFerryRoutes }
            .asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        if (element is Way) return null
        return true
    }

    @Composable
    override fun Form(on: (QuestAction<FerryBicycleAccess>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(YES)) }
            ),
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_generic_answer_noSign)) { on(Answer(NOT_SIGNED)) }
            ) }
        )
    }

    override fun applyAnswerTo(answer: FerryBicycleAccess, tags: StringMapChangesBuilder, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            FerryBicycleAccess.YES ->
                tags["bicycle"] = "yes"

            FerryBicycleAccess.NO ->
                tags["bicycle"] = "no"

            FerryBicycleAccess.NOT_SIGNED ->
                tags["bicycle:signed"] = "no"
        }
    }
}
