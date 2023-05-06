package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.core.os.bundleOf
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.osm.IS_SHOP_EXPRESSION
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.util.math.PositionOnWay
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
        val positionOnWay: PositionOnWay = Json.decodeFromString(args.getString(ARG_POSITION_ON_WAY)!!)
        val action = createNodeAction(positionOnWay, mapDataSource) { changeBuilder ->
            element.tags.forEach { changeBuilder[it.key] = it.value } // todo: also need to remove tags, but first show "starting tags" if i allow them
        } ?: return // todo: null should not be possible... right? that's only if the node doesn't exist for a VertexOnWay

        elementEditsController.add(createPoiEdit, ElementPointGeometry(positionOnWay.position), "survey", action)
        listener?.onCreatedNote(positionOnWay.position)
        arguments?.getString(ARG_FEATURE_ID)?.let {
            val initialTags: Map<String, String> = arguments?.getString(ARG_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap()
            if (!IS_SHOP_EXPRESSION.matches(element) && initialTags != element.tags)
                prefs.edit { putString(Prefs.CREATE_NODE_LAST_TAGS_FOR_FEATURE + it, Json.encodeToString(element.tags)) }
        }
    }

    companion object {
        private const val ARG_POSITION_ON_WAY = "position_on_way"
        private const val ARG_FEATURE_NAME = "feature_name"
        private const val ARG_FEATURE_ID = "feature_id"
        private const val ARG_TAGS = "tags"

        // todo:
        //  what if a node with tags is re-used (user choice!)
        //   this should also add the relevant tags to tag editor
        //   only if i want to allow this...
        //   maybe add the tags to positionOnWay?
        fun create(positionOnWay: PositionOnWay, feature: Feature?, startTags: Map<String, String>?): InsertNodeTagEditor {
            val f = InsertNodeTagEditor()
            val args = createArguments(Node(0L, positionOnWay.position), ElementPointGeometry(positionOnWay.position), null, null)
            val tags = HashMap<String, String>()
            feature?.addTags?.forEach { tags[it.key] = it.value }
            startTags?.forEach { tags[it.key] = it.value } // todo: how to handle conflicts? e.g. if user wants to insert speed table, but there is a speed bump already?
            args.putAll(bundleOf(
                ARG_POSITION_ON_WAY to Json.encodeToString(positionOnWay),
                ARG_TAGS to tags,
            ))
            feature?.let {
                args.putString(ARG_FEATURE_NAME, it.name)
                args.putString(ARG_FEATURE_ID, it.id)
                args.putString(ARG_TAGS, Json.encodeToString(it.addTags))
            }
            f.arguments = args
            return f
        }
    }
}
