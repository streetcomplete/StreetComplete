package de.westnordost.streetcomplete.testutils

import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.atp.atpquests.AtpQuestHidden
import de.westnordost.streetcomplete.data.atp.atpquests.CreateElementUsingAtpQuest
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.notequests.createOsmNoteQuest
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtpAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement

fun questHidden(
    elementType: ElementType = ElementType.NODE,
    elementId: Long = 1L,
    questType: OsmElementQuestType<*> = QUEST_TYPE,
    geometry: ElementGeometry = pGeom(),
    timestamp: Long = 123L
) = OsmQuestHidden(elementType, elementId, questType, geometry, timestamp)

fun noteQuestHidden(
    note: Note = note(),
    timestamp: Long = 123L
) = OsmNoteQuestHidden(note, timestamp)

fun osmQuest(
    questType: OsmElementQuestType<*> = QUEST_TYPE,
    elementType: ElementType = ElementType.NODE,
    elementId: Long = 1L,
    geometry: ElementGeometry = pGeom()
) =
    OsmQuest(questType, elementType, elementId, geometry)

fun osmNoteQuest(
    id: Long = 1L,
    pos: LatLon = p()
) = createOsmNoteQuest(id, pos)

fun atpQuest(
    id: Long = 1L,
    atpEntry: AtpEntry = atpEntry(),
    pos: LatLon = p(),
) = CreateElementUsingAtpQuest(id, atpEntry, MockAtpQuestType, pos)

fun osmQuestKey(
    elementType: ElementType = ElementType.NODE,
    elementId: Long = 1L,
    questTypeName: String = QUEST_TYPE.name
) = OsmQuestKey(elementType, elementId, questTypeName)

fun atpQuestHidden(
    atpEntry: AtpEntry = atpEntry(),
    timestamp: Long = 123L
) = AtpQuestHidden(atpEntry, timestamp)

val QUEST_TYPE = TestQuestTypeA()

object MockAtpQuestType : OsmCreateElementQuestType<CreatePoiBasedOnAtpAnswer> {
    override val icon: Int = 199
    override val title: Int = 199
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = mock()
    override val changesetComment: String = "changeset comment from MockQuestType"
}
