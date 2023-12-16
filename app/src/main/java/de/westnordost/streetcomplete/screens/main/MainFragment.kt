package de.westnordost.streetcomplete.screens.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.LayerDrawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.AnyThread
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.graphics.minus
import androidx.core.graphics.toPointF
import androidx.core.graphics.toRectF
import androidx.core.os.ConfigurationCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.isWayComplete
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.EffectQuestPlopBinding
import de.westnordost.streetcomplete.databinding.FragmentMainBinding
import de.westnordost.streetcomplete.osm.IS_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.level.createLevelsOrNull
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.IsShowingElement
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.IsShowingQuestDetails
import de.westnordost.streetcomplete.quests.LeaveNoteInsteadFragment
import de.westnordost.streetcomplete.quests.TagEditor
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreateNoteFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreatePoiFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.InsertNodeFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MoveNodeFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.SplitWayFragment
import de.westnordost.streetcomplete.screens.main.controls.LocationStateButton
import de.westnordost.streetcomplete.screens.main.controls.MainMenuButtonFragment
import de.westnordost.streetcomplete.screens.main.controls.UndoButtonFragment
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryFragment
import de.westnordost.streetcomplete.screens.main.map.LocationAwareMapFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.MapFragment
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.screens.main.map.getPinIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraPosition
import de.westnordost.streetcomplete.screens.settings.DisplaySettingsFragment
import de.westnordost.streetcomplete.util.SoundFx
import de.westnordost.streetcomplete.util.buildGeoUri
import de.westnordost.streetcomplete.util.dialogs.showProfileSelectionDialog
import de.westnordost.streetcomplete.util.getFakeCustomOverlays
import de.westnordost.streetcomplete.util.ktx.childFragmentManagerOrNull
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toList
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.truncateTo5Decimals
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.showOverlayCustomizer
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.concurrent.FutureTask
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** This fragment controls the main view.
 *
 *  The logical sub components of this main view are all outsourced into individual child fragments
 *  with which this fragment communicates with.
 *
 *  The child fragments do not communicate with each other but only with their parent (this class)
 *  and the parent then controls its children. Hence, all the logic when interacting with the
 *  map / bottom sheets / sidebars / buttons etc. passes through this class and this is why this
 *  class implements all the listeners of its child fragments.
 *
 *  This class does not contain so much logic itself, it delegates most of it to its children.
 *  Think of it as the wiring that binds all the components together.
 *
 *  Still, as this is by far the largest in terms of lines of code. For easier reading, in
 *  IntelliJ, you can collapse sections of this class that start with "//region" using the little
 *  [-] icon next to it.
 *
 *  */
