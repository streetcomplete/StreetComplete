package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.pin_circle
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.PinPublicationTracker
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.Instant
import kotlin.math.roundToInt

/** Deterministic debug scenario for profiling the production main-map style without touch input. */
@Composable
internal fun MapPerformanceScenario(
    questTypes: QuestTypeRegistry = koinInject(),
) {
    val state = rememberMainMapState()
    val icons = remember(questTypes) {
        questTypes.map { it.icon }.distinct().take(SCENARIO_ICON_COUNT)
            .ifEmpty { listOf(Res.drawable.pin_circle) }
    }
    val pinSnapshots = remember(icons) {
        listOf(
            PinSnapshot.Empty.updated(scenarioPins(icons, longitudeOffset = 0.0)),
            PinSnapshot.Empty.updated(scenarioPins(icons, longitudeOffset = 0.012)),
            PinSnapshot.Empty.updated(scenarioPins(icons, longitudeOffset = 0.024)),
        )
    }
    val panPinPages = remember(icons) {
        List(SCENARIO_PAN_PAGE_COUNT) { page ->
            scenarioPins(icons, longitudeOffset = 0.04 + page * 0.02)
        }
    }
    val viewModel = remember(panPinPages, icons) {
        ScenarioMainMapViewModel(panPinPages, icons)
    }
    val focusedGeometry = remember { scenarioFocusedGeometry() }
    val markers = remember(icons) { scenarioMarkers(icons) }
    val recorder = remember { MapPerformanceRecorder() }
    var phase by remember { mutableStateOf("startup") }
    var mapVisible by remember { mutableStateOf(true) }
    var locationRotation by remember { mutableStateOf<Float?>(0f) }

    DisposableEffect(viewModel) {
        onDispose(viewModel::close)
    }

    LaunchedEffect(recorder) {
        while (true) {
            withFrameNanos(recorder::onUiFrame)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.mapState.style.loadState }.collect { loadState ->
            Log.i("MapPerformanceScenario", "STYLE $loadState")
        }
    }

    LaunchedEffect(state, pinSnapshots) {
        Log.i("MapPerformanceScenario", "VERSION $MAP_PERFORMANCE_SCENARIO_VERSION")
        snapshotFlow { state.mapState.style.loadState }.first { it == StyleLoadState.Ready }

        suspend fun runPhase(
            name: String,
            duration: Duration,
            action: suspend () -> Unit,
        ) {
            recorder.begin(name)
            phase = name
            action()
            delay(duration)
        }

        state.setPinMode(MainMapPinMode.QUESTS)
        runPhase("warmup", 2.seconds) {
            state.moveTo(SCENARIO_CENTER, zoom = 13.25, duration = Duration.ZERO)
        }
        runPhase("load-352-pins-37-icons", 4.seconds) {
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude + 0.002, SCENARIO_CENTER.longitude + 0.002),
                zoom = 13.25,
                duration = 2.seconds,
            )
            val after = state.styleConfiguration.pinPublicationTracker.latestSequence
            viewModel.publishQuestPins(pinSnapshots[0])
            state.styleConfiguration.pinPublicationTracker.awaitPublished(pinSnapshots[0], after)
            Log.i(MapPerformanceRecorder.TAG, "QUEST_PINS_READY revision=${pinSnapshots[0].revision}")
        }
        runPhase("cluster-to-pins", 3.seconds) {
            state.moveTo(SCENARIO_CENTER, zoom = 16.0, duration = 1500.milliseconds)
        }
        runPhase("overlay-160-elements-8-icons", 3.seconds) {
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude - 0.002, SCENARIO_CENTER.longitude - 0.002),
                zoom = 16.0,
                duration = 2.seconds,
            )
            viewModel.selectOverlay()
            withTimeout(3.seconds) {
                viewModel.styleableElements.first {
                    it.size == SCENARIO_OVERLAY_ELEMENT_COUNT
                }
            }
        }
        runPhase("quest-open", 4.seconds) {
            // Match MainScreen's production selection burst. Marker derivation completes shortly
            // afterward on IO, while focus, selected pins, and visibility change together.
            state.startFocus(focusedGeometry)
            state.showGeometry(focusedGeometry)
            state.selectPins(icons.first(), listOf(SCENARIO_CENTER))
            state.hidePins()
            state.hideOverlay()
            delay(40.milliseconds)
            state.setMarkers(markers)
        }
        runPhase("selected-pan", 3.seconds) {
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude + 0.006, SCENARIO_CENTER.longitude + 0.012),
                zoom = 16.0,
                duration = 1500.milliseconds,
            )
        }
        runPhase("quest-close", 2.seconds) {
            state.clearHighlighting()
            state.endFocus()
        }
        runPhase("reload-known-icons", 3.seconds) {
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude + 0.004, SCENARIO_CENTER.longitude - 0.004),
                zoom = 16.0,
                duration = 2.seconds,
            )
            val after = state.styleConfiguration.pinPublicationTracker.latestSequence
            viewModel.publishQuestPins(pinSnapshots[1])
            state.styleConfiguration.pinPublicationTracker.awaitPublished(pinSnapshots[1], after)
        }
        runPhase("far-pan-source-loads", 2.seconds) {
            viewModel.enableViewportLoads()
            repeat(SCENARIO_PAN_PAGE_COUNT) { page ->
                state.moveTo(
                    LatLon(
                        SCENARIO_CENTER.latitude + if (page % 2 == 0) 0.01 else -0.01,
                        SCENARIO_CENTER.longitude + 0.04 + page * 0.02,
                    ),
                    zoom = 16.0,
                    duration = 500.milliseconds,
                )
                delay(650.milliseconds)
            }
            viewModel.disableViewportLoads()
        }
        runPhase("sustained-pan-source-soak", 1.seconds) {
            viewModel.enableViewportLoads()
            repeat(SCENARIO_SOAK_PAN_COUNT) { step ->
                val afterFrame = recorder.mapFrameSequence.value
                state.moveTo(
                    LatLon(
                        SCENARIO_CENTER.latitude + if (step % 2 == 0) 0.012 else -0.012,
                        SCENARIO_CENTER.longitude + 0.04 + step.mod(SCENARIO_PAN_PAGE_COUNT) * 0.02,
                    ),
                    zoom = 16.0,
                    duration = 700.milliseconds,
                )
                delay(650.milliseconds)
                if (step == SCENARIO_SOAK_PAN_COUNT - 1) {
                    recorder.awaitMapFrames(afterFrame, 3)
                }
            }
            viewModel.disableViewportLoads()
            Log.i(MapPerformanceRecorder.TAG, "SUSTAINED_PAN_PROBE_READY")
        }
        runPhase("location-heading-track-realistic", 1.seconds) {
            state.startTrackRecording()
            // A real map already has a location when recording starts. Seed it immediately so a
            // deliberately idle renderer is not misclassified as a 1-second map freeze while the
            // scenario waits for its first simulated GPS interval.
            state.onLocationEvent(scenarioLocationEvent(SCENARIO_CENTER, 0L))
            repeat(SCENARIO_REALISTIC_LOCATION_UPDATES) { update ->
                val fraction = (update + 1).toDouble() / SCENARIO_REALISTIC_LOCATION_UPDATES
                repeat(10) { headingUpdate ->
                    locationRotation = (update * 30f + headingUpdate * 3f) % 360f
                    delay(100.milliseconds)
                }
                state.onLocationEvent(
                    scenarioLocationEvent(
                        LatLon(
                            SCENARIO_CENTER.latitude + fraction * 0.004,
                            SCENARIO_CENTER.longitude + fraction * 0.006,
                        ),
                        (update + 1) * 1_000L,
                    )
                )
            }
        }
        runPhase("location-heading-track-stress", 1.seconds) {
            repeat(SCENARIO_STRESS_LOCATION_UPDATES) { update ->
                val fraction = update.toDouble() / SCENARIO_STRESS_LOCATION_UPDATES
                locationRotation = (update * 11f) % 360f
                if (update % 10 == 0) {
                    state.onLocationEvent(
                        scenarioLocationEvent(
                            LatLon(
                                SCENARIO_CENTER.latitude + fraction * 0.004,
                                SCENARIO_CENTER.longitude + fraction * 0.006,
                            ),
                            update * 33L,
                        )
                    )
                }
                delay(33.milliseconds)
            }
        }
        runPhase("track-stop", 2.seconds) {
            state.stopTrackRecording()
        }
        runPhase("base-style-reload", 3.seconds) {
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude - 0.005, SCENARIO_CENTER.longitude + 0.005),
                zoom = 16.0,
                duration = 2.seconds,
            )
            viewModel.publishQuestPins(pinSnapshots[2])
            state.mapState.style.baseStyle = BaseStyle.Json(
                streetCompleteBaseStyle().replace(
                    "\"name\": \"StreetComplete\"",
                    "\"name\": \"StreetComplete benchmark reload\"",
                )
            )
            snapshotFlow { state.mapState.style.loadState }
                .first { it != StyleLoadState.Ready }
            val afterReloadStarted = state.styleConfiguration.pinPublicationTracker.latestSequence
            snapshotFlow { state.mapState.style.loadState }
                .first { it == StyleLoadState.Ready }
            state.styleConfiguration.pinPublicationTracker.awaitPublished(
                pinSnapshots[2],
                afterReloadStarted,
            )
        }
        runPhase("downloaded-area-1681-tiles", 3.seconds) {
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude + 0.005, SCENARIO_CENTER.longitude - 0.005),
                zoom = 16.0,
                duration = 2.seconds,
            )
            viewModel.publishDownloadedTiles(scenarioDownloadedTiles())
        }
        runPhase("app-background-foreground", 3.seconds) {
            // The host script backgrounds the app for one second after seeing this phase begin.
            // Wait until it has foregrounded us, then require fresh data and rendered frames.
            delay(2.seconds)
            val afterFrame = recorder.mapFrameSequence.value
            val afterPublication = state.styleConfiguration.pinPublicationTracker.latestSequence
            viewModel.publishQuestPins(pinSnapshots[1])
            state.moveTo(
                LatLon(SCENARIO_CENTER.latitude + 0.003, SCENARIO_CENTER.longitude + 0.003),
                zoom = 16.0,
                duration = 300.milliseconds,
            )
            state.styleConfiguration.pinPublicationTracker.awaitPublished(
                pinSnapshots[1],
                afterPublication,
            )
            recorder.awaitMapFrames(afterFrame, 3)
            Log.i(MapPerformanceRecorder.TAG, "LIFECYCLE_RESUME_PROBE_READY")
        }
        runPhase("presentation-cycles", 1.seconds) {
            repeat(5) { cycle ->
                mapVisible = false
                delay(120.milliseconds)
                val afterFrame = recorder.mapFrameSequence.value
                mapVisible = true
                delay(80.milliseconds)
                state.moveTo(
                    LatLon(
                        SCENARIO_CENTER.latitude + cycle * 0.0002,
                        SCENARIO_CENTER.longitude - cycle * 0.0002,
                    ),
                    zoom = 16.0,
                    duration = 100.milliseconds,
                )
                recorder.awaitMapFrames(afterFrame, 3)
                Log.i(MapPerformanceRecorder.TAG, "PRESENTATION_RECOVERED cycle=${cycle + 1}")
                delay(300.milliseconds)
            }
        }
        recorder.finish()
        phase = "complete"
    }

    Box(Modifier.fillMaxSize()) {
        if (mapVisible) {
            MainMap(
                onClickOverlayElement = { _: ElementKey -> },
                onClickQuest = { _: QuestKey -> },
                onClickEdit = { _: EditKey -> },
                onClickMap = { _, _ -> },
                onLongPress = { _, _ -> },
                locationEvent = null,
                locationRotation = locationRotation,
                hiddenBaseLayerIds = if (state.showStyleableOverlay) {
                    SCENARIO_HIDDEN_BASE_LAYER_IDS
                } else {
                    emptySet()
                },
                modifier = Modifier.fillMaxSize(),
                state = state,
                onFrame = recorder::onFrame,
                viewModel = viewModel,
            )
        }
        Column(
            Modifier
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(8.dp)
        ) {
            Text("Map performance scenario", color = Color.White)
            Text(phase, color = MaterialTheme.colors.secondary)
        }
    }
}

