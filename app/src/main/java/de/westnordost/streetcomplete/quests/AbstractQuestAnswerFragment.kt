package de.westnordost.streetcomplete.quests

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.viewbinding.ViewBinding
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.databinding.ButtonPanelButtonBinding
import de.westnordost.streetcomplete.databinding.FragmentQuestAnswerBinding
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.concurrent.FutureTask
import javax.inject.Inject

/** Abstract base class for any bottom sheet with which the user answers a specific quest(ion)  */
abstract class AbstractQuestAnswerFragment<T> :
    AbstractBottomSheetFragment(), IsShowingQuestDetails, IsLockable {

    private var _binding: FragmentQuestAnswerBinding? = null
    private val binding get() = _binding!!

    protected var otherAnswersButton: TextView? = null
    private set

    override val bottomSheetContainer get() = binding.bottomSheetContainer
    override val bottomSheet get() = binding.bottomSheet
    override val scrollViewChild get() = binding.scrollViewChild
    override val bottomSheetTitle get() = binding.speechBubbleTitleContainer
    override val bottomSheetContent get() = binding.speechbubbleContentContainer
    override val floatingBottomView get() = binding.okButton
    override val backButton get() = binding.closeButton

    protected val scrollView: NestedScrollView get() = binding.scrollView

    // dependencies
    private val countryInfos: CountryInfos
    private val questTypeRegistry: QuestTypeRegistry
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>

    private var _countryInfo: CountryInfo? = null // lazy but resettable because based on lateinit var
        get() {
            if(field == null) {
                val latLon = elementGeometry.center
                field = countryInfos.get(latLon.longitude, latLon.latitude)
            }
            return field
        }
    protected val countryInfo get() = _countryInfo!!

    protected val featureDictionary: FeatureDictionary get() = featureDictionaryFuture.get()

    // passed in parameters
    override lateinit var questKey: QuestKey
    protected lateinit var elementGeometry: ElementGeometry private set
    private lateinit var questType: QuestType<T>
    private var initialMapRotation = 0f
    private var initialMapTilt = 0f
    protected var osmElement: Element? = null
        private set

    private var currentContext = WeakReference<Context>(null)
    private var currentCountryContext: ContextWrapper? = null

    private val englishResources: Resources
        get() {
            val conf = Configuration(resources.configuration)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = super.requireContext().createConfigurationContext(conf)
            return localizedContext.resources
        }

    private var startedOnce = false

    override var locked: Boolean = false
        set(value) {
            field = value
            binding.glassPane.isGone = !locked
        }

    // overridable by child classes
    open val contentLayoutResId: Int? = null
    open val buttonPanelAnswers = listOf<AnswerItem>()
    open val otherAnswers = listOf<AnswerItem>()
    open val contentPadding = true

    interface Listener {
        /** Called when the user answered the quest with the given id. What is in the bundle, is up to
         * the dialog with which the quest was answered */
        fun onAnsweredQuest(questKey: QuestKey, answer: Any)

        /** Called when the user chose to leave a note instead */
        fun onComposeNote(questKey: QuestKey, questTitle: String)

        /** Called when the user chose to split the way */
        fun onSplitWay(osmQuestKey: OsmQuestKey)

        /** Called when the user chose to skip the quest */
        fun onSkippedQuest(questKey: QuestKey)

        /** Called when the node shall be deleted */
        fun onDeletePoiNode(osmQuestKey: OsmQuestKey)

        /** Called when a new feature has been selected for an element (a shop of some kind) */
        fun onReplaceShopElement(osmQuestKey: OsmQuestKey, tags: Map<String, String>)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        val fields = InjectedFields()
        Injector.applicationComponent.inject(fields)
        countryInfos = fields.countryInfos
        featureDictionaryFuture = fields.featureDictionaryFuture
        questTypeRegistry = fields.questTypeRegistry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        questKey = Json.decodeFromString(args.getString(ARG_QUEST_KEY)!!)
        osmElement = args.getString(ARG_ELEMENT)?.let { Json.decodeFromString(it) }
        elementGeometry = Json.decodeFromString(args.getString(ARG_GEOMETRY)!!)
        questType = questTypeRegistry.getByName(args.getString(ARG_QUESTTYPE)!!) as QuestType<T>
        initialMapRotation = args.getFloat(ARG_MAP_ROTATION)
        initialMapTilt = args.getFloat(ARG_MAP_TILT)
        _countryInfo = null // reset lazy field
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentQuestAnswerBinding.inflate(inflater, container, false)

        /* content and buttons panel should be inflated in onCreateView because in onViewCreated,
        *  subclasses may already want to access the content. */
        otherAnswersButton = ButtonPanelButtonBinding.inflate(layoutInflater, binding.buttonPanel, true).root

        contentLayoutResId?.let { setContentView(it) }
        updateButtonPanel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleLabel.text = resources.getHtmlQuestTitle(questType, osmElement, featureDictionaryFuture)

        val levelLabelText = osmElement?.let { resources.getLocationLabelString(it.tags) }
        binding.titleHintLabel.isGone = levelLabelText == null
        if (levelLabelText != null) {
            binding.titleHintLabel.text = levelLabelText
        }

        // no content? -> hide the content container
        if (binding.content.childCount == 0) {
            binding.content.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        otherAnswersButton = null
    }

    private fun assembleOtherAnswers() : List<AnswerItem> {
        val answers = mutableListOf<AnswerItem>()

        val cantSay = AnswerItem(R.string.quest_generic_answer_notApplicable) { onClickCantSay() }
        answers.add(cantSay)

        createSplitWayAnswer()?.let { answers.add(it) }
        createDeleteOrReplaceElementAnswer()?.let { answers.add(it) }

        answers.addAll(otherAnswers)
        return answers
    }

    private fun createSplitWayAnswer(): AnswerItem? {
        val isSplitWayEnabled = (questType as? OsmElementQuestType)?.isSplitWayEnabled == true
        if (!isSplitWayEnabled) return null

        val way = osmElement as? Way ?: return null

        /* splitting up a closed roundabout can be very complex if it is part of a route
           relation, so it is not supported
           https://wiki.openstreetmap.org/wiki/Relation:route#Bus_routes_and_roundabouts
        */
        val isClosedRoundabout = way.nodeIds.firstOrNull() == way.nodeIds.lastOrNull() &&
            way.tags["junction"] == "roundabout"
        if (isClosedRoundabout) return null

        if (way.isArea()) return null

        return AnswerItem(R.string.quest_generic_answer_differs_along_the_way) {
            onClickSplitWayAnswer()
        }
    }

    private fun createDeleteOrReplaceElementAnswer(): AnswerItem? {
        val isDeletePoiEnabled =
            (questType as? OsmElementQuestType)?.isDeleteElementEnabled == true
                && osmElement?.type == ElementType.NODE
        val isReplaceShopEnabled = (questType as? OsmElementQuestType)?.isReplaceShopEnabled == true
        if (!isDeletePoiEnabled && !isReplaceShopEnabled) return null
        check(!(isDeletePoiEnabled && isReplaceShopEnabled)) {
            "Only isDeleteElementEnabled OR isReplaceShopEnabled may be true at the same time"
        }

        return AnswerItem(R.string.quest_generic_answer_does_not_exist) {
            if (isDeletePoiEnabled) deletePoiNode()
            else if (isReplaceShopEnabled) replaceShopElement()
        }
    }

    private fun showOtherAnswers() {
        val otherAnswersButton = otherAnswersButton ?: return
        val answers = assembleOtherAnswers()
        val popup = PopupMenu(requireContext(), otherAnswersButton)
        for (i in answers.indices) {
            val otherAnswer = answers[i]
            val order = answers.size - i
            popup.menu.add(Menu.NONE, i, order, otherAnswer.titleResourceId)
        }
        popup.show()

        popup.setOnMenuItemClickListener { item ->
            answers[item.itemId].action()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        if(!startedOnce) {
            onMapOrientation(initialMapRotation, initialMapTilt)
            startedOnce = true
        }

        val answers = assembleOtherAnswers()
        if (answers.size == 1) {
            otherAnswersButton?.setText(answers.first().titleResourceId)
            otherAnswersButton?.setOnClickListener { answers.first().action() }
        } else {
            otherAnswersButton?.setText(R.string.quest_generic_otherAnswers)
            otherAnswersButton?.setOnClickListener { showOtherAnswers() }
        }
    }

    /** Note: Due to Android architecture limitations, a layout inflater based on this ContextWrapper
     * will not resolve any resources specified in the XML according to MCC  */
    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        // will always return a layout inflater for the current country
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(context)
    }

    override fun getContext(): Context? {
        val context = super.getContext()
        if (currentContext.get() !== context) {
            currentContext = WeakReference<Context>(context)
            currentCountryContext = if (context != null) createCurrentCountryContextWrapper(context) else null
        }
        return currentCountryContext
    }

    private fun createCurrentCountryContextWrapper(context: Context): ContextWrapper {
        val conf = Configuration(context.resources.configuration)
        conf.mcc = countryInfo.mobileCountryCode ?: 0
        val res = context.createConfigurationContext(conf).resources
        return object : ContextWrapper(context) {
            override fun getResources(): Resources {
                return res
            }
        }
    }

    protected fun onClickCantSay() {
        context?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_leave_new_note_title)
            .setMessage(R.string.quest_leave_new_note_description)
            .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> skipQuest() }
            .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ -> composeNote() }
            .show()
        }
    }

    protected fun composeNote() {
        val questTitle = englishResources.getQuestTitle(questType, osmElement, featureDictionaryFuture)
        listener?.onComposeNote(questKey, questTitle)
    }

    private fun onClickSplitWayAnswer() {
        context?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_split_way_description)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.onSplitWay(questKey as OsmQuestKey)
            }
            .show()
        }
    }

    protected fun applyAnswer(data: T) {
        listener?.onAnsweredQuest(questKey, data as Any)
    }

    protected fun skipQuest() {
        listener?.onSkippedQuest(questKey)
    }

    protected fun replaceShopElement() {
        val ctx = context ?: return
        val element = osmElement ?: return
        val isoCountryCode = countryInfo.countryCode.substringBefore('-')

        if (element.isSomeKindOfShop()) {
            ShopGoneDialog(
                ctx,
                element.geometryType,
                isoCountryCode,
                featureDictionary,
                onSelectedFeature = { tags ->
                    listener?.onReplaceShopElement(questKey as OsmQuestKey, tags)
                },
                onLeaveNote = this::composeNote
            ).show()
        } else {
            composeNote()
        }
    }

    protected fun deletePoiNode() {
        val context = context ?: return

        AlertDialog.Builder(context)
            .setMessage(R.string.osm_element_gone_description)
            .setPositiveButton(R.string.osm_element_gone_confirmation) { _, _ ->
                listener?.onDeletePoiNode(questKey as OsmQuestKey)
            }
            .setNeutralButton(R.string.leave_note) { _, _ ->
                composeNote()
            }.show()
    }

    /** Inflate given layout resource id into the content view and return the inflated view */
    protected fun setContentView(resourceId: Int): View {
        if (binding.content.childCount > 0) {
            binding.content.removeAllViews()
        }
        binding.content.visibility = View.VISIBLE
        updateContentPadding()
        layoutInflater.inflate(resourceId, binding.content)
        return binding.content.getChildAt(0)
    }

    private fun updateContentPadding() {
        if(!contentPadding) {
            binding.content.setPadding(0,0,0,0)
        } else {
            val horizontal = resources.getDimensionPixelSize(R.dimen.quest_form_horizontal_padding)
            val vertical = resources.getDimensionPixelSize(R.dimen.quest_form_vertical_padding)
            binding.content.setPadding(horizontal, vertical, horizontal, vertical)
        }
    }

    protected fun updateButtonPanel() {
        // the other answers button is never removed/replaced
        if (binding.buttonPanel.childCount > 1) {
            binding.buttonPanel.removeViews(1, binding.buttonPanel.childCount - 1)
        }

        for (buttonPanelAnswer in buttonPanelAnswers) {
            val button = ButtonPanelButtonBinding.inflate(layoutInflater, binding.buttonPanel, true).root
            button.setText(buttonPanelAnswer.titleResourceId)
            button.setOnClickListener { buttonPanelAnswer.action() }
        }
    }

    @AnyThread open fun onMapOrientation(rotation: Float, tilt: Float) {
        // default empty implementation
    }

    class InjectedFields {
        @Inject internal lateinit var countryInfos: CountryInfos
        @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
        @Inject internal lateinit var featureDictionaryFuture: FutureTask<FeatureDictionary>
    }

    protected inline fun <reified T : ViewBinding> contentViewBinding(
        noinline viewBinder: (View) -> T
    ) = FragmentViewBindingPropertyDelegate(this, viewBinder, R.id.content)

    companion object {
        private const val ARG_QUEST_KEY = "quest_key"
        private const val ARG_ELEMENT = "element"
        private const val ARG_GEOMETRY = "geometry"
        private const val ARG_QUESTTYPE = "quest_type"
        private const val ARG_MAP_ROTATION = "map_rotation"
        private const val ARG_MAP_TILT = "map_tilt"

        fun createArguments(quest: Quest, element: Element?, rotation: Float, tilt: Float) = bundleOf(
            ARG_QUEST_KEY to Json.encodeToString(quest.key),
            ARG_ELEMENT to element?.let { Json.encodeToString(element) },
            ARG_GEOMETRY to Json.encodeToString(quest.geometry),
            ARG_QUESTTYPE to quest.type::class.simpleName!!,
            ARG_MAP_ROTATION to rotation,
            ARG_MAP_TILT to tilt
        )
    }
}

data class AnswerItem(val titleResourceId: Int, val action: () -> Unit)
