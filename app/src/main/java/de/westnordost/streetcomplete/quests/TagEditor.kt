package de.westnordost.streetcomplete.quests

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.graphics.Paint
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
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.data.osm.edits.update_tags.createChanges
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.databinding.EditTagsBinding
import de.westnordost.streetcomplete.overlays.custom.CustomOverlayForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.InsertNodeTagEditor
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.util.EditTagsAdapter
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.copy
import de.westnordost.streetcomplete.util.ktx.geometryType
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import de.westnordost.streetcomplete.util.ktx.updateMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.view.checkIsSurvey
import de.westnordost.streetcomplete.view.confirmIsSurvey
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

// todo: ideas for improvements
//  ability to copy and paste everything
//   see https://stackoverflow.com/questions/19177231/android-copy-paste-from-clipboard-manager
//   button that copies all tags into clipboard: tagsList.joinToString("\n") { "${it.first} = ${it.second}" }
//   and one that pastes clipboard into tags: newTags.putAll(clipboard.toTags())
//    only show button if clipboard contains data that can be parsed to tags
//  undo button, for undoing delete or paste (and maybe other changes? but will not work well with typing)

open class TagEditor : Fragment(), IsCloseableBottomSheet {
    private var _binding: EditTagsBinding? = null
    protected val binding: EditTagsBinding get() = _binding!!
    private var updateQuestsJob: Job? = null
    private var minBottomInset = Int.MAX_VALUE

    private val osmQuestController: OsmQuestController by inject()
    protected val prefs: ObservableSettings by inject()
    protected val elementEditsController: ElementEditsController by inject()
    private val featureDictionary: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    protected val mapDataSource: MapDataWithEditsSource by inject()
    private val externalSourceQuestController: ExternalSourceQuestController by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    protected val recentLocationStore: RecentLocationStore by inject()