private val PinPublicationTracker.latestSequence: Long
    get() = publication.value?.sequence ?: 0L

private suspend fun PinPublicationTracker.awaitPublished(snapshot: PinSnapshot, after: Long) {
    withTimeout(30.seconds) {
        publication.first { it != null && it.sequence > after && it.snapshot === snapshot }
    }
}

/** Exercises MainMap's production viewport-to-StateFlow-to-style handoff with deterministic data. */
private class ScenarioMainMapViewModel(
    private val panPinPages: List<List<Pin>>,
    icons: List<DrawableResource>,
) : MainMapViewModel() {
    private val overlayPipeline = MapPerformanceOverlayPipeline(
        icons = icons.take(SCENARIO_OVERLAY_ICON_COUNT),
        elementCount = SCENARIO_OVERLAY_ELEMENT_COUNT,
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val requestedQuestPins = MutableStateFlow(PinSnapshot.Empty)
    private val activePinMode = MutableStateFlow(MainMapPinMode.NONE)
    private val viewportLoadsEnabled = MutableStateFlow(false)
    private var viewportLoadJob: Job? = null
    private var lastViewportKey: Int? = null

    private val _downloadedTiles = MutableStateFlow<List<TilePos>>(emptyList())
    private val _questPins = MutableStateFlow(PinSnapshot.Empty)
    private val _editHistoryPins = MutableStateFlow(PinSnapshot.Empty)

    override val downloadedTiles: StateFlow<List<TilePos>> = _downloadedTiles
    override val questPins: StateFlow<PinSnapshot> = _questPins
    override val editHistoryPins: StateFlow<PinSnapshot> = _editHistoryPins
    override val styleableElements: StateFlow<List<StyledElement>> =
        overlayPipeline.styledElements

    override fun setPresented(presented: Boolean) {
        overlayPipeline.setPresented(presented)
        Log.i(MapPerformanceRecorder.TAG, "MAP_PRESENTED $presented")
    }

    override fun setActivePinMode(mode: MainMapPinMode) {
        if (activePinMode.value == mode) return
        activePinMode.value = mode
        Log.i(
            MapPerformanceRecorder.TAG,
            MapPerformanceDiagnostics.contextualize("ACTIVE_PIN_MODE $mode"),
        )
        _questPins.value = if (mode == MainMapPinMode.QUESTS) {
            requestedQuestPins.value
        } else {
            PinSnapshot.Empty
        }
        _editHistoryPins.value = PinSnapshot.Empty
    }

    override fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?) {
        overlayPipeline.onViewportChanged(zoom, displayedArea)
        if (
            !viewportLoadsEnabled.value || activePinMode.value != MainMapPinMode.QUESTS ||
            zoom < 14.0 || displayedArea == null
        ) return
        val viewportKey = ((displayedArea.min.longitude + displayedArea.max.longitude) * 25.0)
            .roundToInt()
        if (lastViewportKey == viewportKey) return
        lastViewportKey = viewportKey
        viewportLoadJob?.cancel()
        val page = viewportKey.mod(panPinPages.size)
        viewportLoadJob = scope.launch {
            delay(40.milliseconds)
            val snapshot = PinSnapshot.Empty.updated(panPinPages[page])
            requestedQuestPins.value = snapshot
            if (activePinMode.value == MainMapPinMode.QUESTS) _questPins.value = snapshot
            Log.i(
                MapPerformanceRecorder.TAG,
                MapPerformanceDiagnostics.contextualize("SCENARIO_PIN_VIEWPORT_LOAD page=$page"),
            )
        }
    }

    override fun getQuestKey(properties: Map<String, String>): QuestKey? = null
    override fun getEditKey(properties: Map<String, String>): EditKey? = null

    fun publishQuestPins(snapshot: PinSnapshot) {
        requestedQuestPins.value = snapshot
        Log.i(
            MapPerformanceRecorder.TAG,
            "QUEST_PIN_REQUEST pins=${snapshot.pins.size} active=${activePinMode.value}",
        )
        if (activePinMode.value == MainMapPinMode.QUESTS) _questPins.value = snapshot
    }

    fun publishDownloadedTiles(tiles: List<TilePos>) {
        _downloadedTiles.value = tiles
    }

    fun selectOverlay() = overlayPipeline.select()

    fun close() = overlayPipeline.close()

    fun enableViewportLoads() {
        lastViewportKey = null
        viewportLoadsEnabled.value = true
    }

    fun disableViewportLoads() {
        viewportLoadsEnabled.value = false
        viewportLoadJob?.cancel()
    }
}

