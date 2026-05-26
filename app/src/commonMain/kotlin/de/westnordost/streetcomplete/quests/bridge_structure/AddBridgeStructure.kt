package de.westnordost.streetcomplete.quests.bridge_structure

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource

class AddBridgeStructure : OsmFilterQuestType<BridgeStructure>() {

    override val elementFilter = """
        ways with
          man_made = bridge
          and !bridge:structure
          and !bridge:movable
          and (!indoor or indoor = no)
    """
    override val changesetComment = "Specify bridge structures"
    override val wikiLink = "Key:bridge:structure"
    override val icon = Res.drawable.quest_bridge
    override val title = Res.string.quest_bridge_structure_title
    override val achievements = listOf(BUILDING)

    @Composable
    override fun Form(onAnswer: (BridgeStructure) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = BridgeStructure.entries,
            itemsPerRow = 1,
            itemContent = { Image(painterResource(it.icon), null) },
            onClickOk = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: BridgeStructure, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["bridge:structure"] = answer.osmValue
    }
}
