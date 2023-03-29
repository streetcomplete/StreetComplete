package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.content.edit
import androidx.core.os.bundleOf
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
import de.westnordost.streetcomplete.data.osm.edits.insert.InsertBetween
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.databinding.FragmentSplitWayBinding
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.screens.main.map.getPinIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.util.ktx.forEachLine
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.alongTrackDistanceTo
import de.westnordost.streetcomplete.util.math.crossTrackDistanceTo
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.distanceToArcs
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.measuredLength
import de.westnordost.streetcomplete.util.math.pointOnPolylineFromStart
import de.westnordost.streetcomplete.util.viewBinding
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
import kotlin.math.abs

/** Fragment that lets the user split an OSM way */
class InsertNodeFragment :
    Fragment(R.layout.fragment_split_way), IsCloseableBottomSheet {

    private var insertLocation: Triple<LatLon, InsertBetween, Way>? = null

    private val binding by viewBinding(FragmentSplitWayBinding::bind)

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))
    private val countryBoundaries: FutureTask<CountryBoundaries> by inject(named("CountryBoundariesFuture"))
    private val prefs: SharedPreferences by inject()

    private val isFormComplete get() = insertLocation != null

    private val showsGeometryMarkersListener: ShowsGeometryMarkers? get() =
        parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers
    private val initialMap = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        if (savedInstanceState == null) {
            binding.speechbubbleContentContainer.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.inflate_answer_bubble)
            )
        }

        val args = requireArguments()
        onClickMapAt(Json.decodeFromString(args.getString(ARG_POS)!!), CLICK_AREA_SIZE_AT_MAX_ZOOM) // start with position already clicked
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // see rant comment in AbstractBottomSheetFragment
        resources.updateConfiguration(newConfig, resources.displayMetrics)

        binding.bottomSheetContainer.updateLayoutParams { width = resources.getDimensionPixelSize(R.dimen.quest_form_width) }
    }

    private fun onClickOk() {
        val il = insertLocation ?: return
        val fd = featureDictionaryFuture.get()
        val country = countryBoundaries.get().getIds(il.first.longitude, il.first.latitude).firstOrNull()
        val defaultFeatureIds = prefs.getString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, "")!!
            .split("§").filter { it.isNotBlank() }
            .ifEmpty { listOf("amenity/post_box", "highway/crossing/unmarked", "barrier/bollard", "traffic_calming/table") }

        // also allow empty somehow?
        SearchFeaturesDialog(
            requireContext(),
            fd,
            GeometryType.VERTEX,
            country,
            null, // pre-filled search text
            { true }, // filter, but we want everything
            { onSelectedFeature(it, il) },
            defaultFeatureIds.reversed(), // features shown without entering text
            il.first,
        ).show()
        restoreBackground()
    }

    private fun onSelectedFeature(feature: Feature, insertLocation: Triple<LatLon, InsertBetween, Way>) {
        viewLifecycleScope.launch {
            val recentFeatureIds = prefs.getString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, "")!!.split("§").toMutableList()
            if (recentFeatureIds.lastOrNull() != feature.id) {
                recentFeatureIds.remove(feature.id)
                recentFeatureIds.add(feature.id)
                prefs.edit().putString(Prefs.INSERT_NODE_RECENT_FEATURE_IDS, recentFeatureIds.takeLast(10).joinToString("§")).apply()
            }
            val mapData = mapDataSource.getMapDataWithGeometry(insertLocation.first.enclosingBoundingBox(20.0))
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
        val f = InsertNodeTagEditor.create(insertLocation.first, feature, insertLocation.second, insertLocation.third)
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

    @UiThread
    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        val mapData = mapDataSource.getMapDataWithGeometry(position.enclosingBoundingBox(50.0))
        val waysAndGeometries = mapData.ways.mapNotNull {
            val geo = mapData.getWayGeometry(it.id) ?: return@mapNotNull null
            it to geo
        }
        val closeWays = hashSetOf<Triple<Way, List<List<LatLon>>, Double>>()
        waysAndGeometries.forEach {
            val geoLines = when (it.second) {
                is ElementPolylinesGeometry -> (it.second as ElementPolylinesGeometry).polylines
                is ElementPolygonsGeometry -> (it.second as ElementPolygonsGeometry).polygons
                else -> return@forEach
            }
            val distance = geoLines.minOf { position.distanceToArcs(it) }
            if (distance <= clickAreaSizeInMeters)
                closeWays.add(Triple(it.first, geoLines, distance))
        }
        if (closeWays.isEmpty()) {
            binding.speechbubbleContentContainer.findViewById<TextView>(R.id.contentText)?.setText(R.string.insert_node_select_way)
            insertLocation = null
            showsGeometryMarkersListener?.clearMarkersForCurrentHighlighting()
            animateButtonVisibilities()
            return true
        }
        // quite often we have several ways using the same nodes
        // prefer highways, then barriers
        val minDistance = closeWays.minOf { it.third }
        closeWays.removeAll { it.third > minDistance + 0.001 } // without the 0.001 this is not reliable
        val closestWay = closeWays.firstOrNull { it.first.tags.containsKey("highway") }
            ?: closeWays.firstOrNull { it.first.tags.containsKey("barrier") } ?: closeWays.first()

        val result = mutableSetOf<Pair<LatLon, InsertBetween>>()
        closestWay.second.forEach { it.forEachLine { first, second ->
            val crossTrackDistance = abs(position.crossTrackDistanceTo(first, second))
            if (clickAreaSizeInMeters > crossTrackDistance) {
                val alongTrackDistance = position.alongTrackDistanceTo(first, second)
                val distance = first.distanceTo(second)
                if (distance > alongTrackDistance && alongTrackDistance > 0) {
                    val delta = alongTrackDistance / distance
                    val line = listOf(first, second)
                    val positionOnLine = line.pointOnPolylineFromStart(line.measuredLength() * delta)!!
                    result.add(positionOnLine to InsertBetween(first, second))
                }
            }
        } }
        val here = result.minByOrNull { position.distanceTo(it.first) }
        if (here == null) { // actually this should not happen
            binding.speechbubbleContentContainer.findViewById<TextView>(R.id.contentText)?.setText(R.string.insert_node_select_way)
            insertLocation = null
            showsGeometryMarkersListener?.clearMarkersForCurrentHighlighting()
            animateButtonVisibilities()
            return true
        }
        insertLocation = Triple(here.first, here.second, closestWay.first)

        binding.speechbubbleContentContainer.findViewById<TextView>(R.id.contentText)?.text =
            closestWay.first.tags.map { "${it.key} = ${it.value}" }.sorted().joinToString("\n")
        animateButtonVisibilities()

        // highlight way and position
        viewLifecycleScope.launch {
            showsGeometryMarkersListener?.clearMarkersForCurrentHighlighting()
            showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(
                ElementPointGeometry(here.first),
                R.drawable.crosshair_marker,
                null
            )
            showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(
                mapData.getWayGeometry(closestWay.first.id)!!,
                null,
                null
            )
            closestWay.first.nodeIds.forEach {
                val node = mapData.getNode(it) ?: return@forEach
                if (node.tags.isEmpty()) return@forEach
                showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(
                    ElementPointGeometry(node.position),
                    getPinIcon(node.tags),
                    getTitle(node.tags)
                )
            }
        }
        return true
    }

    @UiThread override fun onClickClose(onConfirmed: () -> Unit) {
        restoreBackground()
        onConfirmed()
    }

    private fun animateButtonVisibilities() {
        if (isFormComplete) binding.okButton.popIn() else binding.okButton.popOut()
    }

    companion object {
        private const val CLICK_AREA_SIZE_AT_MAX_ZOOM = 2.6

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