private class MapPerformanceRecorder {
    val mapFrameSequence = MutableStateFlow(0L)

    private var phase: String? = null
    private var phaseStarted: TimeMark? = null
    private var frames = 0
    private var reportedSamples = 0
    private var totalFps = 0.0
    private var minimumFps = Double.POSITIVE_INFINITY
    private var framesBelow45Fps = 0
    private var framesBelow20Fps = 0
    private var firstFrameDelay: Duration? = null
    private var previousMapFrame: TimeMark? = null
    private var maximumMapFrameInterval = Duration.ZERO
    private var mapFramesOver50Millis = 0
    private var mapFramesOver100Millis = 0
    private var previousUiFrameNanos: Long? = null
    private var firstUiFrameDelay: Duration? = null
    private var uiFrames = 0
    private var maximumUiFrameInterval = Duration.ZERO
    private var uiFramesOver22Millis = 0
    private var uiFramesOver50Millis = 0
    private var uiFramesOver100Millis = 0
    private var gcEpochAtPhaseStart: Long? = null
    private var lastReportedGcEpoch: Long? = null

    fun begin(name: String) {
        finishPhase()
        phase = name
        MapPerformanceDiagnostics.beginPhase(name)
        phaseStarted = TimeSource.Monotonic.markNow()
        gcEpochAtPhaseStart = latestMapPerformanceGcPause()?.epoch
        Log.i(TAG, "BEGIN $name")
    }

