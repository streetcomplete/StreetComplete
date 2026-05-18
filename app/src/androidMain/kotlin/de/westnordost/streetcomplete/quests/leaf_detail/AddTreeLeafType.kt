package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTreeLeafType : OsmFilterQuestType<TreeLeafTypeAnswer>() {
    override val elementFilter = """
        nodes with
          natural = tree
          and !leaf_type
          and !~"(taxon|genus|species).*"
    """
    override val changesetComment = "Specify leaf types"
    override val wikiLink = "Key:leaf_type"
    override val icon = R.drawable.quest_leaf
    override val title = Res.string.quest_leafType_tree_title
    override val achievements = listOf(OUTDOORS)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with natural = tree")

    @Composable
    override fun Form(onAnswer: (TreeLeafTypeAnswer) -> Unit, element: Element) {
        ItemSelectQuestForm(
            items = TreeLeafType.entries,
            itemsPerRow = 2,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_leafType_tree_is_just_a_stump)) {
                    onAnswer(NotTreeButStump)
                },
            )
        )
    }

    override fun applyAnswerTo(answer: TreeLeafTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is TreeLeafType -> tags["leaf_type"] = answer.osmValue
            NotTreeButStump -> tags["natural"] = "tree_stump"
        }
    }
}