class MainFragment :
    Fragment(R.layout.fragment_main),
    // listeners to child fragments:
    MapFragment.Listener,
    LocationAwareMapFragment.Listener,
    MainMapFragment.Listener,
    AbstractOsmQuestForm.Listener,
    AbstractOverlayForm.Listener,
    SplitWayFragment.Listener,
    NoteDiscussionForm.Listener,
    LeaveNoteInsteadFragment.Listener,
    CreateNoteFragment.Listener,
    MoveNodeFragment.Listener,
    EditHistoryFragment.Listener,
    MainMenuButtonFragment.Listener,
    UndoButtonFragment.Listener,
    // listeners to changes to data:
    VisibleQuestsSource.Listener,
    MapDataWithEditsSource.Listener,
    SelectedOverlaySource.Listener,
    // rest
    ShowsGeometryMarkers,
    // we need the android preferences listener, because the new one can't to what is needed
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val notesSource: NotesWithEditsSource by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val selectedOverlaySource: SelectedOverlayController by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val soundFx: SoundFx by inject()
    private val prefs: Preferences by inject()
    private val questPresetsController: QuestPresetsController by inject()
    private val levelFilter: LevelFilter by inject()
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val osmQuestController: OsmQuestController by inject()

    private lateinit var locationManager: FineLocationManager

    private val binding by viewBinding(FragmentMainBinding::bind)

    private var wasFollowingPosition: Boolean? = null
    private var wasNavigationMode: Boolean? = null

    private var windowInsets: Insets? = null

    private var mapFragment: MainMapFragment? = null
    private var mainMenuButtonFragment: MainMenuButtonFragment? = null

    private val bottomSheetFragment: Fragment? get() =
        childFragmentManagerOrNull?.findFragmentByTag(BOTTOM_SHEET)

    private val editHistoryFragment: EditHistoryFragment? get() =
        childFragmentManagerOrNull?.findFragmentByTag(EDIT_HISTORY) as? EditHistoryFragment

    private var mapOffsetWithOpenBottomSheet: RectF = RectF(0f, 0f, 0f, 0f)

    interface Listener {
        fun onMapInitialized()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    private val historyBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            mapFragment?.endFocus(0L)
            closeEditHistorySidebar()
        }
    }

    private val sheetBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            val f = childFragmentManager.fragments.lastOrNull()
            if (f is IsCloseableBottomSheet) {
                if (f != bottomSheetFragment && f is AbstractQuestForm)
                    f.onClickClose { TagEditor.changes = StringMapChanges(emptySet()) }
                else if (f == bottomSheetFragment)
                    f.onClickClose { closeBottomSheet() }
            }
        }
    }

    //region Lifecycle - Android Lifecycle Callbacks

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(TAG, "onAttach")
        locationManager = FineLocationManager(context, this::onLocationChanged)

        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            when (fragment) {
                is MainMapFragment -> mapFragment = fragment
                is MainMenuButtonFragment -> mainMenuButtonFragment = fragment
            }
        }
        childFragmentManager.commit { add(LocationRequestFragment(), TAG_LOCATION_REQUEST) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(TAG, "onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.i(TAG, "onViewStateRestored")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, "onDetach")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated")

        binding.mapControls.respectSystemInsets(View::setMargins)
        view.respectSystemInsets { windowInsets = it }

        updateCreateButtonVisibility()

        binding.locationPointerPin.setOnClickListener { onClickLocationPointer() }

        binding.compassView.setOnClickListener { onClickCompassButton() }
        binding.gpsTrackingButton.setOnClickListener { onClickTrackingButton() }
        binding.stopTracksButton.setOnClickListener { onClickTracksStop() }
        binding.zoomInButton.setOnClickListener { onClickZoomIn() }
        binding.zoomOutButton.setOnClickListener { onClickZoomOut() }
        binding.createButton.setOnClickListener { onClickCreateButton() }
        binding.quickSettingsButton.visibility = if (prefs.getBoolean(Prefs.QUICK_SETTINGS, false))
            View.VISIBLE
        else
            View.GONE
        binding.quickSettingsButton.setOnClickListener { onClickQuickSettings() }

        updateOffsetWithOpenBottomSheet()

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, historyBackPressedCallback)
        historyBackPressedCallback.isEnabled = editHistoryFragment != null
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, sheetBackPressedCallback)
        sheetBackPressedCallback.isEnabled = bottomSheetFragment is IsCloseableBottomSheet
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        binding.quickSettingsButton.visibility = if (prefs.getBoolean(Prefs.QUICK_SETTINGS, false))
            View.VISIBLE
        else
            View.GONE
        if (mapFragment?.isMapInitialized == true)
            mapFragment?.show3DBuildings = prefs.getBoolean(Prefs.SHOW_3D_BUILDINGS, true)
        if (DisplaySettingsFragment.gpx_track_changed) {
            mapFragment?.loadGpxTrack()
            DisplaySettingsFragment.gpx_track_changed = false
        }
    }

    @UiThread
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "onConfigurationChanged")
        val mapFragment = this.mapFragment ?: return
        /* when rotating the screen and the bottom sheet is open, the view
           should not rotate around its proper center but around the center
           of the part of the map that is not occluded by the bottom sheet */
        val previousOffset = mapOffsetWithOpenBottomSheet
        updateOffsetWithOpenBottomSheet()
        if (bottomSheetFragment != null) {
            mapFragment.adjustToOffsets(previousOffset, mapOffsetWithOpenBottomSheet)
        }
        binding.crosshairView.setPadding(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        )
        updateLocationPointerPin()
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart (add listeners)")
        wasFollowingPosition = mapFragment?.isFollowingPosition // use value from mapFragment if already loaded
        visibleQuestsSource.addListener(this)
        mapDataWithEditsSource.addListener(this)
        selectedOverlaySource.addListener(this)
        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(requireContext().run { hasLocationPermission && isLocationEnabled })
        StreetCompleteApplication.preferences.registerOnSharedPreferenceChangeListener(this)
        reloadOverlaySelector()
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop (remove listeners)")
        wasFollowingPosition = mapFragment?.isFollowingPosition
        wasNavigationMode = mapFragment?.isNavigationMode
        visibleQuestsSource.removeListener(this)
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)
        mapDataWithEditsSource.removeListener(this)
        selectedOverlaySource.removeListener(this)
        locationManager.removeUpdates()
        StreetCompleteApplication.preferences.unregisterOnSharedPreferenceChangeListener(this)
        clearOverlaySelector()
    }

    private fun updateOffsetWithOpenBottomSheet() {
        mapOffsetWithOpenBottomSheet = Rect(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        ).toRectF()
    }

    //endregion

    private fun clearOverlaySelector() = binding.overlayLayout.removeAllViews()

    private fun reloadOverlaySelector() {
        if (!prefs.getBoolean(Prefs.OVERLAY_QUICK_SELECTOR, false)) {
            binding.overlayScrollView.isGone = true
            return
        }
        requireActivity().runOnUiThread { clearOverlaySelector() }
        if (bottomSheetFragment == null) // always fill, but only show if no quest, overlay, etc... is showing
            binding.overlayScrollView.isVisible = true

        val overlays = overlayRegistry.filter {
            val eeAllowed = if (prefs.getBoolean(Prefs.EXPERT_MODE, false)) true
                else overlayRegistry.getOrdinalOf(it)!! < ApplicationConstants.EE_QUEST_OFFSET
            eeAllowed && it !is CustomOverlay
        } + getFakeCustomOverlays(prefs, requireContext())
        val params = ViewGroup.LayoutParams(requireContext().dpToPx(52).toInt(), requireContext().dpToPx(52).toInt())
        overlays.forEach { overlay ->
            val view = ImageView(requireContext())
            val index = overlay.wikiLink?.toIntOrNull()
            val isActive = selectedOverlaySource.selectedOverlay == overlay
                || (selectedOverlaySource.selectedOverlay is CustomOverlay && index == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0))
            if (isActive) {
                val ring = ContextCompat.getDrawable(requireContext(), R.drawable.pin_selection_ring)!!
                val icon = ContextCompat.getDrawable(requireContext(), overlay.icon)!!
                view.setImageDrawable(LayerDrawable(arrayOf(icon, ring)))
            } else {
                view.setImageResource(overlay.icon)
                view.colorFilter = PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY)
            }
            view.scaleX = 0.95f
            view.scaleY = 0.95f
            if (overlay.title == 0 && index != null)
                view.setOnLongClickListener {
                    showOverlayCustomizer(index, requireContext(), prefs, questTypeRegistry,
                        { isCurrentCustomOverlay ->
                            viewLifecycleScope.launch(Dispatchers.IO) {
                                if (isCurrentCustomOverlay && selectedOverlaySource.selectedOverlay is CustomOverlay) {
                                    selectedOverlaySource.selectedOverlay = null
                                    selectedOverlaySource.selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
                                }
                                reloadOverlaySelector()
                            }
                        },
                        { wasCurrentOverlay ->
                            if (wasCurrentOverlay && selectedOverlaySource.selectedOverlay is CustomOverlay)
                                selectedOverlaySource.selectedOverlay = null
                            reloadOverlaySelector()
                        },
                    )
                    true
                }
            view.setOnClickListener {
                val oldOverlay = selectedOverlaySource.selectedOverlay

                // if active overlay was tapped, disable it
                if (oldOverlay == overlay || (oldOverlay is CustomOverlay && index == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0))) {
                    selectedOverlaySource.selectedOverlay = null
                } else {
                    // if other overlay was tapped, enable it
                    if (overlay.title == 0) {
                        prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, index!!)
                        selectedOverlaySource.selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
                    } else
                        selectedOverlaySource.selectedOverlay = overlay
                }
                reloadOverlaySelector()
            }
            view.layoutParams = params
            requireActivity().runOnUiThread { binding.overlayLayout.addView(view) }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key.startsWith("custom_overlay") && key != Prefs.CUSTOM_OVERLAY_SELECTED_INDEX)
            reloadOverlaySelector()
    }

    //region QuestsMapFragment - Callbacks from the map with its quest pins

    /* ---------------------------------- MapFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        binding.gpsTrackingButton.isActivated = mapFragment?.isFollowingPosition ?: false
        binding.gpsTrackingButton.isNavigation = mapFragment?.isNavigationMode ?: false
        binding.stopTracksButton.isVisible = mapFragment?.isRecordingTracks ?: false
        updateLocationPointerPin()
        mapFragment?.show3DBuildings = prefs.getBoolean(Prefs.SHOW_3D_BUILDINGS, true)
        mapFragment?.cameraPosition?.zoom?.let { updateCreateButtonEnablement(it) }
        listener?.onMapInitialized()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        binding.compassView.rotation = (180 * rotation / PI).toFloat()
        binding.compassView.rotationX = (180 * tilt / PI).toFloat()

        val margin = 2 * PI / 180
        binding.compassView.isInvisible = binding.otherQuestsScrollView.visibility == View.VISIBLE
            || (abs(rotation) < margin && tilt < margin)

        updateLocationPointerPin()
        updateCreateButtonEnablement(zoom)

        val f = bottomSheetFragment
        if (f is IsMapOrientationAware) f.onMapOrientation(rotation, tilt)
        if (f is IsMapPositionAware) f.onMapMoved(position)
    }

    override fun onPanBegin() {
        /* panning only results in not following location anymore if a location is already known
           and displayed
         */
        if (mapFragment?.displayedLocation != null) {
            setIsFollowingPosition(false)
        }
    }

    override fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float) { }

    override fun onLongPress(x: Float, y: Float) {
        val point = PointF(x, y)
        val position = mapFragment?.getPositionAt(point) ?: return
        if (bottomSheetFragment != null || editHistoryFragment != null) return

        binding.contextMenuView.translationX = x
        binding.contextMenuView.translationY = y

        showMapContextMenu(position)
    }

    /* ---------------------------- LocationAwareMapFragment.Listener --------------------------- */

    override fun onDisplayedLocationDidChange() {
        updateLocationPointerPin()
    }

    /* ---------------------------- QuestsMapFragment.Listener --------------------------- */

    override fun onClickedQuest(questKey: QuestKey) {
        if (isQuestDetailsCurrentlyDisplayedFor(questKey)) return
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { viewLifecycleScope.launch { showQuestDetails(questKey) } }
        } else {
            viewLifecycleScope.launch { showQuestDetails(questKey) }
        }
    }

    override fun onClickedEdit(editKey: EditKey) {
        editHistoryFragment?.select(editKey)
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        val f = childFragmentManager.fragments.lastOrNull()
        if (f is IsCloseableBottomSheet) {
            if (f != bottomSheetFragment && f is AbstractQuestForm && !f.onClickMapAt(position, clickAreaSizeInMeters))
                f.onClickClose { TagEditor.changes = StringMapChanges(emptySet()) }
            else if (f == bottomSheetFragment && !f.onClickMapAt(position, clickAreaSizeInMeters))
                f.onClickClose { closeBottomSheet() }
        } else if (editHistoryFragment != null) {
            closeEditHistorySidebar()
        }
    }

    override fun onClickedElement(elementKey: ElementKey) {
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { viewLifecycleScope.launch { showElementDetails(elementKey) } }
        } else {
            viewLifecycleScope.launch { showElementDetails(elementKey) }
        }
    }

    //endregion

    //region Buttons - Callbacks from the buttons in the main view

    /* ---------------------------- MainMenuButtonFragment.Listener ----------------------------- */

    override fun getDownloadArea(): BoundingBox? {
        val displayArea = mapFragment?.getDisplayedArea()
        if (displayArea == null) {
            context?.toast(R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG)
            return null
        }

        val enclosingBBox = displayArea.asBoundingBoxOfEnclosingTiles(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        val areaInSqKm = enclosingBBox.area() / 1000000
        if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
            context?.toast(R.string.download_area_too_big, Toast.LENGTH_LONG)
            return null
        }

        // below a certain threshold, it does not make sense to download, so let's enlarge it
        if (areaInSqKm < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM) {
            val cameraPosition = mapFragment?.cameraPosition
            if (cameraPosition != null) {
                val radius = sqrt(1000000 * ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM / PI)
                return cameraPosition.position.enclosingBoundingBox(radius)
            }
        }

        return enclosingBBox
    }

    /* ------------------------------ UndoButtonFragment.Listener ------------------------------- */

    override fun onClickShowEditHistory(allHidden: Boolean) {
        showEditHistorySidebar(allHidden)
    }

    //endregion

    //region Bottom Sheet - Callbacks from the bottom sheet (quest forms, split way form, create note form, ...)

    /* ------------------------------- AbstractOsmQuestForm.Listener ---------------------------- */
    /* -------------------------------- AbstractOverlayForm.Listener ---------------------------- */

    override val displayedMapLocation: Location? get() = mapFragment?.displayedLocation

    override val metersPerPixel: Double? get() = mapFragment?.getMetersPerPixel()

    override fun onEdited(editType: ElementEditType, geometry: ElementGeometry) {
        Log.i(TAG, "edited: ${editType.name}")
        showQuestSolvedAnimation(editType.icon, geometry.center)
        if (editType is OsmElementQuestType<*> && prefs.getBoolean(Prefs.SHOW_NEXT_QUEST_IMMEDIATELY, false)) {
            visibleQuestsSource.getAllVisible(geometry.center.enclosingBoundingBox(1.0))
                .filterIsInstance<OsmQuest>()
                .firstOrNull { it.geometry == geometry && it.type.dotColor == null } // this is not great, but we don't have key on the edited element any more
                ?.let { runBlocking { viewLifecycleScope.launch { showQuestDetails(it) } } }
                ?: closeBottomSheet()
        }
        closeBottomSheet()
    }

    override fun onComposeNote(editType: ElementEditType, element: Element, geometry: ElementGeometry, leaveNoteContext: String) {
        showInBottomSheet(
            LeaveNoteInsteadFragment.create(element.type, element.id, leaveNoteContext, geometry.center),
            false
        )
    }

    override fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry) {
        val mapFragment = mapFragment ?: return
        showInBottomSheet(SplitWayFragment.create(editType, way, geometry))
        mapFragment.highlightGeometry(geometry)
        mapFragment.hideNonHighlightedPins()
        mapFragment.hideOverlay()
    }

    override fun onQuestHidden(questKey: QuestKey) {
        closeBottomSheet()
    }

    override fun getPointOf(pos: LatLon): PointF? =
        mapFragment?.getPointOf(pos)

    override fun onEditTags(element: Element, geometry: ElementGeometry, questKey: QuestKey?, editTypeName: String?) {
        val f = TagEditor()
        if (f.arguments == null) f.arguments = bundleOf()
        val args = TagEditor.createArguments(element, geometry, mapFragment?.cameraPosition?.rotation, mapFragment?.cameraPosition?.tilt, questKey, editTypeName)
        f.requireArguments().putAll(args)
        binding.otherQuestsScrollView.visibility = View.GONE
        childFragmentManager.commit(true) {
            replace(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET)
            addToBackStack(BOTTOM_SHEET)
        }
    }

    /* ------------------------------- SplitWayFragment.Listener -------------------------------- */

    override fun onSplittedWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry) {
        showQuestSolvedAnimation(editType.icon, geometry.center)
        closeBottomSheet()
    }

    /* ------------------------------- MoveNodeFragment.Listener -------------------------------- */

    override fun onMoveNode(editType: ElementEditType, node: Node) {
        val ways = mapDataWithEditsSource.getWaysForNode(node.id)
        val relations = mapDataWithEditsSource.getRelationsForNode(node.id)
        if (ways.isNotEmpty() || relations.isNotEmpty()) {
            val multipolygons = relations.filter { it.tags["type"] == "multipolygon" }
            val message = if (ways.isNotEmpty() || multipolygons.isNotEmpty())
                getString(R.string.move_node_with_geometry, (ways + multipolygons).map { featureDictionaryFuture.get().byTags(it.tags).find().firstOrNull()?.name ?: it.tags }.toString())
            else
                getString(R.string.move_node_of_other_relation, relations.map { featureDictionaryFuture.get().byTags(it.tags).find().firstOrNull()?.name ?: it.tags }.toString())
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.general_warning)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.dialog_button_understood) { _,_ -> moveNode(editType, node) }
                .show()
        } else {
            moveNode(editType, node)
        }
    }

    private fun moveNode(editType: ElementEditType, node: Node) {
        val mapFragment = mapFragment ?: return
        showInBottomSheet(MoveNodeFragment.create(editType, node), clearPreviousHighlighting = false)
        mapFragment.clearSelectedPins()
        mapFragment.hideNonHighlightedPins()
        if (editType !is Overlay) {
            mapFragment.hideOverlay()
        }

        mapFragment.show3DBuildings = false
        val offsetPos = mapFragment.getPositionThatCentersPosition(node.position, RectF())
        mapFragment.updateCameraPosition { position = offsetPos }
    }

    override fun onMovedNode(editType: ElementEditType, position: LatLon) {
        showQuestSolvedAnimation(editType.icon, position)
        closeBottomSheet()
    }

    override fun getScreenPositionAt(mapPos: LatLon): PointF? =
        mapFragment?.getPointOf(mapPos)

    /* ------------------------------- ShowsPointMarkers -------------------------------- */

    override fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?,
        color: Int?,
        rotation: Double?
    ) {
        mapFragment?.putMarkerForCurrentHighlighting(geometry, drawableResId, title, color, rotation)
    }

    override fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry) {
        mapFragment?.deleteMarkerForCurrentHighlighting(geometry)
    }

    override fun clearMarkersForCurrentHighlighting() {
        mapFragment?.clearMarkersForCurrentHighlighting()
    }

    /* ------------------------------ NoteDiscussionForm.Listener ------------------------------- */

    override fun onNoteQuestSolved(questType: QuestType, noteId: Long, position: LatLon) {
        showQuestSolvedAnimation(questType.icon, position)
        closeBottomSheet()
    }

    override fun onNoteQuestClosed() {
        closeBottomSheet()
    }

    /* ------------------------------- CreateNoteFragment.Listener ------------------------------ */

    override fun onCreatedNote(position: LatLon) {
        Log.i(TAG, "created note at $position")
        showQuestSolvedAnimation(R.drawable.ic_quest_create_note, position)
        closeBottomSheet()
    }

    override fun getMapPositionAt(screenPos: PointF): LatLon? =
        mapFragment?.getPositionAt(screenPos)

    override fun getRecordedTrack(): List<Trackpoint>? =
        mapFragment?.recordedTracks

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ------------------------------ SelectedOverlaySource.Listener -----------------------------*/

    override fun onSelectedOverlayChanged() {
        viewLifecycleScope.launch {
            updateCreateButtonVisibility()
            reloadOverlaySelector()

            val f = bottomSheetFragment
            if (f is IsShowingElement) {
                closeBottomSheet()
            }
        }
    }

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread
    override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            // open quest has been deleted
            if (f is IsShowingQuestDetails && f.view != null && f.questKey in removed) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onVisibleQuestsInvalidated() {
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            if (f is IsShowingQuestDetails) {
                val openQuest = withContext(Dispatchers.IO) { visibleQuestsSource.get(f.questKey) }
                // open quest does not exist anymore after visible quest invalidation
                if (openQuest == null) closeBottomSheet()
            }
        }
    }

    /* ---------------------------- MapDataWithEditsSource.Listener ----------------------------- */

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            // open element has been deleted
            if (f is IsShowingElement && f.elementKey in deleted) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        if (view == null) return
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            if (f !is IsShowingElement) return@launch
            val elementKey = f.elementKey ?: return@launch
            val openElement = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) }
            // open element does not exist anymore after download
            if (openElement == null) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onCleared() {
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            if (f is IsShowingElement) {
                closeBottomSheet()
            }
        }
    }

    //endregion

    //region Edit History - Callbacks from the Edit History Sidebar

    override fun onSelectedEdit(edit: Edit) {
        val geometry = edit.getGeometry()
        mapFragment?.startFocus(geometry, mapOffsetWithOpenBottomSheet)
        mapFragment?.highlightGeometry(geometry)
        mapFragment?.highlightPins(edit.icon, listOf(edit.position))
        mapFragment?.hideOverlay()
    }

    private fun Edit.getGeometry(): ElementGeometry = when (this) {
        is ElementEdit -> originalGeometry
        is OsmQuestHidden -> mapDataWithEditsSource.getGeometry(elementType, elementId)
        else -> null
    } ?: ElementPointGeometry(position)

    override fun onDeletedSelectedEdit() {
        mapFragment?.endFocus()
        mapFragment?.clearHighlighting()
    }

    override fun onEditHistoryIsEmpty() {
        mapFragment?.endFocus()
        closeEditHistorySidebar()
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++ VIEW CONTROL ++++++++++++++++++++++++++++++++++++++ */

    //region Location - Request location and update location status

    private fun updateLocationAvailability(isAvailable: Boolean) {
        if (isAvailable) {
            onLocationIsEnabled()
        } else {
            onLocationIsDisabled()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationIsEnabled() {
        binding.gpsTrackingButton.state = LocationStateButton.State.SEARCHING
        mapFragment!!.startPositionTracking()

        setIsFollowingPosition(wasFollowingPosition ?: true)
        locationManager.getCurrentLocation()
    }

    private fun onLocationIsDisabled() {
        binding.gpsTrackingButton.state = when {
            requireContext().hasLocationPermission -> LocationStateButton.State.ALLOWED
            else -> LocationStateButton.State.DENIED
        }
        binding.gpsTrackingButton.isNavigation = false
        binding.locationPointerPin.visibility = View.GONE
        mapFragment!!.clearPositionTracking()
        locationManager.removeUpdates()
    }

    private fun onLocationChanged(location: Location) {
        viewLifecycleScope.launch {
            binding.gpsTrackingButton.state = LocationStateButton.State.UPDATING
            updateLocationPointerPin()
        }
    }

    //endregion

    //region Buttons - Functionality for the buttons in the main view

    fun onClickMainMenu() {
        mainMenuButtonFragment?.onClickMainMenu()
    }

    private fun onClickQuickSettings() {
        val popupMenu = PopupMenu(requireContext(), binding.quickSettingsButton)
        popupMenu.menu.add(Menu.NONE, 1, Menu.NONE, R.string.quick_switch_preset)
        popupMenu.menu.add(Menu.NONE, 2, Menu.NONE, R.string.level_filter)
        popupMenu.menu.add(Menu.NONE, 3, Menu.NONE, R.string.quick_switch_map_background)
        popupMenu.menu.add(Menu.NONE, 4, Menu.NONE, if (mapFragment?.isOrderReversed() == true) R.string.quest_order_normal else R.string.quest_order_reverse)
        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                1 -> showProfileSelectionDialog(requireContext(), questPresetsController, prefs)
                2 -> this.context?.let { levelFilter.showLevelFilterDialog(it) }
                3 -> prefs.putString(Prefs.THEME_BACKGROUND, if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP") "AERIAL" else "MAP")
                4 -> { viewLifecycleScope.launch { mapFragment?.reverseQuests() } }
            }
            true
        }
        popupMenu.show()
    }

    fun onClickZoomOut() {
        mapFragment?.updateCameraPosition(300) { zoomBy = -1f }
    }

    fun onClickZoomIn() {
        mapFragment?.updateCameraPosition(300) { zoomBy = +1f }
    }

    private fun onClickTracksStop() {
        // hide the track information
        binding.stopTracksButton.isVisible = false
        val mapFragment = mapFragment ?: return
        mapFragment.stopPositionTrackRecording()
        val pos = mapFragment.displayedLocation?.toLatLon() ?: return
        composeNote(pos, true)
    }

    private fun onClickCompassButton() {
        /* Clicking the compass button will always rotate the map back to north and remove tilt */
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition ?: return

        // if the user wants to rotate back north, it means he also doesn't want to use nav mode anymore
        if (mapFragment.isNavigationMode) {
            mapFragment.updateCameraPosition(300) { rotation = 0f }
            setIsNavigationMode(false)
        } else {
            mapFragment.updateCameraPosition(300) {
                rotation = 0f
                tilt = 0f
            }
        }
    }

    private fun onClickTrackingButton() {
        val mapFragment = mapFragment ?: return

        when {
            !binding.gpsTrackingButton.state.isEnabled -> {
                requestLocation()
            }
            !mapFragment.isFollowingPosition -> {
                setIsFollowingPosition(true)
            }
            else -> {
                if (!prefs.getBoolean(Prefs.DISABLE_NAVIGATION_MODE, false) || mapFragment.isNavigationMode)
                    setIsNavigationMode(!mapFragment.isNavigationMode)
            }
        }
    }

    private fun requestLocation() {
        (childFragmentManager.findFragmentByTag(TAG_LOCATION_REQUEST) as? LocationRequestFragment)?.startRequest()
    }

    private fun onClickCreateButton() {
        showOverlayFormForNewElement()
    }

    private fun updateCreateButtonVisibility() {
        val isCreateNodeEnabled = selectedOverlaySource.selectedOverlay?.isCreateNodeEnabled == true
        binding.createButton.isGone = !isCreateNodeEnabled
        binding.crosshairView.isGone = !isCreateNodeEnabled
    }

    private fun updateCreateButtonEnablement(zoom: Float) {
        binding.createButton.isEnabled = zoom >= 18f
    }

    private fun setIsNavigationMode(navigation: Boolean) {
        val mapFragment = mapFragment ?: return
        mapFragment.isNavigationMode = navigation
        binding.gpsTrackingButton.isNavigation = navigation
        // always re-center position because navigation mode shifts the center position
        mapFragment.centerCurrentPositionIfFollowing()
    }

    private fun setIsFollowingPosition(follow: Boolean) {
        val mapFragment = mapFragment ?: return
        mapFragment.isFollowingPosition = follow
        binding.gpsTrackingButton.isActivated = follow
        if (follow) mapFragment.centerCurrentPositionIfFollowing()
    }

    /* -------------------------------------- Context Menu -------------------------------------- */

    private fun showMapContextMenu(position: LatLon) {
        val popupMenu = PopupMenu(requireContext(), binding.contextMenuView)
        popupMenu.inflate(R.menu.menu_map_context)
        if (prefs.getBoolean(Prefs.EXPERT_MODE, false)) {
            popupMenu.menu.add(Menu.NONE, 4, 4, R.string.create_poi)
            popupMenu.menu.add(Menu.NONE, 5, 5, R.string.insert_node)
        }
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create_note -> onClickCreateNote(position)
                R.id.action_create_track -> onClickCreateTrack()
                R.id.action_open_location -> onClickOpenLocationInOtherApp(position)
                4 -> onClickAddPoi(position)
                5 -> {
                    mapFragment?.hideOverlay()
                    showInBottomSheet(InsertNodeFragment.create(position))
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun onClickOpenLocationInOtherApp(pos: LatLon) {
        val ctx = context ?: return

        val zoom = mapFragment?.cameraPosition?.zoom
        val uri = buildGeoUri(pos.latitude, pos.longitude, zoom)

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val otherMapAppInstalled = ctx.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .any { !it.activityInfo.packageName.equals(ctx.packageName) }
        if (otherMapAppInstalled) {
            startActivity(intent)
        } else {
            ctx.toast(R.string.map_application_missing, Toast.LENGTH_LONG)
        }
    }

    private fun onClickCreateNote(pos: LatLon) {
        if ((mapFragment?.cameraPosition?.zoom ?: 0f) < ApplicationConstants.NOTE_MIN_ZOOM) {
            context?.toast(R.string.create_new_note_unprecise)
            return
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) f.onClickClose { composeNote(pos) }
        else composeNote(pos)
    }

    private fun composeNote(pos: LatLon, hasGpxAttached: Boolean = false) {
        val mapFragment = mapFragment ?: return
        showInBottomSheet(CreateNoteFragment.create(hasGpxAttached))

        mapFragment.show3DBuildings = false
        val offsetPos = mapFragment.getPositionThatCentersPosition(pos, mapOffsetWithOpenBottomSheet)
        mapFragment.updateCameraPosition { position = offsetPos }
    }

    private fun onClickAddPoi(pos: LatLon) {
        if ((mapFragment?.cameraPosition?.zoom ?: 0f) < ApplicationConstants.NOTE_MIN_ZOOM) {
            context?.toast(R.string.create_new_note_unprecise)
            return
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) f.onClickClose { selectPoiType(pos) }
        else selectPoiType(pos)
    }

    private fun selectPoiType(pos: LatLon) {
        val country = countryBoundaries.get().getIds(pos.longitude, pos.latitude).firstOrNull()
        val defaultFeatureIds: List<String>? = prefs.getString(Prefs.CREATE_POI_RECENT_FEATURE_IDS, "")!!
            .split("§").filter { it.isNotBlank() }
            .ifEmpty { null } // null will show defaults, while empty list will not

        SearchFeaturesDialog(
            requireContext(),
            featureDictionaryFuture.get(),
            GeometryType.POINT,
            country,
            null, // pre-filled search text
            { true }, // filter, but we want everything
            { addPoi(pos, it) },
            false,
            defaultFeatureIds?.reversed(), // features shown without entering text
            pos,
        ).show()
    }

    private fun addPoi(pos: LatLon, feature: Feature) {
        showInBottomSheet(CreatePoiFragment.createFromFeature(feature, pos))

        // actually this could run again if tags are changed
        viewLifecycleScope.launch {
            val bbox = pos.enclosingBoundingBox(50.0)
            val data = withContext(Dispatchers.IO) { mapDataWithEditsSource.getMapDataWithGeometry(bbox) }
            val filter = if (IS_SHOP_EXPRESSION.matches(Node(0L, pos, feature.addTags))) IS_SHOP_EXPRESSION
            else "nodes, ways, relations with ${feature.tags
                    .map { if (it.value == "*") it.key else it.key + "=" + it.value }
                    .joinToString(" and ")}".toElementFilterExpression()
            val elements = data.filter { filter.matches(it) }

            for (e in elements) {
                // include only elements that fit with the currently active level filter
                if (!levelFilter.levelAllowed(e)) continue

                val geometry = data.getGeometry(e.type, e.id) ?: continue
                val icon = getPinIcon(featureDictionaryFuture.get(), e.tags)
                val title = getTitle(e.tags)
                putMarkerForCurrentHighlighting(geometry, icon, title)
            }
        }
        offsetPos(pos)
    }

    fun offsetPos(pos: LatLon) {
        val mapFragment = mapFragment ?: return
        mapFragment.show3DBuildings = false
        val offsetPos = mapFragment.getPositionThatCentersPosition(pos, mapOffsetWithOpenBottomSheet)
        mapFragment.updateCameraPosition { position = offsetPos }
    }

    private fun onClickCreateTrack() {
        val mapFragment = mapFragment ?: return
        mapFragment.startPositionTrackRecording()
        binding.stopTracksButton.isVisible = true
    }

    // ---------------------------------- Location Pointer Pin  --------------------------------- */

    private fun updateLocationPointerPin() {
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition ?: return
        val position = camera.position
        val rotation = camera.rotation

        val location = mapFragment.displayedLocation
        if (location == null) {
            binding.locationPointerPin.visibility = View.GONE
            return
        }
        val displayedPosition = LatLon(location.latitude, location.longitude)

        var target = mapFragment.getClippedPointOf(displayedPosition) ?: return
        windowInsets?.let {
            target -= PointF(it.left.toFloat(), it.top.toFloat())
        }
        val intersection = findClosestIntersection(binding.mapControls, target)
        if (intersection != null) {
            val intersectionPosition = mapFragment.getPositionAt(intersection)
            binding.locationPointerPin.isGone = intersectionPosition == null
            if (intersectionPosition != null) {
                val angleAtIntersection = position.initialBearingTo(intersectionPosition)
                binding.locationPointerPin.pinRotation = angleAtIntersection.toFloat() + (180 * rotation / PI).toFloat()

                val a = angleAtIntersection * PI / 180f + rotation
                val offsetX = (sin(a) / 2.0 + 0.5) * binding.locationPointerPin.width
                val offsetY = (-cos(a) / 2.0 + 0.5) * binding.locationPointerPin.height
                binding.locationPointerPin.x = intersection.x - offsetX.toFloat()
                binding.locationPointerPin.y = intersection.y - offsetY.toFloat()
            }
        } else {
            binding.locationPointerPin.visibility = View.GONE
        }
    }

    private fun onClickLocationPointer() {
        setIsFollowingPosition(true)
    }

    //endregion

    //region Edit History Sidebar

    private fun showEditHistorySidebar(allHidden: Boolean) {
        val appearAnim = R.animator.edit_history_sidebar_appear
        val disappearAnim = R.animator.edit_history_sidebar_disappear
        childFragmentManager.commit(true) {
            setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
            replace(R.id.edit_history_container, EditHistoryFragment(allHidden), EDIT_HISTORY)
            addToBackStack(EDIT_HISTORY)
        }
        mapFragment?.hideOverlay()
        mapFragment?.pinMode = if (allHidden) MainMapFragment.PinMode.HIDDEN_QUESTS else MainMapFragment.PinMode.EDITS
        historyBackPressedCallback.isEnabled = true
    }

    private fun closeEditHistorySidebar() {
        if (editHistoryFragment != null) {
            childFragmentManager.popBackStack(EDIT_HISTORY, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        clearHighlighting()
        mapFragment?.clearFocus()
        mapFragment?.pinMode = MainMapFragment.PinMode.QUESTS
        historyBackPressedCallback.isEnabled = false
    }

    //endregion

    //region Bottom Sheet - Controlling the bottom sheet and its interaction with the map

    /** Close bottom sheet, clear associated highlighting on the map and return to the previous
     *  view (e.g. if it was zoomed in before to focus on an element) */
    @UiThread
    private fun closeBottomSheet() {
        val showing = (bottomSheetFragment as? IsShowingElement)?.elementKey ?: (bottomSheetFragment as? IsShowingQuestDetails)?.questKey
        Log.i(TAG, "closeBottomSheet while showing $showing")
        activity?.currentFocus?.hideKeyboard()
        if (bottomSheetFragment != null) {
            childFragmentManager.popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            binding.otherQuestsLayout.removeAllViews()
            binding.otherQuestsScrollView.visibility = View.GONE
            binding.compassView.isInvisible = abs(binding.compassView.rotation) < 2 && binding.compassView.rotationX < 2
        }
        if (prefs.getBoolean(Prefs.OVERLAY_QUICK_SELECTOR, false))
            binding.overlayScrollView.isVisible = true
        clearHighlighting()
        unfreezeMap()
        mapFragment?.endFocus()
        sheetBackPressedCallback.isEnabled = false
    }

    /** Open or replace the bottom sheet. If the bottom sheet is replaces, no appear animation is
     *  played and the highlighting of the previous bottom sheet is cleared. */
    private fun showInBottomSheet(f: Fragment, clearPreviousHighlighting: Boolean = true) {
        activity?.currentFocus?.hideKeyboard()
        binding.overlayScrollView.isGone = true
        freezeMap()
        if (bottomSheetFragment != null && clearPreviousHighlighting) {
            clearHighlighting()
        }
        val appearAnim = R.animator.quest_answer_form_appear
        val disappearAnim = R.animator.quest_answer_form_disappear
        childFragmentManager.commit(true) {
            setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
            replace(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET)
            addToBackStack(BOTTOM_SHEET)
        }
        sheetBackPressedCallback.isEnabled = f is IsCloseableBottomSheet
    }

    /** Make the map not follow the user's location anymore temporarily */
    private fun freezeMap() {
        val mapFragment = mapFragment ?: return
        if (wasFollowingPosition == null) wasFollowingPosition = mapFragment.isFollowingPosition
        if (wasNavigationMode == null) wasNavigationMode = mapFragment.isNavigationMode
        mapFragment.isFollowingPosition = false
        mapFragment.isNavigationMode = false
    }

    /** Make the map follow the user's location again (if it was following before) */
    private fun unfreezeMap() {
        wasFollowingPosition?.let { mapFragment?.isFollowingPosition = it }
        wasNavigationMode?.let { mapFragment?.isNavigationMode = it }
        wasFollowingPosition = null
        wasNavigationMode = null
    }

    private fun clearHighlighting() {
        mapFragment?.clearHighlighting()
        mapFragment?.show3DBuildings = prefs.getBoolean(Prefs.SHOW_3D_BUILDINGS, true)
    }

    //endregion

    //region Bottom sheets

    @UiThread
    private fun showOverlayFormForNewElement() {
        val overlay = selectedOverlaySource.selectedOverlay ?: return
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition
        if (overlay is CustomOverlay) {
            val pos = camera?.position ?: return
            showInBottomSheet(CreatePoiFragment.createWithPrefill(prefs.getString(Prefs.CUSTOM_OVERLAY_IDX_FILTER, "")!!.substringAfter("with "), pos))
            mapFragment.show3DBuildings = false
            return
        }

        val f = overlay.createForm(null) ?: return
        if (f.arguments == null) f.arguments = bundleOf()
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        val args = AbstractOverlayForm.createArguments(overlay, null, null, rotation, tilt)
        f.requireArguments().putAll(args)

        showInBottomSheet(f)
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private suspend fun showElementDetails(elementKey: ElementKey) {
        Log.i(TAG, "showElementDetails for $elementKey")
        if (isElementCurrentlyDisplayed(elementKey)) return
        val overlay = selectedOverlaySource.selectedOverlay ?: return
        val geometry = mapDataWithEditsSource.getGeometry(elementKey.type, elementKey.id) ?: return
        val mapFragment = mapFragment ?: return

        // open note if it is blocking element
        val center = geometry.center
        val note = withContext(Dispatchers.IO) {
            notesSource
                .getAll(BoundingBox(center, center).enlargedBy(1.2)).filterNot { it.isClosed }
                .firstOrNull { it.position.truncateTo5Decimals() == center.truncateTo5Decimals() }
        }
        if (note != null) {
            showQuestDetails(OsmNoteQuest(note.id, note.position))
            return
        }

        val element = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) } ?: return
        val f = overlay.createForm(element) ?: return
        if (f.arguments == null) f.arguments = bundleOf()

        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        val args = AbstractOverlayForm.createArguments(overlay, element, geometry, rotation, tilt)
        f.requireArguments().putAll(args)

        showInBottomSheet(f)

        mapFragment.highlightGeometry(geometry)
        mapFragment.highlightPins(overlay.icon, listOf(geometry.center))
        mapFragment.hideNonHighlightedPins()
    }

    private fun isElementCurrentlyDisplayed(elementKey: ElementKey): Boolean {
        val f = bottomSheetFragment
        if (f !is IsShowingElement) return false
        return f.elementKey == elementKey
    }

    private suspend fun showQuestDetails(questKey: QuestKey) {
        val quest = visibleQuestsSource.get(questKey)
        if (quest != null) {
            showQuestDetails(quest)
        }
    }

    @UiThread
    private suspend fun showQuestDetails(quest: Quest) {
        Log.i(TAG, "showQuestDetails for ${quest.key}")
        val mapFragment = mapFragment ?: return
        if (isQuestDetailsCurrentlyDisplayedFor(quest.key)) return

        val f = quest.type.createForm()
        if (f.arguments == null) f.arguments = bundleOf()

        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        val args = AbstractQuestForm.createArguments(quest.key, quest.type, quest.geometry, rotation, tilt)
        f.requireArguments().putAll(args)

        val element = if (quest is OsmQuest) withContext(Dispatchers.IO) {
            val e = mapDataWithEditsSource.get(quest.elementType, quest.elementId)
            if (e == null) // this sometimes occurred in tests... until reason is found, just remove the quest
                osmQuestController.delete(quest.key)
            e
        } ?: return
            else null
        val highlightedElementMarkers = viewLifecycleScope.async(Dispatchers.IO) { getHighlightedElements(quest, element) }
        val otherQuestMarkers = viewLifecycleScope.async(Dispatchers.IO) { showOtherQuests(quest) }
        if (quest is OsmQuest) {
            val osmArgs = AbstractOsmQuestForm.createArguments(element!!)
            f.requireArguments().putAll(osmArgs)

            showInBottomSheet(f)
        } else {
            showInBottomSheet(f)
        }

        mapFragment.startFocus(quest.geometry, mapOffsetWithOpenBottomSheet)
        mapFragment.highlightGeometry(quest.geometry)
        mapFragment.highlightPins(quest.type.icon, quest.markerLocations)
        mapFragment.hideNonHighlightedPins()
        mapFragment.hideOverlay()

        viewLifecycleScope.launch(Dispatchers.IO) {
            val markers = mergeMarkersAtSamePosition(highlightedElementMarkers.await(), otherQuestMarkers.await())
            markers.forEach { mapFragment.putMarkerForCurrentHighlighting(it.geometry, it.drawableResId, it.title, it.color) }
        }
    }

    // if quest and highlight marker at same position, set color of highlight marker to quest color
    private fun mergeMarkersAtSamePosition(highlightMarkers: List<Marker>, questMarkers: List<Marker>): List<Marker> {
        // creating a map of possibly many markers may not be the fastest thing... but still ok i guess
        val m = hashMapOf<LatLon, Marker>()
        highlightMarkers.associateByTo(m) { it.geometry.center }
        questMarkers.forEach { questMarker ->
            val highlightMarker = m[questMarker.geometry.center]
            if (highlightMarker == null) {
                m[questMarker.geometry.center] = questMarker
                return@forEach
            }
            m[questMarker.geometry.center] = highlightMarker.copy(color = questMarker.color)
        }
        return m.values.toList()
    }

    private fun getHighlightedElements(quest: Quest, element: Element? = null): List<Marker> {
        val bbox = when (quest) {
            is OsmQuest -> quest.geometry.getBounds().enlargedBy(quest.type.highlightedElementsRadius)
            is ExternalSourceQuest -> quest.geometry.getBounds().enlargedBy(quest.type.highlightedElementsRadius)
            else -> return emptyList()
        }
        var mapData: MapDataWithGeometry? = null
        val markers = mutableListOf<Marker>()

        fun getMapData(): MapDataWithGeometry {
            val data = mapDataWithEditsSource.getMapDataWithGeometry(bbox)
            if (data is MutableMapDataWithGeometry && element is Way && !data.isWayComplete(element.id)) {
                // complete way to show stuff along it
                mapDataWithEditsSource.getWayComplete(element.id)?.nodes?.forEach {
                    data.put(it, ElementPointGeometry(it.position))
                }
            }
            mapData = data
            return data
        }

        val elements =
            when (quest) {
                is OsmQuest -> element?.let { quest.type.getHighlightedElements(it, ::getMapData) } ?: emptySequence()
                is ExternalSourceQuest -> quest.type.getHighlightedElements(::getMapData)
                else -> emptySequence()
            }
        if (elements == emptySequence<Element>()) return emptyList()
        val levels = element?.let { createLevelsOrNull(it.tags) }
        val localLanguages = ConfigurationCompat.getLocales(resources.configuration).toList().map { it.language }
        for (e in elements) {
            // don't highlight "this" element
            if (element == e) continue
            // include only elements with the same (=intersecting) level, if any
            val eLevels = createLevelsOrNull(e.tags)
            if (!levels.levelsIntersect(eLevels)) continue
            // include only elements with the same layer, if any (except for bridges)
            if (element?.tags?.get("layer") != e.tags["layer"] && e.tags["bridge"] == null) continue

            val geometry = mapData?.getGeometry(e.type, e.id) ?: continue
            val icon = getPinIcon(featureDictionaryFuture.get(), e.tags)
            val title = getTitle(e.tags, localLanguages)
            markers.add(Marker(geometry, icon, title))
        }
        return markers
    }

    private fun showOtherQuests(quest: Quest): List<Marker> {
        if (prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0) == 0) return emptyList()

        // Quests should be grouped by element key, so non-OsmQuests need some kind of fake key
        fun Quest.thatKey() = if (this is OsmQuest) ElementKey(elementType, elementId)
            else ElementKey(ElementType.values()[abs(key.hashCode() % 3)], -abs(7 * key.hashCode()).toLong())

        val markers = mutableListOf<Marker>()

        val quests = visibleQuestsSource.getNearbyQuests(quest, prefs.getFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, 0.0f).toDouble() + 0.01)
            .filterNot { it == quest || it.type.dotColor != null } // ignore current quest and poi dots
            .sortedBy { it.thatKey() != quest.thatKey() }
        if (quests.isEmpty()) return emptyList()

        val questsAndColorByElement = mutableMapOf<ElementKey, Pair<Int, MutableList<Quest>>>()
        val colors = arrayOf(Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.BLUE, ColorUtils.blendARGB(Color.RED, Color.YELLOW, 0.5f))
        var colorIterator = colors.iterator()
        quests.forEach {
            questsAndColorByElement.getOrPut(it.thatKey()) {
                val color = if (it.thatKey() == quest.thatKey()) Color.WHITE // no color for other quests of the selected element
                    else colorIterator.next()
                if (!colorIterator.hasNext()) colorIterator = colors.iterator() // cycle through color list if there are many elements
                if (color != Color.WHITE)
                    markers.add(Marker(it.geometry, color = color))
                Pair(color, mutableListOf())
            }.second.add(it)
        }
        val params = ViewGroup.LayoutParams(requireContext().dpToPx(54).toInt(), requireContext().dpToPx(54).toInt())
        activity?.runOnUiThread {
            questsAndColorByElement.values.forEach {
                val color = it.first
                it.second.forEach { q ->
                    val questView = ImageView(context).apply {
                        layoutParams = params
                        scaleX = 0.95f
                        scaleY = 0.95f
                        setOnClickListener {
                            binding.otherQuestsLayout.removeAllViews()
                            viewLifecycleScope.launch { showQuestDetails(q) }
                        }

                        // create layerDrawable from quest icon and ring
                        val ring = ContextCompat.getDrawable(context, R.drawable.pin_selection_ring)!! // thanks google for not providing documentation WHEN this can be null... is it instead of resourceNotFoundException?
                        ring.colorFilter = if (color == Color.WHITE) null
                            else PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                        val icon = ContextCompat.getDrawable(context, q.type.icon)!!
                        icon.colorFilter = PorterDuffColorFilter(ColorUtils.blendARGB(color, Color.WHITE, 0.8f), PorterDuff.Mode.MULTIPLY)
                        setImageDrawable(LayerDrawable(arrayOf(icon, ring)))
                    }
                    binding.otherQuestsLayout.addView(questView)
                }
            }
            binding.compassView.isInvisible = true
            binding.otherQuestsScrollView.fullScroll(View.FOCUS_UP) // scroll up when the quest changes
            binding.otherQuestsScrollView.visibility = View.VISIBLE
        }
        return markers
    }

    private fun isQuestDetailsCurrentlyDisplayedFor(questKey: QuestKey): Boolean {
        val f = bottomSheetFragment
        return f is IsShowingQuestDetails && f.questKey == questKey
    }

    //endregion

    //region Animation - Animation(s) for when a quest is solved

    private fun showQuestSolvedAnimation(iconResId: Int, position: LatLon) {
        if (!prefs.getBoolean(Prefs.SHOW_SOLVED_ANIMATION, true)) return
        val ctx = context ?: return
        val offset = view?.getLocationInWindow() ?: return
        val startPos = mapFragment?.getPointOf(position) ?: return

        val size = ctx.dpToPx(42).toInt()
        startPos.x += offset.x - size / 2f
        startPos.y += offset.y - size * 1.5f

        showMarkerSolvedAnimation(iconResId, startPos)
    }

    private fun showMarkerSolvedAnimation(@DrawableRes iconResId: Int, startScreenPos: PointF) {
        val ctx = context ?: return
        val activity = activity ?: return
        val view = view ?: return

        viewLifecycleScope.launch {
            soundFx.play(resources.getIdentifier("plop" + Random.nextInt(4), "raw", ctx.packageName))
        }

        val root = activity.window.decorView as ViewGroup
        val img = EffectQuestPlopBinding.inflate(layoutInflater, root, false).root
        img.x = startScreenPos.x
        img.y = startScreenPos.y
        img.setImageResource(iconResId)
        root.addView(img)

        val answerTarget = view.findViewById<View>(
            if (isAutosync) R.id.answers_counter_fragment else R.id.upload_button_fragment
        )
        flingQuestMarkerTo(img, answerTarget) { root.removeView(img) }
    }

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getStringOrNull(Prefs.AUTOSYNC) ?: "ON") == Prefs.Autosync.ON

    private fun flingQuestMarkerTo(quest: View, target: View, onFinished: () -> Unit) {
        val targetPos = target.getLocationInWindow().toPointF()
        quest.animate()
            .scaleX(1.6f).scaleY(1.6f)
            .setInterpolator(OvershootInterpolator(8f))
            .setDuration(250)
            .withEndAction {
                quest.animate()
                    .scaleX(0.2f).scaleY(0.2f)
                    .alpha(0.8f)
                    .x(targetPos.x).y(targetPos.y)
                    .setDuration(250)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction(onFinished)
            }
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++++ INTERFACE +++++++++++++++++++++++++++++++++++++++ */

    //region Interface - For the parent fragment / activity

    fun getCameraPosition(): CameraPosition? {
        return mapFragment?.cameraPosition
    }

    fun setCameraPosition(position: LatLon, zoom: Float) {
        mapFragment?.isFollowingPosition = false
        mapFragment?.isNavigationMode = false
        mapFragment?.setInitialCameraPosition(CameraPosition(position, 0f, 0f, zoom))
        setIsFollowingPosition(false)
    }

    //endregion

    companion object {
        private const val BOTTOM_SHEET = "bottom_sheet"
        private const val EDIT_HISTORY = "edit_history"

        private const val TAG_LOCATION_REQUEST = "LocationRequestFragment"
    }
}

private data class Marker(
    val geometry: ElementGeometry,
    val drawableResId: Int? = null,
    val title: String? = null,
    val color: Int? = null,
)

private const val TAG = "MainFragment" +
    ""
