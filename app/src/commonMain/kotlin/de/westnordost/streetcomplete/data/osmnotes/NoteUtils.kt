package de.westnordost.streetcomplete.data.osmnotes

import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getSystemResourceEnvironment

/** Returns a string that explains in which element and quest context this note was created */
suspend fun getEditTypeContextForNote(
    element: Element?,
    elementEditType: ElementEditType,
    featureDictionary: FeatureDictionary,
): String {
    val resourceEnvironment = getSystemResourceEnvironment()
    val title = getString(resourceEnvironment, elementEditType.title)
    val nameAndLocationLabel = element?.let {
        getNameAndLocationLabel(resourceEnvironment, LayoutDirection.Ltr, element, featureDictionary)
    }
    val headerText = when (elementEditType) {
        is OsmElementQuestType<*> -> "Can't answer \"$title\""
        is Overlay -> "In overlay \"$title\""
        else -> throw IllegalArgumentException()
    }
    val result = StringBuilder()
    result.append(headerText)
    if (element != null) {
        result.append(" for https://osm.org/${element.type.name.lowercase()}/${element.id}")
    }
    if (!nameAndLocationLabel.isNullOrEmpty()) {
        result.append(" ($nameAndLocationLabel)")
    }
    result.append(", via ${ApplicationConstants.USER_AGENT}:")

    return result.toString()
}

suspend fun getNoteTextForInvalidEdit(
    element: Element?,
    elementEditType: ElementEditType,
    changes: Collection<StringMapEntryChange>,
    featureDictionary: FeatureDictionary,
): String =
    getEditTypeContextForNote(element, elementEditType, featureDictionary) +
    "\n\n" +
    "One of the tags in the attempted edit exceeds the 255 character limit." +
    "\n\n" +
    changes.joinToString("\n") { when (it) {
        is StringMapEntryAdd -> "${it.key}=${it.value}"
        is StringMapEntryModify -> "${it.key}=${it.value}"
        is StringMapEntryDelete -> "delete ${it.key}"
    } } +
    "\n\n" +
    "Can it be rephrased or approximated to fit?"
