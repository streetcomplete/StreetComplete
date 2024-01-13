package de.westnordost.streetcomplete.overlays

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.data.location.checkIsSurvey
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.edits.AddElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.databinding.FragmentOverlayBinding
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.util.FragmentViewBindingPropertyDelegate
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RoundRectOutlineProvider
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.add
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.Locale
import java.util.concurrent.FutureTask

/** Abstract base class for any form displayed for an overlay */
abstract class AbstractOverlayForm :
    Fragment(), IsShowingElement, IsCloseableBottomSheet, IsMapOrientationAware {

    // dependencies
    private val elementEditsController: ElementEditsController by inject()
    private val countryInfos: CountryInfos by inject()
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val overlayRegistry: OverlayRegistry by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val recentLocationStore: RecentLocationStore by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    protected val featureDictionary: FeatureDictionary get() = featureDictionaryFuture.get()
    private var _countryInfo: CountryInfo? = null // lazy but resettable because based on lateinit var
        get() {
            if (field == null) {
                field = countryInfos.getByLocation(
                    countryBoundaries.get(),
                    geometry.center.longitude,
                    geometry.center.latitude,
                )
            }
            return field
        }
    protected val countryInfo get() = _countryInfo!!

    /** either DE or US-NY (or null), depending on what countryBoundaries returns */
    protected val countryOrSubdivisionCode: String? get() {
        val latLon = geometry.center
        return countryBoundaries.get().getIds(latLon.longitude, latLon.latitude).firstOrNull()
    }

    private val englishResources: Resources
        get() {
            val conf = Configuration(resources.configuration)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = super.requireContext().createConfigurationContext(conf)
            return localizedContext.resources
        }

    // only used for testing / only used for ShowQuestFormsActivity! Found no better way to do this
    var addElementEditsController: AddElementEditsController = elementEditsController

    // view / state
    private var _binding: FragmentOverlayBinding? = null
    private val binding get() = _binding!!

    private var startedOnce = false

    // passed in parameters
    protected lateinit var overlay: Overlay private set
    protected var element: Element? = null
        private set
    private var _geometry: ElementGeometry? = null
    protected val geometry: ElementGeometry
        get() = _geometry ?: ElementPointGeometry(getDefaultMarkerPosition()!!)

    private var initialMapRotation = 0f
    private var initialMapTilt = 0f
    override val elementKey: ElementKey? get() = element?.key

    protected val metersPerPixel: Double? get() = listener?.metersPerPixel

    // overridable by child classes
    open val contentLayoutResId: Int? = null
    open val contentPadding = true
    open val otherAnswers = listOf<IAnswerItem>()

    interface Listener {
        /** The GPS position at which the user is displayed at */
        val displayedMapLocation: Location?

        /** How many pixels equal one meter on display at the current zoom */
        val metersPerPixel: Double?

        /** Called when the user successfully answered the quest */
        fun onEdited(editType: ElementEditType, geometry: ElementGeometry)

        /** Called when the user chose to leave a note instead */
        fun onComposeNote(editType: ElementEditType, element: Element, geometry: ElementGeometry, leaveNoteContext: String)

        /** Called when the user chose to split the way */
        fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry)

        /** Called when the user chose to move the node */
        fun onMoveNode(editType: ElementEditType, node: Node)

        fun getMapPositionAt(screenPos: PointF): LatLon?
        fun getPointOf(pos: LatLon): PointF?
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* --------------------------------------- Lifecycle --------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        overlay = overlayRegistry.getByName(args.getString(ARG_OVERLAY)!!)!!
        element = args.getString(ARG_ELEMENT)?.let { Json.decodeFromString(it) }
        _geometry = (savedInstanceState?.getString(ARG_GEOMETRY) ?: args.getString(ARG_GEOMETRY))
            ?.let { Json.decodeFromString(it) }
        initialMapRotation = args.getFloat(ARG_MAP_ROTATION)
        initialMapTilt = args.getFloat(ARG_MAP_TILT)
        _countryInfo = null // reset lazy field

        /* deliberately did not copy the mobile-country-code hack from AbstractQuestForm because
           this is kind of deprecated and should not be used for new code */
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentOverlayBinding.inflate(inflater, container, false)
        contentLayoutResId?.let { setContentView(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMarkerVisibility(_geometry == null)
        binding.createMarker.doOnLayout { setMarkerPosition(null) }
        binding.bottomSheetContainer.respectSystemInsets(View::setMargins)

        val cornerRadius = resources.getDimension(R.dimen.speech_bubble_rounded_corner_radius)
        val margin = resources.getDimensionPixelSize(R.dimen.horizontal_speech_bubble_margin)
        binding.speechbubbleContentContainer.outlineProvider = RoundRectOutlineProvider(
            cornerRadius, margin, margin, margin, margin
        )
        binding.speechbubbleContentContainer.clipToOutline = true

        setTitleHintLabel(
            element?.let { getNameAndLocationLabel(it, resources, featureDictionary) }
        )

        binding.moreButton.setOnClickListener {
            showOtherAnswers()
        }
        binding.okButton.setOnClickListener {
            if (!isFormComplete()) {
                activity?.toast(R.string.no_changes)
            } else {
                onClickOk()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // see rant comment in AbstractBottomSheetFragment
        resources.updateConfiguration(newConfig, resources.displayMetrics)

        binding.bottomSheetContainer.updateLayoutParams { width = resources.getDimensionPixelSize(R.dimen.quest_form_width) }

        setMarkerPosition(null)
    }

    override fun onStart() {
        super.onStart()

        checkIsFormComplete()

        if (!startedOnce) {
            onMapOrientation(initialMapRotation, initialMapTilt)
            startedOnce = true
        }
    }

    override fun onMapOrientation(rotation: Float, tilt: Float) {
        // default empty implementation
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_GEOMETRY, Json.encodeToString(geometry))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /* --------------------------------- IsCloseableBottomSheet  ------------------------------- */

    @UiThread override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        return false
    }

    /** Request to close the form through user interaction (back button, clicked other quest,..),
     * requires user confirmation if any changes have been made  */
    @UiThread override fun onClickClose(onConfirmed: () -> Unit) {
        if (!isRejectingClose()) {
            onDiscard()
            onConfirmed()
        } else {
            activity?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_discard_title)
                    .setPositiveButton(R.string.confirmation_discard_positive) { _, _ ->
                        onDiscard()
                        onConfirmed()
                    }
                    .setNegativeButton(R.string.short_no_answer_on_button, null)
                    .show()
            }
        }
    }

    /* ------------------------------- Interface for subclasses  ------------------------------- */

    protected fun setTitleHintLabel(text: CharSequence?) {
        binding.titleHintLabel.text = text
        binding.titleHintLabelContainer.isGone = text == null
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

    protected fun setMarkerIcon(iconResId: Int) {
        binding.createMarkerIconView.setImageResource(iconResId)
    }

    protected fun setMarkerVisibility(isVisible: Boolean) {
        binding.createMarker.isInvisible = !isVisible
    }

    protected fun setMarkerPosition(position: LatLon?) {
        val point = if (position == null) {
            getDefaultMarkerScreenPosition()
        } else {
            listener?.getPointOf(position)
        } ?: return
        binding.createMarker.x = point.x - binding.createMarker.width / 2
        binding.createMarker.y = point.y - binding.createMarker.height
    }

    private fun updateContentPadding() {
        if (!contentPadding) {
            binding.content.setPadding(0, 0, 0, 0)
        } else {
            val horizontal = resources.getDimensionPixelSize(R.dimen.quest_form_horizontal_padding)
            val vertical = resources.getDimensionPixelSize(R.dimen.quest_form_vertical_padding)
            binding.content.setPadding(horizontal, vertical, horizontal, vertical)
        }
    }

    protected fun applyEdit(answer: ElementEditAction, geometry: ElementGeometry = this.geometry) {
        viewLifecycleScope.launch {
            solve(answer, geometry)
        }
    }

    protected fun checkIsFormComplete() {
        val isComplete = isFormComplete()
        binding.okButton.isEnabled = hasChanges() && isComplete
        if (isComplete) {
            binding.okButtonContainer.popIn()
        } else {
            binding.okButtonContainer.popOut()
        }
    }

    private fun isRejectingClose(): Boolean = hasChanges()

    protected abstract fun hasChanges(): Boolean

    protected open fun onDiscard() {}

    protected abstract fun isFormComplete(): Boolean

    protected abstract fun onClickOk()

    protected inline fun <reified T : ViewBinding> contentViewBinding(
        noinline viewBinder: (View) -> T
    ) = FragmentViewBindingPropertyDelegate(this, viewBinder, R.id.content)

    /* -------------------------------------- ...-Button -----------------------------------------*/

    private fun showOtherAnswers() {
        val answers = assembleOtherAnswers()
        val popup = PopupMenu(requireContext(), binding.moreButton)
        for (i in answers.indices) {
            val otherAnswer = answers[i]
            val order = answers.size - i
            popup.menu.add(Menu.NONE, i, order, otherAnswer.title)
        }
        popup.show()

        popup.setOnMenuItemClickListener { item ->
            answers[item.itemId].action()
            true
        }
    }

    private fun assembleOtherAnswers(): List<IAnswerItem> {
        val answers = mutableListOf<IAnswerItem>()

        val element = element
        if (element != null) {
            answers.add(AnswerItem(R.string.leave_note) { composeNote(element) })

            if (element.isSplittable()) {
                answers.add(AnswerItem(R.string.split_way) { splitWay(element) })
            }

            if (element is Node // add moveNodeAnswer only if it's a free floating node
                && mapDataWithEditsSource.getWaysForNode(element.id).isEmpty()
                && mapDataWithEditsSource.getRelationsForNode(element.id).isEmpty()) {
                answers.add(AnswerItem(R.string.move_node) { moveNode() })
            }
        }

        answers.addAll(otherAnswers)
        return answers
    }

    protected fun splitWay(element: Element) {
        listener?.onSplitWay(overlay, element as Way, geometry as ElementPolylinesGeometry)
    }

    private fun moveNode() {
        listener?.onMoveNode(overlay, element as Node)
    }

    protected fun composeNote(element: Element) {
        val overlayTitle = englishResources.getString(overlay.title)
        val hintLabel = getNameAndLocationLabel(element, englishResources, featureDictionary)
        val leaveNoteContext = if (hintLabel.isNullOrBlank()) {
            "In context of overlay \"$overlayTitle\""
        } else {
            "In context of overlay \"$overlayTitle\" – $hintLabel"
        }
        listener?.onComposeNote(overlay, element, geometry, leaveNoteContext)
    }

    /* -------------------------------------- Apply edit  -------------------------------------- */

    private suspend fun solve(action: ElementEditAction, geometry: ElementGeometry) {
        setLocked(true)
        if (!checkIsSurvey(requireContext(), geometry, recentLocationStore.get())) {
            setLocked(false)
            return
        }

        withContext(Dispatchers.IO) {
            addElementEditsController.add(overlay, geometry, "survey", action)
        }
        listener?.onEdited(overlay, geometry)
    }

    private fun setLocked(locked: Boolean) {
        binding.glassPane.isGone = !locked
    }

    /* ------------------------------------- marker position ------------------------------------ */

    private fun getDefaultMarkerPosition(): LatLon? {
        val point = getDefaultMarkerScreenPosition() ?: return null
        return listener?.getMapPositionAt(point)
    }

    private fun getDefaultMarkerScreenPosition(): PointF? {
        val view = view ?: return null
        val left = resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset)
        val right = resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset)
        val top = resources.getDimensionPixelSize(R.dimen.quest_form_topOffset)
        val bottom = resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        val x = (view.width + left - right) / 2f
        val y = (view.height + top - bottom) / 2f
        return PointF(x, y)
    }

    companion object {
        private const val ARG_ELEMENT = "element"
        private const val ARG_GEOMETRY = "geometry"
        private const val ARG_OVERLAY = "overlay"
        private const val ARG_MAP_ROTATION = "map_rotation"
        private const val ARG_MAP_TILT = "map_tilt"

        fun createArguments(overlay: Overlay, element: Element?, geometry: ElementGeometry?, rotation: Float, tilt: Float) = bundleOf(
            ARG_ELEMENT to element?.let { Json.encodeToString(it) },
            ARG_GEOMETRY to geometry?.let { Json.encodeToString(it) },
            ARG_OVERLAY to overlay.name,
            ARG_MAP_ROTATION to rotation,
            ARG_MAP_TILT to tilt
        )
    }
}

interface IAnswerItem {
    val title: Text
    val action: () -> Unit
}

data class AnswerItem(val titleResourceId: Int, override val action: () -> Unit) : IAnswerItem {
    override val title: Text get() = ResText(titleResourceId)
}

data class AnswerItem2(val titleString: String, override val action: () -> Unit) : IAnswerItem {
    override val title: Text get() = CharSequenceText(titleString)
}
