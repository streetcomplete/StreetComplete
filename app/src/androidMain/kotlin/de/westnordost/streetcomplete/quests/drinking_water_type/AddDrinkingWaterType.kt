package de.westnordost.streetcomplete.quests.drinking_water_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddDrinkingWaterType : OsmFilterQuestType<DrinkingWaterType>() {

    override val elementFilter = """
        nodes with
        (
            (amenity = drinking_water and !disused:amenity)
            or
            (disused:amenity = drinking_water and !amenity and older today -1 years)
        )
        and (!intermittent or intermittent = no)
        and (!seasonal or seasonal = no)
        and !man_made and !natural and !fountain and !pump
    """

    override val changesetComment = "Specify drinking water types"
    override val wikiLink = "Tag:amenity=drinking_water"
    override val icon = R.drawable.quest_drinking_water // another icon?
    override val title = Res.string.quest_drinking_water_type_title2
    override val achievements = listOf(CITIZEN, OUTDOORS)

    @Composable
    override fun Form(onAnswer: (DrinkingWaterType) -> Unit, element: Element) {
        ItemSelectQuestForm(
            items = DrinkingWaterType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
            favoriteKey = "AddDrinkingWaterTypeForm",
        )
    }

    override fun applyAnswerTo(answer: DrinkingWaterType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
