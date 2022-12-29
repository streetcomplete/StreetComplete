package de.westnordost.streetcomplete.quests

import android.app.ActionBar.LayoutParams
import android.content.SharedPreferences
import android.icu.text.DateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.databinding.EditTagsBinding
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.util.EditTagsAdapter
import de.westnordost.streetcomplete.util.ktx.copy
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.FutureTask
import kotlin.math.min

// todo: ideas for improvements
//  ability to copy and paste everything (this is the only advantage of the old editor)
//   button that copies all tags into clipboard: tagsList.joinToString("\n")
//   and one that pastes clipboard into tags: newTags.putAll(clipboard.toTags())
//    overwrite existing tags and add others, don't delete
//  undo button, for undoing delete or paste (and maybe other changes? but will not work well with typing)
//  don't depend on that TagEditor.isShowing and TagEditor.changes in companion object

open class TagEditor : Fragment(), IsCloseableBottomSheet {
    private var _binding: EditTagsBinding? = null
    protected val binding: EditTagsBinding get() = _binding!!
    private var updateQuestsJob: Job? = null
    private var minBottomInset = Int.MAX_VALUE

    private val osmQuestController: OsmQuestController by inject()
    protected val prefs: SharedPreferences by inject()
    protected val elementEditsController: ElementEditsController by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val mapDataSource: MapDataWithEditsSource by inject()

    protected lateinit var originalElement: Element
    protected lateinit var element: Element // element with adjusted tags and edit date
    protected val newTags = ConcurrentHashMap<String, String>()
    protected val tagList = mutableListOf<Pair<String, String>>() // sorted list of tags from newTags, need to keep in sync manually
    private lateinit var geometry: ElementGeometry
    protected var questKey: QuestKey? = null

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
        questKey = arguments?.getString(ARG_QUEST_KEY)?.let { Json.decodeFromString(it) }
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
        binding.editTags.adapter = EditTagsAdapter(tagList, newTags, featureDictionaryFuture.get(), requireContext(), prefs) {
            viewLifecycleScope.launch(Dispatchers.IO) { updateQuests(1000) }
            showOk()
        }.apply { setHasStableIds(true) }

        binding.okButton.setOnClickListener {
            if (!tagsChangedAndOk()) return@setOnClickListener // for now keep the button visible and just do nothing if invalid
            newTags.keys.removeAll { it.isBlank() } // if value is not blank ok button is disabled, so we discard only empty lines here
            showingTagEditor = false
            viewLifecycleScope.launch { applyEdit() } // tags are updated, and the different timestamp should not matter
        }

        binding.questsGrid.columnCount = resources.displayMetrics.widthPixels / (questIconWidth + 3 * (resources.displayMetrics.density * 2 + 0.5f).toInt()) // last part is for the margins of icons and view
        binding.questsGrid.addView(ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_add_24dp)
            setBackgroundColor(ContextCompat.getColor(context, R.color.background))
            layoutParams = questIconParameters
            setOnClickListener {
                if (tagList.lastOrNull() == emptyEntry) return@setOnClickListener
                tagList.add(emptyEntry)
                newTags[""] = ""
                binding.editTags.adapter?.notifyItemInserted(tagList.lastIndex)
                // clearing focus is necessary to avoid crash java.lang.IllegalArgumentException: Scrapped or attached views may not be recycled. isScrap:false isAttached:true androidx.recyclerview.widget.RecyclerView
                activity?.currentFocus?.clearFocus()
                binding.editTags.scrollToPosition(tagList.lastIndex)
                binding.editTags.post {
                    // focus new view (does not change keyboard state)
                    // use post to avoid NPE because view does not exist: https://stackoverflow.com/a/54751851
                    (binding.editTags.findViewHolderForAdapterPosition(tagList.lastIndex) as? EditTagsAdapter.ViewHolder)?.keyView?.requestFocus()
                }
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
        }
        // hide tag editor while quest is shown
        binding.editTags.visibility = View.GONE
        binding.questsGrid.visibility = View.GONE
        binding.okButton.visibility = View.GONE
        binding.lastEditDate.visibility = View.GONE

        viewLifecycleScope.launch {
            // this thread waits while the quest form is showing
            // quest sets changes when answering, but does nothing else
            // when the quest form is closed without answer, changes are set to empty (in main fragment)
            changes = null
            withContext(Dispatchers.IO) { while (changes == null) { delay(50) } }
            val ch = changes
            binding.editTags.visibility = View.VISIBLE
            binding.questsGrid.visibility = View.VISIBLE
            binding.lastEditDate.visibility = View.VISIBLE
            f.onClickClose {
                parentFragmentManager.popBackStack()
                changes = null
            }
            if (ch?.isEmpty() != false) // usually changes is set to null after this check, but better be safe as the order is not strict
                return@launch
            ch.applyTo(newTags) // apply before showOk()
            showOk()
            tagList.clear()
            tagList.addAll(newTags.toList())
            tagList.sortBy { it.first }
            if (tagList.contains(emptyEntry)) {
                tagList.remove(emptyEntry)
                tagList.add(emptyEntry)
            }
            // data set changes include tags that were changed manually before opening the quest, so it really may be everything different!
            binding.editTags.adapter?.notifyDataSetChanged()
            // remove the quest immediately, because answering again may crash
            binding.questsGrid.removeView(binding.questsGrid.findViewWithTag<ImageView>(quest.type.name))
            withContext(Dispatchers.IO) { updateQuests() }
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
            viewLifecycleScope.launch(Dispatchers.IO) { elementEditsController.add(tagEdit, originalElement, geometry, "survey", action, questKey) }
        } else {
            elementEditsController.add(tagEdit, originalElement, geometry, "survey", action, questKey)
            listener?.onEdited(tagEdit, element, geometry)
        }
    }

    private fun tagsChangedAndOk(): Boolean =
        originalElement.tags != HashMap<String, String>().apply {
            putAll(newTags)
            entries.removeAll { it.key.isBlank() && it.value.isBlank() }
        }
            && newTags.none {
                (it.key.isBlank() && it.value.isNotBlank()) || (it.value.isBlank() && it.key.isNotBlank())
            }
            && newTags.keys.all { it.length < 255 }
            && newTags.values.all { it.length < 255 }
            && (newTags.isNotEmpty() || mapDataSource.getWaysForNode(originalElement.id).isNotEmpty()) // allow deleting all tags if node is part of a way
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
        private const val ARG_QUEST_KEY = "quest_key"

        fun createArguments(element: Element, geometry: ElementGeometry, rotation: Float?, tilt: Float?, questKey: QuestKey? = null) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element),
            ARG_GEOMETRY to Json.encodeToString(geometry),
            ARG_MAP_ROTATION to rotation,
            ARG_MAP_TILT to tilt,
            ARG_QUEST_KEY to Json.encodeToString(questKey)
        )

        var changes: StringMapChanges? = null
        var showingTagEditor = false
    }
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
