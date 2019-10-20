package de.westnordost.streetcomplete.quests

import android.content.ContextWrapper
import androidx.annotation.AnyThread
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu

import java.lang.ref.WeakReference
import java.util.Locale

import javax.inject.Inject

import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.OsmAreas
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import java.util.concurrent.FutureTask

/** Abstract base class for any dialog with which the user answers a specific quest(ion)  */
abstract class AbstractQuestAnswerFragment<T> : AbstractBottomSheetFragment(), IsShowingQuestDetails {

    private val countryInfos: CountryInfos
    private val questTypeRegistry: QuestTypeRegistry
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>

    private val questAnswerComponent = QuestAnswerComponent()

    private lateinit var content: ViewGroup
    private lateinit var buttonPanel: ViewGroup
    private lateinit var otherAnswersButton: Button

    protected lateinit var elementGeometry: ElementGeometry private set
    private lateinit var questType: QuestType<T>
    private var initialMapRotation = 0f
    private var initialMapTilt = 0f
    protected var osmElement: OsmElement? = null
        private set

    private var currentContext = WeakReference<Context>(null)
    private var currentCountryContext: ContextWrapper? = null

    // lazy but resettable because based on lateinit var
    private var _countryInfo: CountryInfo? = null
        get() {
            if(field == null) {
                val latLon = elementGeometry.center
                field = countryInfos.get(latLon.longitude, latLon.latitude)
            }
            return field
        }
    protected val countryInfo get() = _countryInfo!!

    private val englishResources: Resources
        get() {
            val conf = Configuration(resources.configuration)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = super.getContext()!!.createConfigurationContext(conf)
            return localizedContext.resources
        }

    private var startedOnce = false

    override val questId: Long get() = questAnswerComponent.questId
    override val questGroup: QuestGroup get() = questAnswerComponent.questGroup

    open val contentLayoutResId: Int? = null
    open val buttonsResId: Int? = null
    open val otherAnswers = listOf<OtherAnswer>()

    open val contentPadding = true

    init {
        val fields = InjectedFields()
        Injector.instance.applicationComponent.inject(fields)
        countryInfos = fields.countryInfos
        questTypeRegistry = fields.questTypeRegistry
        featureDictionaryFuture = fields.featureDictionaryFuture
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        questAnswerComponent.onCreate(arguments)
        osmElement = arguments!!.getSerializable(ARG_ELEMENT) as OsmElement?
        elementGeometry = arguments!!.getSerializable(ARG_GEOMETRY) as ElementGeometry
        questType = questTypeRegistry.getByName(arguments!!.getString(ARG_QUESTTYPE)) as QuestType<T>
        initialMapRotation = arguments!!.getFloat(ARG_MAP_ROTATION)
        initialMapTilt = arguments!!.getFloat(ARG_MAP_TILT)
        _countryInfo = null // reset lazy field
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_quest_answer, container, false)
        content = view.findViewById(R.id.content)
        buttonPanel = view.findViewById(R.id.buttonPanel)
        otherAnswersButton = buttonPanel.findViewById(R.id.otherAnswersButton)

        contentLayoutResId?.let { setContentView(it) }
        buttonsResId?.let { setButtonsView(it) }
        if(!contentPadding) content.setPadding(0,0,0,0)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleLabel.text = resources.getHtmlQuestTitle(questType, osmElement, featureDictionaryFuture)

        val levelLabelText = getLevelLabelText()
        if (levelLabelText != null) {
            levelLabel.visibility = View.VISIBLE
            levelLabel.text = levelLabelText
        } else {
            levelLabel.visibility = View.GONE
        }

        // no content? -> hide the content container
        if (content.childCount == 0) {
            content.visibility = View.GONE
        }

        val answers = assembleOtherAnswers()
        if (answers.size == 1) {
            otherAnswersButton.setText(answers.first().titleResourceId)
            otherAnswersButton.setOnClickListener { answers.first().action() }
        } else {
            otherAnswersButton.setText(R.string.quest_generic_otherAnswers)
            otherAnswersButton.setOnClickListener {
                val popup = PopupMenu(activity, otherAnswersButton)
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
        }
    }

