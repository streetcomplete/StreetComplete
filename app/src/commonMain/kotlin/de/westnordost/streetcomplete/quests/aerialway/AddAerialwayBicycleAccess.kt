package de.westnordost.streetcomplete.quests.aerialway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddAerialwayBicycleAccess : OsmFilterQuestType<AerialwayBicycleAccessAnswer>() {

    override val elementFilter = """
        ways with
          aerialway ~ cable_car|gondola|chair_lift
          and !aerialway:bicycle and !bicycle
    """

    override val changesetComment = "Specify whether bicycles can be taken on aerialway"
    override val wikiLink = "Tag:aerialway"
    override val icon = Res.drawable.quest_aerialway_bicycle
    override val title = Res.string.quest_aerialway_bicycle_title
    override val achievements = listOf(RARE, BICYCLIST)

    @Composable
    override fun Form(on: (QuestAction<AerialwayBicycleAccessAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO)) },
                AnswerItem(stringResource(Res.string.quest_aerialway_bicycle_summer)) { on(Answer(SUMMER)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(YES)) }
            ),
            on = on,
            otherAnswers = listOf(
                AnswerItem(stringResource(Res.string.quest_hairdresser_not_signed)) { on(Answer(NO_SIGN)) }
            )
        )
    }

    override fun applyAnswerTo(answer: AerialwayBicycleAccessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["aerialway:bicycle"] = "yes"
            SUMMER -> tags["aerialway:bicycle"] = "summer"
            NO -> tags["aerialway:bicycle"] = "no"
            NO_SIGN -> tags["aerialway:bicycle:signed"] = "no"
        }
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways with aerialway
        """.toElementFilterExpression())
    }