    fun onFrame(framesPerSecond: Double) {
        if (phase == null) return
        mapFrameSequence.value += 1L
        val now = TimeSource.Monotonic.markNow()
        val interval = previousMapFrame?.elapsedNow() ?: phaseStarted?.elapsedNow()
        interval?.let(::recordMapInterval)
        previousMapFrame = now
        frames += 1
        if (firstFrameDelay == null) {
            firstFrameDelay = interval
            return
        }
        reportedSamples += 1
        totalFps += framesPerSecond
        minimumFps = minOf(minimumFps, framesPerSecond)
        if (framesPerSecond < 45.0) framesBelow45Fps += 1
        if (framesPerSecond < 20.0) framesBelow20Fps += 1
    }

    fun onUiFrame(frameTimeNanos: Long) {
        if (phase == null) return
        val previous = previousUiFrameNanos
        previousUiFrameNanos = frameTimeNanos
        uiFrames += 1
        val interval = if (previous == null) {
            phaseStarted?.elapsedNow()?.also { firstUiFrameDelay = it } ?: return
        } else {
            (frameTimeNanos - previous).coerceAtLeast(0).nanoseconds
        }
        maximumUiFrameInterval = maxOf(maximumUiFrameInterval, interval)
        if (interval > 22.milliseconds) uiFramesOver22Millis += 1
        if (interval > 50.milliseconds) uiFramesOver50Millis += 1
        if (interval > 100.milliseconds) uiFramesOver100Millis += 1
        if (interval > 50.milliseconds) logGap("ui", interval)
    }

