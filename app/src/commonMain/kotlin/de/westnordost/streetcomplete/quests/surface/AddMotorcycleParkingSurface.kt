package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.resources.*

class AddMotorcycleParkingSurface : OsmFilterQuestType<Surface>() {

    override val elementFilter = """
        nodes, ways with amenity = motorcycle_parking
        and access !~ private|no
        and (
          !surface
          or surface ~ ${INVALID_SURFACES.joinToString("|")}
          or (
              surface ~ paved|unpaved
              and !surface:note
              and !note:surface
              and !check_date:surface
          )
          or surface older today -12 years
        )
    """

    override val changesetComment = "Specify motorcycle parking surface"
    override val wikiLink = "Key:surface"
    override val icon = Res.drawable.quest_motorcycle_parking_surface
    override val title = Res.string.quest_surface_title
    override val achievements = listOf(OUTDOORS)
    override val hint = Res.string.quest_select_hint_most_specific

    @Composable
    override fun Form(
        on: (QuestAction<Surface>) -> Unit,
        element: Element,
        geometry: ElementGeometry,
        countryInfo: CountryInfo,
    ) {
        AddMotorcycleParkingSurfaceForm(on,countryInfo)
    }

    override fun applyAnswerTo(answer: Surface, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
