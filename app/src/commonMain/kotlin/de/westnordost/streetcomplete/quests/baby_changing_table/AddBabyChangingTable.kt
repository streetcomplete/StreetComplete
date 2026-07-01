package de.westnordost.streetcomplete.quests.baby_changing_table

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.baby_changing_table.BabyChangingTableAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.stringResource

class AddBabyChangingTable : OsmFilterQuestType<BabyChangingTableAnswer>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity = toilets
          or (
            amenity ~ restaurant|cafe|biergarten|food_court|fuel|library|community_centre
            or amenity = fast_food and indoor_seating = yes
            or shop ~ mall|department_store|baby_goods
            or shop = bakery and indoor_seating = yes
          ) and toilets != no
        )
        and !diaper and !changing_table
    """
    override val changesetComment = "Survey availability of baby changing tables"
    override val wikiLink = "Key:changing_table"
    override val icon = Res.drawable.quest_baby
    override val title = Res.string.quest_baby_changing_table_title2
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    @Composable
    override fun Form(on: (QuestAction<BabyChangingTableAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(YES)) }
            ),
            otherAnswers = {
                listOfNotNull(
                    if (element.tags["amenity"] != "toilets") {
                        AnswerItem(stringResource(Res.string.quest_wheelchairAccessPat_noToilet)) { on(Answer(NO_TOILET)) }
                    } else null
                )
            },
        )
    }

    override fun applyAnswerTo(answer: BabyChangingTableAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["changing_table"] = "yes"
            NO -> tags["changing_table"] = "no"
            NO_TOILET -> tags["toilets"] = "no"
        }
    }
}
