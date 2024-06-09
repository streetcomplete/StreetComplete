package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.view.checkIsSurvey
import de.westnordost.streetcomplete.view.confirmIsSurvey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Fragment that lets the user split an OSM way */
class InsertNodeTagEditor : TagEditor() {

    private val listener: CreateNoteFragment.Listener? get() = parentFragment as? CreateNoteFragment.Listener ?: activity as? CreateNoteFragment.Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newTags.putAll(arguments?.getString(ARG_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap())
        tagList.clear()
        tagList.addAll(newTags.toList())
        tagList.sortBy { it.first }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.elementInfo.text = arguments?.getString(ARG_FEATURE_NAME) ?: ""
    }

    override suspend fun applyEdit() {
        val args = requireArguments()
        val positionOnWay: PositionOnWay = Json.decodeFromString(args.getString(ARG_POSITION_ON_WAY)!!)
        val isSurvey = checkIsSurvey(ElementPointGeometry(positionOnWay.position), recentLocationStore.get())
        if (!isSurvey && !confirmIsSurvey(requireContext()))
            return
        val action = createNodeAction(positionOnWay, mapDataSource) { changeBuilder ->
            changeBuilder.keys.forEach { if (it !in element.tags) changeBuilder.remove(it) } // remove tags, only relevant if there are startTags
            element.tags.forEach { changeBuilder[it.key] = it.value } // and add changes
        } ?: return

        elementEditsController.add(addNodeEdit, ElementPointGeometry(positionOnWay.position), "survey", action, isSurvey)
        listener?.onCreatedNote(positionOnWay.position)
        arguments?.getString(ARG_FEATURE_ID)?.let {
            val initialTags: Map<String, String> = arguments?.getString(ARG_TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap()
            if (!element.isPlace() && initialTags != element.tags)
                prefs.putString(Prefs.CREATE_NODE_LAST_TAGS_FOR_FEATURE + it, Json.encodeToString(element.tags))
        }
    }

    companion object {
        private const val ARG_POSITION_ON_WAY = "position_on_way"
        private const val ARG_FEATURE_NAME = "feature_name"
        private const val ARG_FEATURE_ID = "feature_id"
        private const val ARG_TAGS = "tags"

        fun create(positionOnWay: PositionOnWay, feature: Feature?, startTags: Map<String, String> = emptyMap()): InsertNodeTagEditor {
            val f = InsertNodeTagEditor()
            val args = createArguments(Node(0L, positionOnWay.position, startTags), ElementPointGeometry(positionOnWay.position), null, null)
            val tags = HashMap<String, String>()
            startTags.forEach { tags[it.key] = it.value } // only relevant if a an existing node with non-empty tags is re-used
            feature?.addTags?.forEach { tags[it.key] = it.value }
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
