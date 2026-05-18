package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBeachSurface : OsmFilterQuestType<Surface>() {

    override val elementFilter = """
        ways, relations with
          natural = beach
          and !surface
    """

    override val changesetComment = "Specify beach surface"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.quest_beach
    override val title = Res.string.quest_surface_title
    override val achievements = listOf(OUTDOORS)

    @Composable
    override fun Form(onAnswer: (Surface) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = Surface.selectableValuesForBeaches,
            itemContent = { item ->
                ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
            },
            onClickOk = onAnswer,
            favoriteKey = "AddBeachSurfaceForm",
        )
    }

    override fun applyAnswerTo(answer: Surface, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
