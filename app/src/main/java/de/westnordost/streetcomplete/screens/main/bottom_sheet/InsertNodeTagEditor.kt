package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.insert.InsertBetween
import de.westnordost.streetcomplete.data.osm.edits.insert.InsertNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Fragment that lets the user split an OSM way */
class InsertNodeTagEditor : TagEditor() {

    private val listener: CreateNoteFragment.Listener? get() = parentFragment as? CreateNoteFragment.Listener ?: activity as? CreateNoteFragment.Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newTags.clear() // they are filled from the original element, which is unwanted...
        newTags.putAll(arguments?.getString(ARG_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap())
        tagList.clear()
        tagList.addAll(newTags.toList())
        tagList.sortBy { it.first }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lastEditDate.text = arguments?.getString(ARG_FEATURE_NAME) ?: ""
    }

    override fun applyEdit() {
        val args = requireArguments()
        val position: LatLon = Json.decodeFromString(args.getString(ARG_POS)!!)
        val between: InsertBetween = Json.decodeFromString(args.getString(ARG_BETWEEN)!!)
        val way: Way = Json.decodeFromString(args.getString(ARG_WAY)!!)
        elementEditsController.add(createPoiEdit, way, ElementPointGeometry(position), "survey", InsertNodeAction(position, element.tags, between))
        listener?.onCreatedNote(position)
    }

    companion object {
        private const val ARG_POS = "pos"
        private const val ARG_WAY = "way"
        private const val ARG_FEATURE_NAME = "feature"
        private const val ARG_BETWEEN = "between"
        private const val ARG_TAGS = "tags"

        fun create(position: LatLon, feature: Feature?, between: InsertBetween, way: Way): InsertNodeTagEditor {
            val f = InsertNodeTagEditor()
            val args = createArguments(Node(0L, position), ElementPointGeometry(position), null, null)
            args.putAll(bundleOf(
                ARG_POS to Json.encodeToString(position),
                ARG_BETWEEN to Json.encodeToString(between),
                ARG_WAY to Json.encodeToString(way),
                ARG_TAGS to feature?.addTags,
            ))
            feature?.let {
                args.putString(ARG_FEATURE_NAME, it.name)
                args.putString(ARG_TAGS, Json.encodeToString(it.addTags))
            }
            f.arguments = args
            return f
        }
    }
}
