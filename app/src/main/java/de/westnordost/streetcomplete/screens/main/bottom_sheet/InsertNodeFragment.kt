package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.content.edit
import androidx.core.graphics.toRectF
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.databinding.FragmentInsertNodeBinding
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.screens.main.map.getPinIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import de.westnordost.streetcomplete.view.RoundRectOutlineProvider
import de.westnordost.streetcomplete.view.dialogs.SearchFeaturesDialog
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.util.concurrent.FutureTask

/** Fragment that lets the user split an OSM way */
class InsertNodeFragment :
    Fragment(R.layout.fragment_split_way), IsCloseableBottomSheet, IsMapPositionAware {

    private var _binding: FragmentInsertNodeBinding? = null
    private val binding get() = _binding!!

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val prefs: SharedPreferences by inject()
    private val levelFilter: LevelFilter by inject()

    private val isFormComplete get() = positionOnWay != null

    private val showsGeometryMarkersListener: ShowsGeometryMarkers? get() =
        parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers
    private val overlayFormListener: AbstractOverlayForm.Listener? get() = parentFragment as? AbstractOverlayForm.Listener ?: activity as? AbstractOverlayForm.Listener
    private val initialMap = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
    private val tagsText by lazy { binding.speechbubbleContentContainer.findViewById<TextView>(R.id.contentText).apply {
        maxLines = 10
        isClickable = true
        scrollBarFadeDuration = 0
        movementMethod = ScrollingMovementMethod()
    } }
    private val mapFragment by lazy {
        (parentFragment as? MainFragment)?.childFragmentManager?.fragments?.filterIsInstance<MainMapFragment>()?.singleOrNull()
    }
    private lateinit var mapData: MapDataWithGeometry
    private lateinit var ways: List<Pair<Way, List<LatLon>>>
    private var positionOnWay: PositionOnWay? = null
        set(value) {
            field = value
            setMarkerPosition(value?.position)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsertNodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createMarker.doOnLayout { setMarkerPosition(null) }
        binding.bottomSheetContainer.respectSystemInsets(View::setMargins)

        binding.okButton.setOnClickListener { onClickOk() }
        binding.cancelButton.setOnClickListener { activity?.onBackPressed() }

        binding.undoButton.isInvisible = true
        binding.okButton.isInvisible = !isFormComplete
        binding.mapButton.setOnClickListener { toggleBackground() }
        updateMapButtonText()

        val cornerRadius = resources.getDimension(R.dimen.speech_bubble_rounded_corner_radius)
        val margin = resources.getDimensionPixelSize(R.dimen.horizontal_speech_bubble_margin)
        binding.speechbubbleContentContainer.outlineProvider = RoundRectOutlineProvider(
            cornerRadius, margin, margin, margin, margin
        )

        val args = requireArguments()
        val pos: LatLon = Json.decodeFromString(args.getString(ARG_POS)!!)
        getMapData(pos)

        if (savedInstanceState == null) {
            binding.speechbubbleContentContainer.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.inflate_answer_bubble)
            )
        }
        val offsetRect = Rect( // slightly lower position of marker than usual
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset) / 2
        ).toRectF()
        mapFragment?.getPositionThatCentersPosition(pos, offsetRect)
            ?.let { mapFragment?.updateCameraPosition { position = it } }

        onMapMoved(pos)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // see rant comment in AbstractBottomSheetFragment
        resources.updateConfiguration(newConfig, resources.displayMetrics)

        binding.bottomSheetContainer.updateLayoutParams { width = resources.getDimensionPixelSize(R.dimen.quest_form_width) }
    }

    private fun onClickOk() {
        val pow = positionOnWay ?: return
        val fd = featureDictionaryFuture.get()
        val country = countryBoundaries.get().getIds(pow.position.longitude, pow.position.latitude).firstOrNull()
        val defaultFeatureIds = prefs.getString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, "")!!
            .split("§").filter { it.isNotBlank() }
            .ifEmpty { listOf("amenity/post_box", "barrier/gate", "highway/crossing/unmarked", "highway/crossing/uncontrolled", "highway/traffic_signals", "barrier/bollard", "traffic_calming/table") }

        // also allow empty somehow?
        SearchFeaturesDialog(
            requireContext(),
            fd,
            GeometryType.VERTEX,
            country,
            null, // pre-filled search text
            { true }, // filter, but we want everything
            { onSelectedFeature(it, pow) },
            false,
            defaultFeatureIds.reversed(), // features shown without entering text
            pow.position,
        ).show()
        restoreBackground()
    }

    private fun onSelectedFeature(feature: Feature, positionOnWay: PositionOnWay) {
        viewLifecycleScope.launch {
            val recentFeatureIds = prefs.getString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, "")!!.split("§").toMutableList()
            if (recentFeatureIds.lastOrNull() != feature.id) {
                recentFeatureIds.remove(feature.id)
                recentFeatureIds.add(feature.id)
                prefs.edit().putString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, recentFeatureIds.takeLast(25).joinToString("§")).apply()
            }
            showsGeometryMarkersListener?.putMarkerForCurrentHighlighting( // currently not done, but still need it
                ElementPointGeometry(positionOnWay.position),
                R.drawable.crosshair_marker,
                null
            )
            val mapData = mapDataSource.getMapDataWithGeometry(positionOnWay.position.enclosingBoundingBox(30.0))
            val nearbySimilarElements = mapData.filter { e -> feature.tags.all { e.tags[it.key] == it.value } }
            nearbySimilarElements.forEach {
                val geo = mapData.getGeometry(it.type, it.id) ?: return@forEach
                showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(
                    geo,
                    getPinIcon(it.tags),
                    getTitle(it.tags)
                )
            }
        }
        val f = InsertNodeTagEditor.create(positionOnWay, feature)
        parentFragmentManager.commit {
            replace(id, f, "bottom_sheet")
            addToBackStack("bottom_sheet")
        }
    }

    private fun toggleBackground() {
        prefs.edit { putString(Prefs.THEME_BACKGROUND, if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP") "AERIAL" else "MAP") }
        updateMapButtonText()
    }

    private fun updateMapButtonText() {
        val isMap = prefs.getString(Prefs.THEME_BACKGROUND, "MAP") == "MAP"
        val textId = if (isMap) R.string.background_type_aerial_esri else R.string.background_type_map
        binding.mapButton.setText(textId)
    }

    private fun restoreBackground() {
        if (prefs.getString(Prefs.THEME_BACKGROUND, "MAP") != initialMap)
            prefs.edit { putString(Prefs.THEME_BACKGROUND, initialMap) }
    }

    override fun onMapMoved(position: LatLon) {
        newPosition(position)
    }

    private fun newPosition(position: LatLon, forceMoveMarker: Boolean = false) {
        if (position !in mapData.boundingBox!!)
            getMapData(position)
        val metersPerPixel = overlayFormListener?.metersPerPixel ?: return
        val maxDistance = metersPerPixel * requireContext().dpToPx(24)
        val snapToVertexDistance = maxDistance / 2

        // todo: switch to something similar that possibly allows inserting a node into multiple ways
        positionOnWay = if (forceMoveMarker) position.getPositionOnWays(ways, maxDistance, snapToVertexDistance)
            else getDefaultMarkerPosition()?.getPositionOnWays(ways, maxDistance, snapToVertexDistance)
        val pow = positionOnWay
        if (pow == null) {
            noWaySelected()
            return
        }
        val ways = when (pow) {
            is PositionOnWaySegment -> listOf(mapData.getWay(pow.wayId)!!)
            is VertexOfWay -> pow.wayIds.map { mapData.getWay(it)!! }
        }

        // todo:
        //  positionOnWay should be changed a bit, because I want to allow multiple ways
        //   also snap to crossings that don't have a node?
        //    how to really do? always snap to parallel way segments, and optionally snap to crossings?
        //   don't snap to nodes with tags? or show the tags?
        //    this could be an option in getPositionOnWays?
        //   adding another sub-class should be fine i think, just make sure that it's accessible only if an option was set
        //  description text needs to be adjusted in here (also containing multiple ways)
        //   and a way to remove single ways out of these
        //   maybe sth like list of ways on top, and tap to show tags below (and highlight tags for which way are shown)
        //  existing tags of nodes need to be shown!

        if (ways.isEmpty()) { // actually this should never happen
            noWaySelected()
            return
        }
        // view:
        //  tag view on bottom, like now (but with other text)
        //  element view on top, contains
        //   node if it has tags
        //   each way
        //   on click, show tags
        //   switch (or delete button?) on right side to not include this way if it's possible (the new positionOnWay)
        val node = if (pow is VertexOfWay) mapData.getNode(pow.nodeId) else null
        val nodeTags = node?.tags?.takeIf { it.isNotEmpty() }
        val texts = ways.map { it.tags.map { "${it.key} = ${it.value}" }.sorted().joinToString("\n") }
        val text = texts.joinToString("\n\n")
        if (tagsText.text != text) {
            tagsText.text = text
            tagsText.scrollY = 0
        }

        // highlight ways and position
        viewLifecycleScope.launch {
            showsGeometryMarkersListener?.clearMarkersForCurrentHighlighting()
            // todo: no crosshair as we have the marker? but it may hide nodes...
            //  just change the marker to a crosshair?
//            showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(
//                ElementPointGeometry(position),
//                R.drawable.crosshair_marker,
//                null
//            )
            mapFragment?.highlightGeometries(ways.map { mapData.getWayGeometry(it.id)!! }) // todo: can't be null, right?
            ways.forEach { way ->
                way.nodeIds.forEach {
                    val node = mapData.getNode(it)!!
                    if (node.tags.isNotEmpty())
                        showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(
                            ElementPointGeometry(node.position),
                            getPinIcon(node.tags),
                            getTitle(node.tags)
                        )
                }
            }
        }
        animateButtonVisibilities()
    }

    private fun noWaySelected() {
        tagsText.setText(R.string.insert_node_select_way)
        showsGeometryMarkersListener?.clearMarkersForCurrentHighlighting()
        mapFragment?.clearHighlighting()
        animateButtonVisibilities()
    }

    private fun setMarkerPosition(position: LatLon?) {
        val point = if (position == null) {
            getDefaultMarkerScreenPosition()
        } else {
            overlayFormListener?.getPointOf(position)
        } ?: return
        binding.createMarker.x = point.x - binding.createMarker.width / 2
        binding.createMarker.y = point.y - binding.createMarker.height / 2
    }

    private fun getDefaultMarkerPosition(): LatLon? {
        val point = getDefaultMarkerScreenPosition() ?: return null
        return overlayFormListener?.getMapPositionAt(point)
    }

    private fun getDefaultMarkerScreenPosition(): PointF? {
        val view = view ?: return null
        val left = resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset)
        val right = resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset)
        val top = resources.getDimensionPixelSize(R.dimen.quest_form_topOffset)
        val bottom = resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset) / 2
        val x = (view.width + left - right) / 2f
        val y = (view.height + top - bottom) / 2f
        return PointF(x, y)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        newPosition(position, forceMoveMarker = true)
        return true
    }

    @UiThread override fun onClickClose(onConfirmed: () -> Unit) {
        restoreBackground()
        onConfirmed()
    }

    private fun getMapData(position: LatLon) {
        mapData = mapDataSource.getMapDataWithGeometry(position.enclosingBoundingBox(100.0))
        ways = mapData.ways.mapNotNull { way ->
            if (!levelFilter.levelAllowed(way)) return@mapNotNull null
            val positions = way.nodeIds.map { mapData.getNode(it)!!.position }
            way to positions
        }
    }

    private fun animateButtonVisibilities() {
        if (isFormComplete) binding.okButton.popIn() else binding.okButton.popOut()
    }

    companion object {
        private const val ARG_POS = "pos"

        fun create(position: LatLon): InsertNodeFragment {
            val f = InsertNodeFragment()
            f.arguments = bundleOf(
                ARG_POS to Json.encodeToString(position),
            )
            return f
        }
    }
}
