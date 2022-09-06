package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

fun createNoteTextForTooLongTags(
    englishQuestTitle: String,
    elementType: ElementType,
    elementId: Long,
    changes: Collection<StringMapEntryChange>
): String =
    "Unable to answer \"$englishQuestTitle\" " +
    "for https://osm.org/${elementType.name.lowercase()}/$elementId " +
    "via ${ApplicationConstants.USER_AGENT}:\n\n" +
    "One of the tags in the attempted edit exceeds the 255 character limit - \n\n" +
    changes.joinToString("\n") { when (it) {
        is StringMapEntryAdd -> "${it.key}=${it.value}"
        is StringMapEntryModify -> "${it.key}=${it.value}"
        is StringMapEntryDelete -> "delete ${it.key}"
    } } +
    "\n\nCan it be rephrased or approximated to fit?"
