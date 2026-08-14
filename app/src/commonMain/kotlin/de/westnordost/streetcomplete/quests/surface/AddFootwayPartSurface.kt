package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.updateCommonSurfaceFromFootAndCyclewaySurface
import de.westnordost.streetcomplete.resources.*

class AddFootwayPartSurface : OsmFilterQuestType<Surface>() {

    override val elementFilter = """
        ways with (
          highway = footway
          or highway = path and foot != no
          or (highway ~ cycleway|bridleway and foot and foot != no)
        )
        and segregated = yes
        and !(sidewalk or sidewalk:left or sidewalk:right or sidewalk:both)
        and (
          !footway:surface
          or footway:surface ~ ${INVALID_SURFACES.joinToString("|")}
          or (
            footway:surface ~ paved|unpaved
            and !footway:surface:note
            and !check_date:footway:surface
          )
          or footway:surface older today -8 years
        )
        and (
          access !~ private|no
          or (foot and foot !~ private|no)
        )
        and ~path|footway|cycleway|bridleway !~ link
    """
    override val changesetComment = "Add footway path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = Res.drawable.quest_footway_surface
    override val title = Res.string.quest_footwayPartSurface_title
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)

    @Composable
    override fun Form(on: (QuestAction<Surface>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddPathPartSurfaceForm(on, countryInfo)
    }

    override fun applyAnswerTo(answer: Surface, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags, "footway")
        updateCommonSurfaceFromFootAndCyclewaySurface(tags)
    }
}