    fun finish() {
        finishPhase()
        Log.i(TAG, "COMPLETE")
    }

    suspend fun awaitMapFrames(after: Long, count: Long) {
        withTimeout(3.seconds) {
            mapFrameSequence.first { it >= after + count }
        }
    }

    private fun finishPhase() {
        val completedPhase = phase ?: return
        val elapsed = phaseStarted?.elapsedNow() ?: Duration.ZERO
        val callbackRate = if (elapsed == Duration.ZERO) {
            0.0
        } else {
            frames / elapsed.inWholeNanoseconds.toDouble() * 1_000_000_000.0
        }
        val averageFps = if (reportedSamples == 0) 0.0 else totalFps / reportedSamples
        val observedMinimumFps = if (reportedSamples == 0) 0.0 else minimumFps
        Log.i(
            TAG,
            "END $completedPhase: elapsed=$elapsed frames=$frames callbackRate=$callbackRate " +
                "firstFrameDelay=$firstFrameDelay " +
                "reportedAverageFps=$averageFps reportedMinimumFps=$observedMinimumFps " +
                "framesBelow45Fps=$framesBelow45Fps framesBelow20Fps=$framesBelow20Fps " +
                "maximumMapFrameInterval=$maximumMapFrameInterval " +
                "mapFramesOver50Millis=$mapFramesOver50Millis " +
                "mapFramesOver100Millis=$mapFramesOver100Millis " +
                "uiFrames=$uiFrames firstUiFrameDelay=$firstUiFrameDelay " +
                "maximumUiFrameInterval=$maximumUiFrameInterval " +
                "uiFramesOver22Millis=$uiFramesOver22Millis " +
                "uiFramesOver50Millis=$uiFramesOver50Millis " +
                "uiFramesOver100Millis=$uiFramesOver100Millis",
        )
        MapPerformanceDiagnostics.endPhase(completedPhase)
        phase = null
        phaseStarted = null
        frames = 0
        reportedSamples = 0
        totalFps = 0.0
        minimumFps = Double.POSITIVE_INFINITY
        framesBelow45Fps = 0
        framesBelow20Fps = 0
        firstFrameDelay = null
        previousMapFrame = null
        maximumMapFrameInterval = Duration.ZERO
        mapFramesOver50Millis = 0
        mapFramesOver100Millis = 0
        previousUiFrameNanos = null
        firstUiFrameDelay = null
        uiFrames = 0
        maximumUiFrameInterval = Duration.ZERO
        uiFramesOver22Millis = 0
        uiFramesOver50Millis = 0
        uiFramesOver100Millis = 0
        gcEpochAtPhaseStart = null
        lastReportedGcEpoch = null
    }

