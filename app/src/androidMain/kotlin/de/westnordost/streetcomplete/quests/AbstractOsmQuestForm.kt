package de.westnordost.streetcomplete.quests

import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.bundleOf
import androidx.core.view.children
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.visiblequests.HideQuestController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.osm.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.isDeletable
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

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

    private fun onShopDisusedSelected() {
        val languages = getLanguagesForFeatureDictionary()
        val vacantShop = featureDictionary
            .getByTags(element.tags)
            .firstOrNull { it.toElement().isPlace() }
            ?.toPrefixedFeature("disused")
            ?: featureDictionary.getById("shop/vacant", languages)!!
        onShopReplacementSelected(vacantShop)
    }

    private fun onShopReplacementSelected(feature: Feature) {
        viewLifecycleScope.launch {
            val builder = StringMapChangesBuilder(element.tags)
            feature.applyReplacePlaceTo(builder)
            solve(UpdateElementTagsAction(element, builder.create()))
        }
    }

    protected fun deletePoiNode() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.osm_element_gone_description)
            .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ -> onDeletePoiNodeConfirmed() }
            .setNeutralButton(R.string.leave_note) { _, _ -> composeNote() }
            .show()
    }
}
