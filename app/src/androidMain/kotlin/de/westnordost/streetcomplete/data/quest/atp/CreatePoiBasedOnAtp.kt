package de.westnordost.streetcomplete.data.quest.atp

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace

class CreatePoiBasedOnAtp() : OsmCreateElementQuestType<CreatePoiBasedOnAtpAnswer>, AndroidQuest {
    // do I actually need getFeature here? it gets passed to form somewhere else TODO
    override fun createForm() = AtpCreateForm()
    override val icon = R.drawable.ic_quest_dot // TODO: a radar icon? A plus icon?
    override val title = R.string.quest_atp_add_missing_poi_title
    override val wikiLink = "All the Places" // create dedicated import page? other documentation page? This one definitely does not explain enough in this context TODO
    override val achievements = listOf(CITIZEN)
    override val changesetComment = "Create POI surveyed by mapper, hint about missing entry was based on AllThePlaces data"

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }
}
