package de.westnordost.streetcomplete.quests.kids_area

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.kids_area.KidsAreaAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddKidsArea : OsmFilterQuestType<KidsAreaAnswer>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          amenity ~ restaurant|cafe|biergarten|food_court
          or amenity = fast_food and indoor_seating = yes
          or shop ~ mall|department_store|furniture|baby_goods|toys
          or tourism = hotel
        )
        and !kids_area
        and access !~ private|no
    """

    override val changesetComment = "Survey availability of kids areas"
    override val wikiLink = "Key:kids_area"
    override val icon = Res.drawable.quest_kids_area
    override val title = Res.string.quest_kids_area_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    @Composable
    override fun Form(on: (QuestAction<KidsAreaAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_kids_area_yes)) { on(Answer(YES)) },
                AnswerItem(stringResource(Res.string.quest_kids_area_limited)) { on(Answer(LIMITED)) },
                AnswerItem(stringResource(Res.string.quest_kids_area_no)) { on(Answer(NO)) },
            ),
        )
    }

    override fun applyAnswerTo(answer: KidsAreaAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["kids_area"] = "yes"
            LIMITED -> tags["kids_area"] = "limited"
            NO -> tags["kids_area"] = "no"
        }
    }
}
