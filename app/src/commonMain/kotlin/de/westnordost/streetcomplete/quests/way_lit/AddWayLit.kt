package de.westnordost.streetcomplete.quests.way_lit

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.osm.lit.applyTo
import de.westnordost.streetcomplete.osm.maxspeed.MAX_SPEED_TYPE_KEYS
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.stringResource

class AddWayLit : OsmFilterQuestType<WayLitOrIsStepsAnswer>() {

    /* Using sidewalk, source:maxspeed=*urban etc and a urban-like maxspeed as tell-tale tags for
       (urban) streets which reached a certain level of development. I.e. non-urban streets will
       usually not even be lit in industrialized countries.

       Also, only include paths only for those which are equal to footway/cycleway to exclude
       most hike paths and trails.

        See #427 for discussion. */
    override val elementFilter = """
        ways with
        (
          highway ~ ${LIT_RESIDENTIAL_ROADS.joinToString("|")}
          or highway ~ ${LIT_NON_RESIDENTIAL_ROADS.joinToString("|")} and
          (
            sidewalk ~ both|left|right|yes|separate
            or sidewalk:both = yes
            or sidewalk:left = yes
            or sidewalk:right = yes
            or ~"${MAX_SPEED_TYPE_KEYS.joinToString("|")}" ~ ".*:(urban|.*zone.*|nsl_restricted)"
            or maxspeed <= 60
          )
          or highway ~ ${LIT_WAYS.joinToString("|")}
          or highway = path and (foot = designated or bicycle = designated)
        )
        and
        (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
        and (access !~ private|no or (foot and foot !~ private|no))
        and indoor != yes
        and ~path|footway|cycleway !~ link
    """

    override val changesetComment = "Specify whether ways are lit"
    override val wikiLink = "Key:lit"
    override val icon = Res.drawable.quest_lantern
    override val title = Res.string.quest_lit_title
    override val achievements = listOf(PEDESTRIAN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_overlay

    @Composable
    override fun Form(on: (QuestAction<WayLitOrIsStepsAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(WayLit(NO))) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(WayLit(YES))) }
            ),
            on = on,
            otherAnswers = { listOfNotNull(
                AnswerItem(stringResource(Res.string.lit_value_24_7)) {
                    on(Answer(WayLit(NIGHT_AND_DAY)))
                },
                AnswerItem(stringResource(Res.string.lit_value_automatic)) {
                    on(Answer(WayLit(AUTOMATIC)))
                },
                if (element.couldBeSteps()) {
                    AnswerItem(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                        on(Answer(IsActuallyStepsAnswer))
                    }
                } else null,
            ) }
        )
    }

    override fun applyAnswerTo(answer: WayLitOrIsStepsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is IsActuallyStepsAnswer -> tags.changeToSteps()
            is WayLit -> answer.litStatus.applyTo(tags)
        }
    }

    companion object {
        private val LIT_RESIDENTIAL_ROADS = arrayOf(
            "residential", "living_street", "pedestrian", "busway"
        )

        private val LIT_NON_RESIDENTIAL_ROADS = arrayOf(
            "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
            "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "service"
        )

        private val LIT_WAYS = arrayOf("footway", "cycleway", "steps")
    }
}