    private fun assembleOtherAnswers() : List<OtherAnswer> {
        val answers = mutableListOf<OtherAnswer>()

        val cantSay = OtherAnswer(R.string.quest_generic_answer_notApplicable) { onClickCantSay() }
        answers.add(cantSay)

        val way = osmElement as? Way
        if (way != null) {
            /* splitting up a closed roundabout can be very complex if it is part of a route
               relation, so it is not supported
               https://wiki.openstreetmap.org/wiki/Relation:route#Bus_routes_and_roundabouts
            */
            val isClosedRoundabout = way.nodeIds.firstOrNull() == way.nodeIds.lastOrNull() &&
                    way.tags?.get("junction") == "roundabout"
            if (!isClosedRoundabout && !OsmAreas.isArea(way)) {
                val splitWay = OtherAnswer(R.string.quest_generic_answer_differs_along_the_way) {
                    onClickSplitWayAnswer()
                }
                answers.add(splitWay)
            }
        }
        answers.addAll(otherAnswers)
        return answers
    }

    private fun getLevelLabelText(): String? {
        val tags = osmElement?.tags ?: return null
        /* prefer addr:floor etc. over level as level is rather an index than how the floor is
           denominated in the building and thus may (sometimes) not coincide with it. E.g.
           addr:floor may be "M" while level is "2" */
        val level = tags["addr:floor"] ?: tags["level:ref"] ?: tags["level"]
        if (level != null) {
            return resources.getString(R.string.on_level, level)
        }
        val tunnel = tags["tunnel"]
        if(tunnel != null && tunnel == "yes" || tags["location"] == "underground") {
            return resources.getString(R.string.underground)
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        if(!startedOnce) {
            onMapOrientation(initialMapRotation, initialMapTilt)
            startedOnce = true
        }
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        questAnswerComponent.onAttach(ctx as OsmQuestAnswerListener)
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
            .setPositiveButton(R.string.quest_leave_new_note_yes) { _, _ ->
                val questTitle = englishResources.getQuestTitle(questType, osmElement, featureDictionaryFuture)
                questAnswerComponent.onComposeNote(questTitle)
            }
            .show()
        }
    }

    private fun onClickSplitWayAnswer() {
        context?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_split_way_description)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                questAnswerComponent.onSplitWay()
            }
            .show()
        }
    }

    protected fun applyAnswer(data: T) {
        questAnswerComponent.onAnsweredQuest(data as Any)
    }

    protected fun skipQuest() {
        questAnswerComponent.onSkippedQuest()
    }

    protected fun setContentView(resourceId: Int): View {
        if (content.childCount > 0) {
            content.removeAllViews()
        }
        content.visibility = View.VISIBLE
        return layoutInflater.inflate(resourceId, content)
    }

    protected fun setNoContentPadding() {
        content.setPadding(0, 0, 0, 0)
    }

    protected fun setButtonsView(resourceId: Int): View {
        // if other buttons are present, the other answers button should have a weight so that it
        // can be squeezed if there is not enough space for everything
        otherAnswersButton.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1f)
        return activity!!.layoutInflater.inflate(resourceId, buttonPanel)
    }

    @AnyThread open fun onMapOrientation(rotation: Float, tilt: Float) {
        // default empty implementation
    }

    internal class InjectedFields {
        @Inject internal lateinit var countryInfos: CountryInfos
        @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
        @Inject internal lateinit var featureDictionaryFuture: FutureTask<FeatureDictionary>
    }

    companion object {
        const val ARG_ELEMENT = "element"
        const val ARG_GEOMETRY = "geometry"
        const val ARG_QUESTTYPE = "quest_type"
        const val ARG_MAP_ROTATION = "map_rotation"
        const val ARG_MAP_TILT = "map_tilt"
    }
}

data class OtherAnswer(val titleResourceId: Int, val action: () -> Unit)