    private fun recordMapInterval(interval: Duration) {
        maximumMapFrameInterval = maxOf(maximumMapFrameInterval, interval)
        if (interval > 50.milliseconds) mapFramesOver50Millis += 1
        if (interval > 100.milliseconds) mapFramesOver100Millis += 1
        if (interval > 50.milliseconds) logGap("map", interval)
    }

    private fun logGap(kind: String, interval: Duration) {
        val currentPhase = phase ?: return
        val gc = latestMapPerformanceGcPause()
        val gcSummary = if (
            gc != null &&
            gc.epoch != gcEpochAtPhaseStart &&
            gc.epoch != lastReportedGcEpoch
        ) {
            lastReportedGcEpoch = gc.epoch
            "gcEpoch=${gc.epoch} gcFirstPause=${gc.firstPauseNanos.nanoseconds} " +
                "gcSecondPause=${gc.secondPauseNanos?.nanoseconds}"
        } else {
            "gcEpoch=unchanged"
        }
        Log.i(TAG, "GAP phase=$currentPhase kind=$kind interval=$interval $gcSummary")
    }

    companion object {
        const val TAG = "MapPerformanceScenario"
    }
}

private fun scenarioPins(
    icons: List<DrawableResource>,
    longitudeOffset: Double,
): List<Pin> = List(SCENARIO_PIN_COUNT) { index ->
    val row = index / SCENARIO_COLUMNS
    val column = index % SCENARIO_COLUMNS
    Pin(
        position = LatLon(
            SCENARIO_CENTER.latitude + (row - 8) * 0.00045,
            SCENARIO_CENTER.longitude + longitudeOffset + (column - 11) * 0.00045,
        ),
        icon = icons[index % icons.size],
        properties = listOf("scenario-index" to index.toString()),
        order = index % icons.size,
    )
}

