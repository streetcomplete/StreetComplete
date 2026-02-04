package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddArtworkType : OsmFilterQuestType<ArtworkType>(), AndroidQuest {

    override val elementFilter = "nodes, ways with tourism = artwork and !artwork_type"

    override val changesetComment = "Survey artwork type"
    override val wikiLink = "Key:artwork_type"
    override val icon = R.drawable.quest_artwork
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_artwork_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with tourism = artwork")

    override fun createForm() = AddArtworkTypeForm()

    override fun applyAnswerTo(answer: ArtworkType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
