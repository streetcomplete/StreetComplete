package de.westnordost.streetcomplete.testutils

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
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA

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

fun osmQuestKey(
    elementType: ElementType = ElementType.NODE,
    elementId: Long = 1L,
    questTypeName: String = QUEST_TYPE.name
) = OsmQuestKey(elementType, elementId, questTypeName)

val QUEST_TYPE = TestQuestTypeA()
