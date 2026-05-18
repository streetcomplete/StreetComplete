package de.westnordost.streetcomplete.quests.camera_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCameraType : OsmFilterQuestType<CameraType>() {

    override val elementFilter = """
        nodes with
         surveillance:type = camera
         and surveillance ~ public|outdoor|traffic
         and !camera:type
    """
    override val changesetComment = "Specify camera types"
    override val wikiLink = "Tag:surveillance:type"
    override val icon = R.drawable.quest_surveillance_camera
    override val title = Res.string.quest_camera_type_title
    override val achievements = listOf(CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with surveillance and surveillance:type = camera")

    @Composable
    override fun Form(onAnswer: (CameraType) -> Unit, element: Element) {
        ItemSelectQuestForm(
            items = CameraType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: CameraType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["camera:type"] = answer.osmValue
    }
}