    protected lateinit var originalElement: Element
    protected lateinit var element: Element // element with adjusted tags and edit date
    protected val newTags = ConcurrentHashMap<String, String>()
    protected val tagList = mutableListOf<Pair<String, String>>() // sorted list of tags from newTags, need to keep in sync manually
    private lateinit var geometry: ElementGeometry
    protected var questKey: QuestKey? = null
    protected var editTypeName: String? = null
    private val keyboardButton by lazy { ImageView(requireContext()).apply {
        setImageResource(android.R.drawable.ic_menu_edit) // is there no nice default keyboard drawable?
        scaleX = 0.8f
        scaleY = 0.8f
        layoutParams = questIconParameters
        setOnClickListener { requireActivity().currentFocus?.showKeyboard() }
    } }

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
        questKey = args.getString(ARG_QUEST_KEY)?.let { Json.decodeFromString(it) }
        editTypeName = args.getString(ARG_EDIT_TYPE_NAME)
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
            if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false) || element.id == 0L)
                osmQuestController.createNonPoiQuestsForElement(element, geometry)
            else
                osmQuestController.getAllVisibleInBBox(geometry.center.enclosingBoundingBox(0.01), null, true)
                .filter { it.elementType == element.type && it.elementId == element.id && it.type.dotColor == null }
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
            val binding = _binding ?: return@respectSystemInsets
            binding.editorContainer.updateMargins(bottom = it.bottom)
            if (keyboardShowing) {
                // setting layout params or requestLayout is unneeded? though some sources say it is...
                binding.questsGrid.layoutParams.height = questIconWidth
                binding.elementInfo.layoutParams.height = 0
            } else {
                binding.questsGrid.layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
                binding.elementInfo.layoutParams.height = LayoutParams.WRAP_CONTENT
            }
            minBottomInset = min(it.bottom, minBottomInset)
            if (keyboardShowing || activity?.currentFocus == null)
                binding.questsGrid.removeView(keyboardButton)
            else if (binding.questsGrid.size > 1 && binding.questsGrid[1] != keyboardButton)
                binding.questsGrid.addView(keyboardButton, 1)
        }
        val date = Date(originalElement.timestampEdited)
        val dateText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            DateFormat.getDateTimeInstance().format(date)
        else
            date.toString()
        binding.elementInfo.text = resources.getString(R.string.tag_editor_last_edited, dateText)
        binding.elementInfo.layoutParams.height = LayoutParams.WRAP_CONTENT
        if (element.id > 0) {
            binding.elementInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.link))
            binding.elementInfo.paintFlags = binding.elementInfo.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.elementInfo.setOnClickListener {
                val url = "https://www.openstreetmap.org/${element.type.name.lowercase()}/${element.id}/history"
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.open_url)
                    .setMessage(url)
                    .setPositiveButton(android.R.string.ok) { _, _ -> openUri(url) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }

        // fill recyclerview and quests view
        binding.editTags.layoutManager = LinearLayoutManager(requireContext())
        val geometryType = if (element is Node && (this is InsertNodeTagEditor || mapDataSource.getWaysForNode(element.id).isNotEmpty())) GeometryType.VERTEX
            else element.geometryType
        binding.editTags.adapter = EditTagsAdapter(tagList, newTags, geometryType, featureDictionary.value, requireContext(), prefs) {
            viewLifecycleScope.launch(Dispatchers.IO) { updateQuests(750) }
            showOk()
        }.apply { setHasStableIds(true) }

        binding.okButton.setOnClickListener {
            if (!tagsChangedAndOk()) return@setOnClickListener // for now keep the button visible and just do nothing if invalid
            newTags.keys.removeAll { it.isBlank() } // if value is not blank ok button is disabled, so we discard only empty lines here
            newTags.filterKeys { it != it.trim() }.forEach {
                newTags.remove(it.key)
                newTags[it.key.trim()] = it.value
            } // trim keys
            newTags.filterValues { it != it.trim() }.forEach { newTags[it.key] = it.value.trim() } // trim values
            showingTagEditor = false
            viewLifecycleScope.launch { applyEdit() } // tags are updated, and the different timestamp should not matter
        }

        binding.questsGrid.columnCount = resources.displayMetrics.widthPixels / (questIconWidth + 3 * (resources.displayMetrics.density * 2 + 0.5f).toInt()) // last part is for the margins of icons and view
        // add "new tag" button
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
        binding.editTags.viewTreeObserver.addOnGlobalFocusChangeListener { _, _ ->
            val binding = _binding ?: return@addOnGlobalFocusChangeListener
            if (activity?.currentFocus == null || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && WindowInsetsCompat.toWindowInsetsCompat(binding.root.rootWindowInsets).isVisible(WindowInsetsCompat.Type.ime())
            ))
                binding.questsGrid.removeView(keyboardButton)
            else if (binding.questsGrid.size < 2 || binding.questsGrid[1] != keyboardButton)
                binding.questsGrid.addView(keyboardButton, 1)
        }

        if (element.id == 0L) {
            val previousTagsForFeature: Map<String, String>? = try { featureDictionary.value
                .getByTags(
                    tags = newTags,
                    isSuggestion = false,
                    languages = getLanguagesForFeatureDictionary(resources.configuration)
                ).firstOrNull()
                ?.let { prefs.getString(Prefs.CREATE_NODE_LAST_TAGS_FOR_FEATURE + it, "") }
                ?.let { Json.decodeFromString(it) }
            } catch (e: Exception) { null }
            if (previousTagsForFeature?.isNotEmpty() == true && previousTagsForFeature != newTags)
                binding.questsGrid.addView(ImageView(requireContext()).apply {
                    setImageResource(R.drawable.ic_undo_24dp)
                    scaleX = -0.7f // mirror to have a redo icon
                    scaleY = 0.7f // and make a little smaller, looks weird otherwise
                    layoutParams = questIconParameters
                    setOnClickListener {
                        previousTagsForFeature.forEach { newTags[it.key] = it.value }
                        binding.editTags.adapter?.notifyDataSetChanged()
                        tagList.clear()
                        tagList.addAll(newTags.toList().sortedBy { it.first })
                        viewLifecycleScope.launch(Dispatchers.IO) { updateQuests(0) }
                        showOk()
                    }
                })
        }

        viewLifecycleScope.launch(Dispatchers.IO) { waitForQuests() }
        focusKey()
        showOk()
    }

    private suspend fun waitForQuests() {
        val quests = deferredQuests.await()
        activity?.runOnUiThread { quests.forEach { q ->
            val icon = ImageView(requireContext())
            icon.setImageResource(q.type.icon)
            icon.layoutParams = questIconParameters
            icon.tag = q.type.name
            icon.setOnClickListener { showQuest(q) }
            _binding?.questsGrid?.addView(icon)
        } }
    }

    // focus value field of a key if desired, create entry if it doesn't exist
    private fun focusKey() {
        val focus = CustomOverlayForm.focusKey?.takeIf { it.startsWith("!") } ?: return
        CustomOverlayForm.focusKey = null
        val tag = focus.substringAfterLast("!")
        if (!newTags.containsKey(tag)) {
            tagList.add(tag to "") // don't add it to newTags, this will happen if the user changes anything
            binding.editTags.adapter?.notifyItemInserted(tagList.lastIndex)
        }
        // select that field
        val position = tagList.indexOfLast { it.first == tag }
        viewLifecycleScope.launch(Dispatchers.IO) {
            delay(30)
            activity?.runOnUiThread {
                val view = (binding.editTags.findViewHolderForAdapterPosition(position) as? EditTagsAdapter.ViewHolder)?.valueView ?: return@runOnUiThread
                view.requestFocus()
                if (newTags.containsKey(tag))
                    view.selectAll()
            }
        }
    }

    @UiThread
    @SuppressLint("NotifyDataSetChanged")
    private fun showQuest(quest: OsmQuest) {
        val f = quest.type.createForm()
        if (f.arguments == null) f.arguments = bundleOf()
        val initialMapRotation = arguments?.getDouble(ARG_MAP_ROTATION) ?: 0.0
        val initialMapTilt = arguments?.getDouble(ARG_MAP_TILT) ?: 0.0
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
        binding.elementInfo.visibility = View.GONE

        viewLifecycleScope.launch {
            // this thread waits while the quest form is showing
            // quest sets changes when answering, but does nothing else
            // when the quest form is closed without answer, changes are set to empty (in main fragment)
            changes = null
            withContext(Dispatchers.IO) { while (changes == null) { delay(50) } }
            val ch = changes
            binding.editTags.visibility = View.VISIBLE
            binding.questsGrid.visibility = View.VISIBLE
            binding.elementInfo.visibility = View.VISIBLE
            f.onClickClose {
                parentFragmentManager.popBackStack()
                changes = null
            }
            if (ch?.isEmpty() != false) { // usually changes is set to null after this check, but better be safe as the order is not strict
                showOk()
                return@launch
            }
            ch.applyTo(newTags) // apply before showOk()
            showOk()
            tagList.clear()
            tagList.addAll(newTags.toList())
            tagList.sortBy { it.first }
            if (tagList.contains(emptyEntry)) {
                tagList.remove(emptyEntry)
                tagList.add(emptyEntry)
            }
            binding.editTags.adapter?.notifyDataSetChanged()
            // remove the quest immediately, because answering again may crash
            binding.questsGrid.removeView(binding.questsGrid.findViewWithTag<ImageView>(quest.type.name))
            withContext(Dispatchers.IO) { updateQuests() }
        }
    }

    protected open suspend fun applyEdit() {
        val isSurvey = checkIsSurvey(geometry, recentLocationStore.get())
        if (!isSurvey && !confirmIsSurvey(requireContext()))
            return
        val builder = element.tags.createChanges(originalElement.tags)

        val action = UpdateElementTagsAction(originalElement, builder.create())
        val questKey = questKey
        val editType = when {
            questKey is ExternalSourceQuestKey -> externalSourceQuestController.getQuestType(questKey)!!
            editTypeName != null -> (overlayRegistry.getByName(editTypeName!!) ?: questTypeRegistry.getByName(editTypeName!!)) as ElementEditType
            else -> tagEdit
        }
        if (questKey is OsmQuestKey && prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
            OsmQuestController.lastAnsweredQuestKey = questKey
        // always use "survey", because either it's tag editor or some external quest that's most like supposed to allow this
        elementEditsController.add(editType, geometry, "survey", action, isSurvey, questKey)
        listener?.onEdited(editType, geometry)
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
            activity?.runOnUiThread {
                // form might be closed while quests were created, so we better not crash on binding == null
                val binding = _binding ?: return@runOnUiThread
                val viewsToKeep = if (binding.questsGrid.size > 1 && binding.questsGrid[1] == keyboardButton) 2
                    else 1
                binding.questsGrid.removeViews(viewsToKeep, binding.questsGrid.childCount - viewsToKeep) // remove all quest views
                q.forEach { binding.questsGrid.addView(it) }
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
        private const val ARG_EDIT_TYPE_NAME = "edit_type_name"

        fun createArguments(element: Element, geometry: ElementGeometry, rotation: Double?, tilt: Double?, questKey: QuestKey? = null, editTypeName: String? = null) = bundleOf(
            ARG_ELEMENT to Json.encodeToString(element),
            ARG_GEOMETRY to Json.encodeToString(geometry),
            ARG_MAP_ROTATION to rotation,
            ARG_MAP_TILT to tilt,
            ARG_QUEST_KEY to Json.encodeToString(questKey),
            ARG_EDIT_TYPE_NAME to editTypeName
        )

        var changes: StringMapChanges? = null
        var showingTagEditor = false
    }
}


val tagEdit = object : ElementEditType {
    override val changesetComment = "Edit element"
    override val icon = R.drawable.ic_edit_tags
    override val title = R.string.quest_generic_answer_show_edit_tags
    override val wikiLink: String? = null
    override val name = "TagEdit"
}

private val emptyEntry = "" to ""

// characters that should not be in keys, see https://taginfo.openstreetmap.org/reports/characters_in_keys
private val problematicKeyCharacters = "[\\s=+/&<>;'\"?%#@,\\\\]".toRegex()
