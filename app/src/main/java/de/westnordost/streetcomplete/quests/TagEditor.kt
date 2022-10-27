package de.westnordost.streetcomplete.quests

import android.app.ActionBar.LayoutParams
import android.content.SharedPreferences
import android.icu.text.DateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.databinding.EditTagsBinding
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.util.ktx.copy
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.updateMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

// todo: ideas for improvements
//  opening tag editor loses state of quest below, probably because we're replacing the bottom sheet instead of adding
//   either fix it, or also close the quest below when closing the editor
//  ability to copy and paste everything (this is the only advantage of the old editor)
//   button that copies all tags into clipboard: tagsList.joinToString("\n")
//   and one that pastes clipboard into tags: newTags.putAll(clipboard.toTags())
//    overwrite existing tags and add others, don't delete
//  undo button, for undoing delete or paste (and maybe other changes? but will not work well with typing)
//  don't depend on that TagEditor.isShowing and TagEditor.changes in companion object
//  help when entering tags
//   suggestions? e.g. common keys, or common values for that key, or last typed keys / values for that key
//    how to show?
//     autocompletetextview is the dropdown thing, but that might not always be convenient
//     can i use the keyboard suggestions? should work, see e.g. TYPE_TEXT_FLAG_AUTO_COMPLETE
//      if this works, it could also be done for tree quest

open class TagEditor : Fragment(), IsCloseableBottomSheet {
    // too different from abstractbottomsheetfragment... though it would be nice to use it...
    private var _binding: EditTagsBinding? = null
    protected val binding: EditTagsBinding get() = _binding!!
    private var updateQuestsJob: Job? = null
    private var minBottomInset = Int.MAX_VALUE

    private val osmQuestController: OsmQuestController by inject()
    protected val prefs: SharedPreferences by inject()
    protected val elementEditsController: ElementEditsController by inject()

    protected lateinit var originalElement: Element
    protected lateinit var element: Element // element with adjusted tags and edit date
    protected val newTags = ConcurrentHashMap<String, String>()
    protected val tagList = mutableListOf<Pair<String, String>>() // sorted list of tags from newTags, need to keep in sync manually
    private lateinit var geometry: ElementGeometry

    // those 2 are lazy because resources require context to be initialized
    private val questIconWidth by lazy { (resources.displayMetrics.density * 56 + 0.5f).toInt() }
    private val questIconParameters by lazy { LinearLayout.LayoutParams(questIconWidth, questIconWidth).apply {
        val margin = (resources.displayMetrics.density * 2 + 0.5f).toInt()
        setMargins(margin, margin, margin, margin)
    } }

