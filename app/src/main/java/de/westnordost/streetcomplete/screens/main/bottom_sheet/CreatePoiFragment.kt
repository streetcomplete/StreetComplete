package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.graphics.toPointF
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.dialogs.showOutsideDownloadedAreaDialog
import de.westnordost.streetcomplete.view.checkIsSurvey
import de.westnordost.streetcomplete.view.confirmIsSurvey
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

/** Abstract base class for a bottom sheet that lets the user create a note */
class CreatePoiFragment : TagEditor() {

    // keep the listener from note fragment, there is nothing note-specific happening anyway
    private val listener: CreateNoteFragment.Listener? get() = parentFragment as? CreateNoteFragment.Listener ?: activity as? CreateNoteFragment.Listener
    private val levelFilter: LevelFilter by inject()
    private val downloadedTilesSource: DownloadedTilesSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefillTags: Map<String, String> = arguments?.getString(ARG_PREFILLED_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap()
        newTags.putAll(prefillTags)
        val allowedLevel = levelFilter.allowedLevel
        if (levelFilter.isEnabled && allowedLevel != null && !newTags.contains("level") && !newTags.contains("level:ref") && !newTags.contains("addr:floor")) {
            val levelTag = if (levelFilter.allowedLevelTags.size == 1) levelFilter.allowedLevelTags.single()
                else if (levelFilter.allowedLevelTags.contains("level:ref") && "[a-zA-Z]".toRegex().containsMatchIn(allowedLevel)) "level:ref"
                else "level"
            newTags[levelTag] = if (levelTag == "level:ref") allowedLevel
                else allowedLevel.toIntOrNull()?.toString() ?: ""
        }
        tagList.clear()
        tagList.addAll(newTags.toList())
        tagList.sortBy { it.first }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.elementInfo.text = arguments?.getString(ARG_NAME) ?: ""
        // set editorContainer top margin so the marker is always visible
        val p = binding.editorContainer.layoutParams as RelativeLayout.LayoutParams
        p.topMargin = (resources.displayMetrics.heightPixels - resources.getDimensionPixelOffset(R.dimen.quest_form_bottomOffset)) * 2 / 3

        arguments?.getString(ARG_ID)?.let {
            val recentFeatureIds = prefs.getString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, "").split("§").toMutableList()
            if (recentFeatureIds.lastOrNull() == it) return@let
            recentFeatureIds.remove(it)
            recentFeatureIds.add(it)
            prefs.putString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, recentFeatureIds.takeLast(25).joinToString("§"))
        }

        binding.markerCreateLayout.pin.pinIconView.setImageResource(R.drawable.ic_add_poi)
        binding.markerCreateLayout.root.visibility = View.VISIBLE
    }

    override suspend fun applyEdit() {
        val createNoteMarker = binding.markerCreateLayout.pin.root
        val screenPos = createNoteMarker.getLocationInWindow()
        screenPos.offset(createNoteMarker.width / 2, createNoteMarker.height / 2)
        val position = listener?.getMapPositionAt(screenPos.toPointF()) ?: return
        showOutsideDownloadedAreaDialog(requireContext(), position, downloadedTilesSource) {
            lifecycleScope.launch { reallyApplyEdit(position) }
        }
    }

    private suspend fun reallyApplyEdit(position: LatLon) {
        val isSurvey = checkIsSurvey(ElementPointGeometry(position), recentLocationStore.get())
        if (!isSurvey && !confirmIsSurvey(requireContext()))
            return
        elementEditsController.add(addNodeEdit, ElementPointGeometry(position), "survey", CreateNodeAction(position, element.tags), isSurvey, questKey)
        listener?.onCreatedNote(position)
        arguments?.getString(ARG_ID)?.let {
            val prefillTags: Map<String, String> = arguments?.getString(ARG_PREFILLED_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap()
            if (!element.isPlace() && prefillTags != element.tags)
                prefs.putString(Prefs.CREATE_NODE_LAST_TAGS_FOR_FEATURE + it, Json.encodeToString(element.tags))
        }
    }

    companion object {
        private const val ARG_PREFILLED_TAGS = "prefilled_tags"
        private const val ARG_NAME = "feature_name"
        private const val ARG_ID = "feature_id"

        fun createFromFeature(feature: Feature?, pos: LatLon) = CreatePoiFragment().also {
            it.arguments = bundleOf(ARG_PREFILLED_TAGS to feature?.addTags?.let { Json.encodeToString(it) }, ARG_NAME to feature?.name, ARG_ID to feature?.id)
            // tag editor arguments are actually unnecessary here, but we still need an original element
            it.requireArguments().putAll(createArguments(Node(0L, pos), ElementPointGeometry(pos), null, null))
        }
        fun createWithPrefill(prefill: String, pos: LatLon, questKey: QuestKey? = null) = CreatePoiFragment().also {
            // this will only prefill if there is one equals sign in the line
            it.arguments = bundleOf(ARG_PREFILLED_TAGS to Json.encodeToString(prefill.toTags()))
            it.requireArguments().putAll(createArguments(Node(0L, pos), ElementPointGeometry(pos), null, null, questKey))
        }
    }
}

val addNodeEdit = object : ElementEditType {
    override val icon: Int = R.drawable.ic_add_poi
    override val title: Int = R.string.create_poi
    override val wikiLink: String? = null
    override val changesetComment: String = "Add node"
    override val name: String = "AddNode"
}

// convert simple key = value pairs into tags, and understand simple filter expressions
fun String.toTags(): Map<String, String> {
    val tags = mutableMapOf<String, String>()
    if (!contains('('))
        split("\n", " and ").forEach { line ->
            if (line.isBlank() || line.contains(" or ")) return@forEach
            val kv = line.split("=", "!~", "~")
            if (kv.size != 1 && kv.size != 2) return@forEach
            if ('|' in kv[0] || '!' in kv[0] || '*' in kv[0]) return@forEach
            if (kv.size == 1 || "!=" in line || '~' in line) tags[kv[0].trim()] = ""
            else tags[kv[0].trim()] = kv[1].trim()
        }
    return tags
}