private fun scenarioMarkers(icons: List<DrawableResource>): List<Marker> =
    List(SCENARIO_MARKER_COUNT) { index ->
        val center = LatLon(
            SCENARIO_CENTER.latitude + (index / 8 - 3) * 0.0007,
            SCENARIO_CENTER.longitude + (index % 8 - 4) * 0.0007,
        )
        val line = List(SCENARIO_MARKER_POINTS) { point ->
            val fraction = point.toDouble() / (SCENARIO_MARKER_POINTS - 1)
            LatLon(
                center.latitude + (fraction - 0.5) * 0.001,
                center.longitude + kotlin.math.sin(fraction * kotlin.math.PI * 2) * 0.0002,
            )
        }
        Marker(
            geometry = ElementPolylinesGeometry(listOf(line), center),
            icon = icons[index % minOf(icons.size, SCENARIO_MARKER_ICON_COUNT)],
            title = "Marker $index",
        )
    }

private fun scenarioFocusedGeometry(): ElementPolylinesGeometry {
    val line = List(SCENARIO_FOCUS_POINTS) { point ->
        val fraction = point.toDouble() / (SCENARIO_FOCUS_POINTS - 1)
        LatLon(
            SCENARIO_CENTER.latitude + (fraction - 0.5) * 0.02,
            SCENARIO_CENTER.longitude + kotlin.math.sin(fraction * kotlin.math.PI * 8) * 0.004,
        )
    }
    return ElementPolylinesGeometry(listOf(line), SCENARIO_CENTER)
}

private fun scenarioLocationEvent(position: LatLon, timestampMillis: Long) = LocationEvent.Update(
    measurement = LocationMeasurement(
        position = Position(position.longitude, position.latitude),
        horizontalAccuracy = 5.0.meters,
        measuredAt = Instant.fromEpochMilliseconds(timestampMillis),
    ),
    measurementMark = TimeSource.Monotonic.markNow(),
)

private fun scenarioDownloadedTiles(): List<TilePos> {
    val center = SCENARIO_CENTER.enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
    return buildList {
        for (x in center.x - 20..center.x + 20) {
            for (y in center.y - 20..center.y + 20) add(TilePos(x, y))
        }
    }
}

private val SCENARIO_CENTER = LatLon(37.7749, -122.4194)
private val SCENARIO_HIDDEN_BASE_LAYER_IDS = setOf("labels-housenumbers")
private const val MAP_PERFORMANCE_SCENARIO_VERSION = 10
private const val SCENARIO_PIN_COUNT = 352
private const val SCENARIO_ICON_COUNT = 37
private const val SCENARIO_COLUMNS = 22
private const val SCENARIO_PAN_PAGE_COUNT = 8
private const val SCENARIO_SOAK_PAN_COUNT = 48
private const val SCENARIO_REALISTIC_LOCATION_UPDATES = 6
private const val SCENARIO_STRESS_LOCATION_UPDATES = 150
private const val SCENARIO_OVERLAY_ELEMENT_COUNT = 160
private const val SCENARIO_OVERLAY_ICON_COUNT = 8
private const val SCENARIO_MARKER_COUNT = 48
private const val SCENARIO_MARKER_ICON_COUNT = 8
private const val SCENARIO_MARKER_POINTS = 64
private const val SCENARIO_FOCUS_POINTS = 2_000