    private val listener: AbstractOsmQuestForm.Listener? get() = parentFragment as? AbstractOsmQuestForm.Listener ?: activity as? AbstractOsmQuestForm.Listener
    private lateinit var deferredQuests: Deferred<List<OsmQuest>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        originalElement = Json.decodeFromString(args.getString(ARG_ELEMENT)!!)
        geometry = Json.decodeFromString(args.getString(ARG_GEOMETRY)!!)
        newTags.putAll(originalElement.tags)
        tagList.addAll(newTags.toList().sortedBy { it.first })
        element = originalElement.copy(tags = newTags, timestampEdited = nowAsEpochMilliseconds()) // we don't want resurvey quests, user can just edit tag or delete and get quest again
        showingTagEditor = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // definitely worth calling early! because it should be finished once we want to fill the quest list (in most cases)
        deferredQuests = viewLifecycleScope.async(Dispatchers.IO) {
            // create quests if we have dynamic quest creation on or a new POI, otherwise just load from db
            // this is much faster, but actually may contain resurvey quests... whatever (for now)
            if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false) || element.id == 0L) osmQuestController.createNonPoiQuestsForElement(element, geometry)
            else osmQuestController.getAllVisibleInBBox(geometry.center.enclosingBoundingBox(0.01), null, true)
                .filter { it.elementType == element.type && it.elementId == element.id && it.type.dotColor == "no" }
        }
        _binding = EditTagsBinding.inflate(inflater, container, false)
        inflater.inflate(R.layout.edit_tags, binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // move if keyboard is shown
        // interestingly this is called several times on showing/hiding keyboard
        view.respectSystemInsets {
            val keyboardShowing = minBottomInset < it.bottom
            binding.editorContainer.updateMargins(bottom = it.bottom)
            if (keyboardShowing) {
                // setting layout params or requestLayout is unneeded? though some sources say it is...
                binding.questsGrid.layoutParams.height = questIconWidth
                binding.lastEditDate.layoutParams.height = 0
            } else {
                binding.questsGrid.layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
                binding.lastEditDate.layoutParams.height = LayoutParams.WRAP_CONTENT
            }
            minBottomInset = min(it.bottom, minBottomInset)

        }
        val date = Date(originalElement.timestampEdited)
        val dateText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            DateFormat.getDateTimeInstance().format(date)
        else
            date.toString()
        binding.lastEditDate.text = resources.getString(R.string.tag_editor_last_edited, dateText)
        binding.lastEditDate.layoutParams.height = LayoutParams.WRAP_CONTENT


        // fill recyclerview and quests view
        binding.editTags.layoutManager = LinearLayoutManager(requireContext())
        binding.editTags.adapter = EditTagsAdapter(tagList, newTags) {
            viewLifecycleScope.launch(Dispatchers.IO) { updateQuests(1000) }
            showOk()
        }.apply { setHasStableIds(true) }

        binding.okButton.setOnClickListener {
            if (!tagsChangedAndOk()) return@setOnClickListener // for now keep the button visible and just do nothing if invalid
            showingTagEditor = false
            viewLifecycleScope.launch { applyEdit() } // tags are updated, and the different timestamp should not matter
        }

        binding.questsGrid.columnCount = resources.displayMetrics.widthPixels / (questIconWidth + 3 * (resources.displayMetrics.density * 2 + 0.5f).toInt()) // last part is for the margins of icons and view
        binding.questsGrid.addView(ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_add_24dp)
            setBackgroundColor(resources.getColor(R.color.background))
            layoutParams = questIconParameters
            setOnClickListener {
                if (tagList.lastOrNull() == emptyEntry) return@setOnClickListener
                tagList.add(emptyEntry)
                binding.editTags.adapter?.notifyItemInserted(tagList.lastIndex)
                binding.editTags.scrollToPosition(tagList.lastIndex)
                showOk()
            }
        })

        val quests = runBlocking { deferredQuests.await() } // should still be fine, doesn't take that long
        quests.forEach { q ->
            val icon = ImageView(requireContext()).apply { setImageResource(q.type.icon) }
            icon.layoutParams = questIconParameters
            icon.tag = q.type.name
            icon.setOnClickListener { showQuest(q) }
            binding.questsGrid.addView(icon)
        }
        showOk()
    }

    private fun showQuest(quest: OsmQuest) {
        val f = quest.type.createForm()
        if (f.arguments == null) f.arguments = bundleOf()
        val initialMapRotation = arguments?.getFloat(ARG_MAP_ROTATION) ?: 0f
        val initialMapTilt = arguments?.getFloat(ARG_MAP_TILT) ?: 0f
        val args = AbstractQuestForm.createArguments(quest.key, quest.type, quest.geometry, initialMapRotation, initialMapTilt)
        f.requireArguments().putAll(args)
        val osmArgs = AbstractOsmQuestForm.createArguments(element)
        f.requireArguments().putAll(osmArgs)
        activity?.currentFocus?.hideKeyboard()
        parentFragmentManager.commit { // in parent fragment, because this handles the callbacks
            add(id, f, null) // add the quest instead of replacing tag editor, so that changes aren't lost
            addToBackStack(null)
            // but hide tag editor while quest is shown
            binding.editTags.visibility = View.GONE
            binding.questsGrid.visibility = View.GONE
            binding.okButton.visibility = View.GONE
            binding.lastEditDate.visibility = View.GONE
        }

        viewLifecycleScope.launch(Dispatchers.IO) {
            // this thread waits while the quest form is showing
            // quest sets changes when answering, but does nothing else
            // when the quest form is closed without answer, changes are set to empty (in main fragment)
            changes = null
            while (changes == null) { delay(50) }
            val ch = changes
            ch?.applyTo(newTags) // apply before showOk() (though it works also when applying after, maybe onClickClose stuff gets executed in a different thread
            requireActivity().runOnUiThread {
                binding.editTags.visibility = View.VISIBLE
                binding.questsGrid.visibility = View.VISIBLE
                binding.lastEditDate.visibility = View.VISIBLE
                showOk()
                f.onClickClose {
                    parentFragmentManager.popBackStack()
                    changes = null
                }
            }
            if (ch?.isEmpty() != false) // usually changes is set to null after this check, but better be safe
                return@launch
            tagList.clear()
            tagList.addAll(newTags.toList())
            tagList.sortBy { it.first }
            if (tagList.contains(emptyEntry)) {
                tagList.remove(emptyEntry)
                tagList.add(emptyEntry)
            }
            requireActivity().runOnUiThread {
                // data set changes include tags that were changed manually before opening the quest, so it really may be everything different!
                binding.editTags.adapter?.notifyDataSetChanged()
                // remove the quest immediately, because answering again may crash
                binding.questsGrid.removeView(binding.questsGrid.findViewWithTag<ImageView>(quest.type.name))
            }
            updateQuests()
        }
    }

    protected open fun applyEdit() {
        val builder = StringMapChangesBuilder(originalElement.tags)
        for (key in originalElement.tags.keys) {
            if (!element.tags.containsKey(key))
                builder.remove(key)
        }
        for ((key, value) in element.tags) {
            if (originalElement.tags[key] == value) continue
            builder[key] = value
        }

        val action = UpdateElementTagsAction(builder.create())
        if (prefs.getBoolean(Prefs.CLOSE_FORM_IMMEDIATELY_AFTER_SOLVING, false) && !prefs.getBoolean(Prefs.SHOW_NEXT_QUEST_IMMEDIATELY, false)) {
            listener?.onEdited(tagEdit, element, geometry)
            viewLifecycleScope.launch(Dispatchers.IO) { elementEditsController.add(tagEdit, originalElement, geometry, "survey", action) }
        } else {
            elementEditsController.add(tagEdit, originalElement, geometry, "survey", action)
            listener?.onEdited(tagEdit, element, geometry)
        }
    }

    private fun tagsChangedAndOk(): Boolean =
        newTags != originalElement.tags
            && !newTags.containsKey("")
            && !newTags.containsValue("")
            && newTags.keys.all { it.length < 255 }
            && newTags.values.all { it.length < 255 }
            && newTags.isNotEmpty() // though this could be allowed...
            && newTags.keys.none { problematicKeyCharacters.containsMatchIn(it) }

    private fun showOk() = requireActivity().runOnUiThread { if (tagsChangedAndOk()) binding.okButton.popIn() else binding.okButton.popOut() }

    private suspend fun updateQuests(waitMillis: Long = 0) {
        updateQuestsJob?.cancel()
        delay(waitMillis)
        updateQuestsJob = viewLifecycleScope.launch(Dispatchers.IO) {
            val q = osmQuestController.createNonPoiQuestsForElement(element, geometry).map { q ->
                val icon = ImageView(requireContext()).apply { setImageResource(q.type.icon) }
                icon.layoutParams = questIconParameters
                icon.tag = q.type.name
                icon.setOnClickListener { showQuest(q) }
                if (!isActive) return@launch
                icon
            }
            if (!isActive) return@launch
            requireActivity().runOnUiThread {
                // form might be closed while quests were created, so we better not crash on binding == null
                _binding?.questsGrid?.removeViews(1, binding.questsGrid.childCount - 1)
                q.forEach { _binding?.questsGrid?.addView(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickClose(onConfirmed: () -> Unit) {
        if (originalElement.tags == newTags) {
            showingTagEditor = false
            return onConfirmed()
        }
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.confirmation_discard_title)
            .setPositiveButton(R.string.confirmation_discard_positive) { _, _ ->
                showingTagEditor = false
                onConfirmed()
            }
            .setNegativeButton(R.string.short_no_answer_on_button, null)
            .show()
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean = false

    companion object {
        private const val ARG_ELEMENT = "element"
        private const val ARG_GEOMETRY = "geometry"
        private const val ARG_MAP_ROTATION = "map_rotation"
        private const val ARG_MAP_TILT = "map_tilt"

        fun createArguments(element: Element, geometry: ElementGeometry, rotation: Float?, tilt: Float?) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element),
            ARG_GEOMETRY to Json.encodeToString(geometry),
            ARG_MAP_ROTATION to rotation,
            ARG_MAP_TILT to tilt
        )

        var changes: StringMapChanges? = null
        var showingTagEditor = false
    }
}

// use displaySet and dataSet: displaySet is the sorted map.toList
// editing the map directly is not so simple, because the order may change if the key is changed (actually removed and re-added)
private class EditTagsAdapter(
    private val displaySet: MutableList<Pair<String, String>>,
    private val dataSet: MutableMap<String, String>,
    private val onDataChanged: () -> Unit
) :
    RecyclerView.Adapter<EditTagsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val keyView: EditText = view.findViewById<EditText>(R.id.keyText).apply {
            doAfterTextChanged {
                val position = absoluteAdapterPosition
                val newKey = it.toString()
                // do nothing if key is unchanged, happens when editTexts are filled by adaoter
                if (displaySet[position].first == newKey) return@doAfterTextChanged
                if (dataSet.containsKey(newKey)) {
                    // don't store duplicate keys, user should rename or delete them
                    context.toast(resources.getString(R.string.tag_editor_duplicate_key, newKey), Toast.LENGTH_LONG)
                    return@doAfterTextChanged
                }
                val oldEntry = displaySet[position]
                dataSet.remove(oldEntry.first)
                val newEntry = newKey to oldEntry.second
                dataSet[newEntry.first] = newEntry.second
                displaySet[position] = newEntry
                onDataChanged()
            }
        }
        val valueView: EditText = view.findViewById<EditText>(R.id.valueText).apply {
            doAfterTextChanged {
                val position = absoluteAdapterPosition
                if (displaySet[position].second == it.toString()) return@doAfterTextChanged
                val oldEntry = displaySet[position]
                val newEntry = oldEntry.first to it.toString()
                dataSet[newEntry.first] = newEntry.second
                displaySet[position] = newEntry
                onDataChanged()
            }
        }
        val delete: ImageView = view.findViewById<ImageView>(R.id.deleteButton).apply {
            setOnClickListener {
                val position = absoluteAdapterPosition
                val oldEntry = displaySet.removeAt(position)
                dataSet.remove(oldEntry.first)
                onDataChanged()
//                notifyItemRemoved(position) // crash when editing an entry, and deleting another one right after
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_edit_tag, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.keyView.setText(displaySet[position].first)
        viewHolder.valueView.setText(displaySet[position].second)
    }

    override fun getItemCount() = displaySet.size

    override fun getItemId(position: Int) = position.toLong()
}

val tagEdit = object : ElementEditType {
    override val changesetComment get() = "Edit element"
    override val icon get() = R.drawable.ic_edit_tags
    override val title get() = R.string.quest_generic_answer_show_edit_tags
    override val wikiLink: String? get() = null
    override val name get() = "TagEditor"
}

private val emptyEntry = "" to ""

// characters that should not be in keys, see https://taginfo.openstreetmap.org/reports/characters_in_keys
private val problematicKeyCharacters = "[\\s=+/&<>;'\"?%#@,\\\\]".toRegex()
