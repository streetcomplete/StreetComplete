package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.quests.toTags
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Abstract base class for a bottom sheet that lets the user create a note */
class CreatePoiFragment : TagEditor() {

    // keep the listener from note fragment, there is nothing note-specific happening anyway
    private val listener: CreateNoteFragment.Listener? get() = parentFragment as? CreateNoteFragment.Listener ?: activity as? CreateNoteFragment.Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefillTags: Map<String, String> = arguments?.getString(ARG_PREFILLED_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap()
        newTags.putAll(prefillTags)
        tagList.clear()
        tagList.addAll(newTags.toList())
        tagList.sortBy { it.first }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lastEditDate.text = arguments?.getString(ARG_NAME) ?: ""
        // set editorContainer top margin so the marker is always visible
        val p = binding.editorContainer.layoutParams as RelativeLayout.LayoutParams
        p.topMargin = (resources.displayMetrics.heightPixels - resources.getDimensionPixelOffset(R.dimen.quest_form_bottomOffset)) * 2 / 3

        arguments?.getString(ARG_ID)?.let {
            val recentFeatureIds = prefs.getString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, "")!!.split("§").toMutableList()
            if (recentFeatureIds.lastOrNull() == it) return@let
            recentFeatureIds.remove(it)
            recentFeatureIds.add(it)
            prefs.edit().putString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, recentFeatureIds.takeLast(10).joinToString("§")).apply()
        }

        binding.markerCreateLayout.createNoteIconView.setImageResource(R.drawable.ic_custom_overlay_poi)
        binding.markerCreateLayout.root.visibility = View.VISIBLE

    }

    override fun applyEdit() {
        val createNoteMarker = binding.markerCreateLayout.createNoteMarker
        val screenPos = createNoteMarker.getLocationInWindow()
        screenPos.offset(createNoteMarker.width / 2, createNoteMarker.height / 2)
        val position = listener?.getMapPositionAt(screenPos) ?: return

        if (prefs.getBoolean(Prefs.CLOSE_FORM_IMMEDIATELY_AFTER_SOLVING, false)) {
            listener?.onCreatedNote(position)
            viewLifecycleScope.launch { elementEditsController.add(createPoiEdit, Node(0, position), ElementPointGeometry(position), "survey", CreateNodeAction(position, element.tags), questKey) }
        } else {
            elementEditsController.add(createPoiEdit, Node(0, position), ElementPointGeometry(position), "survey", CreateNodeAction(position, element.tags))
            listener?.onCreatedNote(position)
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

val createPoiEdit = object : ElementEditType {
    override val icon: Int = R.drawable.ic_custom_overlay_poi
    override val title: Int = R.string.create_poi
    override val wikiLink: String? = null
    override val changesetComment: String = "Add node"
    override val name: String = "CreatePoiEditType" // keep old class name to avoid crash on startup if edit is in database
}
