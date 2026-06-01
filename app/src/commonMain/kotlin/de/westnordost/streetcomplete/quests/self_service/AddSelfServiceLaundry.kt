package de.westnordost.streetcomplete.quests.self_service

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.NO
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.ONLY
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.OPTIONAL
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddSelfServiceLaundry : OsmFilterQuestType<SelfServiceLaundry>() {

    override val elementFilter = "nodes, ways with shop = laundry and !self_service"
    override val changesetComment = "Survey whether laundries provide self-service"
    override val wikiLink = "Tag:shop=laundry"
    override val icon = Res.drawable.quest_laundry
    override val title = Res.string.quest_laundrySelfService_title2
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<SelfServiceLaundry>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_optional)) { on(Answer(OPTIONAL)) },
                AnswerItem(stringResource(Res.string.quest_hasFeature_only)) { on(Answer(ONLY)) }
            ),
            on = on
        )
    }

    override fun applyAnswerTo(answer: SelfServiceLaundry, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            NO -> {
                tags["self_service"] = "no"
                tags["laundry_service"] = "yes"
            }
            OPTIONAL -> {
                tags["self_service"] = "yes"
                tags["laundry_service"] = "yes"
            }
            ONLY -> {
                tags["self_service"] = "yes"
                tags["laundry_service"] = "no"
            }
        }
    }
}
