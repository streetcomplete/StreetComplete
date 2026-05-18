package de.westnordost.streetcomplete.quests.police_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource

class AddPoliceType : OsmFilterQuestType<PoliceType>() {

    override val elementFilter = "nodes, ways with amenity = police and !operator"
    override val changesetComment = "Specify Italian police types"
    override val wikiLink = "Tag:amenity=police"
    override val icon = R.drawable.quest_police
    override val title = Res.string.quest_policeType_title
    override val enabledInCountries = NoCountriesExcept("IT")
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(onAnswer: (PoliceType) -> Unit, element: Element) {
        ItemSelectQuestForm(
            items = PoliceType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), it.title) },
            onClickOk = onAnswer,
            favoriteKey = "AddPoliceTypeForm",
        )
    }

    override fun applyAnswerTo(answer: PoliceType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer.operatorName
        tags["operator:wikidata"] = answer.wikidata
    }
}
