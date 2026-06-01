package de.westnordost.streetcomplete.quests

import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.places.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.places.isPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getSystemResourceEnvironment

/** Abstract base class for any bottom sheet with which the user answers a specific quest(ion)  */
abstract class AbstractOsmQuestForm<T> : AbstractQuestForm(), IsShowingQuestDetails {

    protected fun composeNote() {
        viewLifecycleScope.launch {
            val questTitleResource = osmElementQuestType.getTitle(element.tags) ?: questType.title
            val resourceEnvironment = getSystemResourceEnvironment()
            val questTitle = org.jetbrains.compose.resources.getString(resourceEnvironment, questTitleResource)
            val hintLabel = getNameAndLocationLabel(resourceEnvironment, LayoutDirection.Ltr, element, featureDictionary)
            val leaveNoteContext = if (hintLabel.isNullOrBlank()) {
                "Unable to answer \"$questTitle\""
            } else {
                "Unable to answer \"$questTitle\" – $hintLabel"
            }
            listener?.onComposeNote(osmElementQuestType, element, geometry, leaveNoteContext)
        }
    }
}
