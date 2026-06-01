package de.westnordost.streetcomplete.quests.boat_rental

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBoatRental : OsmFilterQuestType<Set<BoatRental>>() {

    override val elementFilter = """
        nodes, ways with
        amenity = boat_rental
        and (
          ${BoatRental.entries.joinToString(" and ") { "!${it.osmValue}" }}
          or ${DEPRECATED_RENTALS.joinToString(" or ")}
        )
    """
    override val changesetComment = "Specify boats for rental"
    override val wikiLink = "Tag:amenity=boat_rental"
    override val icon = Res.drawable.quest_boat
    override val title = Res.string.quest_boat_rental_title
    override val achievements = listOf(OUTDOORS, RARE)

    @Composable
    override fun Form(on: (QuestAction<Set<BoatRental>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemsSelectQuestForm(
            items = BoatRental.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            on = on,
        )
    }

    override fun applyAnswerTo(answer: Set<BoatRental>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.forEach { tags[it.osmValue] = "yes" }
        // remove ambiguous ones that should have been specified correctly by the user's answer
        DEPRECATED_RENTALS.forEach { tags.remove(it) }
    }
}

private val DEPRECATED_RENTALS = listOf(
    // ambiguous:
    // motor or rowing?        what kind of board?          rowing what?
    "dinghy_rental", "paddleboard_rental", "board_rental", "rowing_rental"
)
