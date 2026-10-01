package de.westnordost.streetcomplete.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.lerp as lerpColor

/** Physical edge the scroller sits on. START is left in LTR and right in RTL. */
enum class FastScrollEdge { START, END }

/**
 * Draggable scrollbar for a vertical, non-reversed LazyColumn.
 *
 * Apply it to the LazyColumn itself. Only touches that start on or near the thumb are
 * consumed; every other touch reaches the list untouched. The thumb can only be grabbed
 * while it is visible. Position data comes from [LazyListState.scrollIndicatorState].
 *
 * @param trackPadding space kept free at the top and bottom of the track
 * @param touchTargetWidth width of the grab zone, measured from the screen edge
 * @param capsuleWidth width of the background capsule drawn behind the thumb
 * @param capsuleExtraHeight how much taller the capsule is than the thumb
 * @param minItemsToShow lists with this many items or fewer get no scrollbar
 * @param maxHandleHeightFraction cap on thumb height as a fraction of the track
 */
@Composable
fun Modifier.scrollbar(
    listState: LazyListState,
    edge: FastScrollEdge = FastScrollEdge.END,
    handleWidth: Dp = 5.dp,
    pressedHandleWidth: Dp = 8.dp,
    minHandleHeight: Dp = 48.dp,
    maxHandleHeightFraction: Float = 0.9f,
    touchTargetWidth: Dp = 48.dp,
    trackPadding: Dp = 8.dp,
    edgePadding: Dp = 4.dp,
    capsuleWidth: Dp = 38.dp,
    capsuleExtraHeight: Dp = 20.dp,
    minItemsToShow: Int = 10,
    hideDelayMillis: Long = 1_500L,
    fadeInMillis: Int = 150,
    fadeOutMillis: Int = 300,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
    pressedColor: Color = MaterialTheme.colors.primary,
    capsuleColor: Color = MaterialTheme.colors.surface.copy(alpha = 0.8f),
): Modifier {
    require(maxHandleHeightFraction in 0f..1f) {
        "maxHandleHeightFraction must be between 0f and 1f"
    }

    val haptics = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()

    // START/LTR and END/RTL are left; the other two are right.
    val onLeft = (edge == FastScrollEdge.START) == (layoutDirection == LayoutDirection.Ltr)

    val controller = remember(scope) { ScrollbarController(scope) }

    SideEffect {
        controller.hideDelayMillis = hideDelayMillis
        controller.fadeInMillis = fadeInMillis
        controller.fadeOutMillis = fadeOutMillis
    }
    DisposableEffect(controller) { onDispose { controller.close() } }

    // Observed via snapshotFlow (not read in composition), so drag start/end don't recompose.
    LaunchedEffect(controller) {
        snapshotFlow { controller.dragging }.collectLatest { isDragging ->
            controller.press.animateTo(if (isDragging) 1f else 0f, tween(120))
        }
    }

    val pointerModifier = Modifier.pointerInput(
        listState, onLeft, trackPadding, minHandleHeight, touchTargetWidth,
        minItemsToShow, maxHandleHeightFraction,
    ) {
        val padding = trackPadding.toPx()
        val minThumb = minHandleHeight.toPx()
        val touchWidth = touchTargetWidth.toPx() // horizontal grab zone, from the screen edge
        val slop = 8.dp.toPx()                   // extra vertical grab tolerance around the thumb

        awaitEachGesture {
            // Initial pass: we see the touch before the list does.
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

            // Not visible means not grabbable.
            if (controller.fade.value < 0.05f) return@awaitEachGesture

            val metrics = controller.frozen
            if (!metrics.update(listState) || metrics.itemCount <= minItemsToShow) return@awaitEachGesture

            val geometry = controller.grabGeometry
            geometry.set(
                size.height.toFloat(), padding, minThumb, maxHandleHeightFraction,
                metrics.thumbFraction, metrics.progress,
            )
            val track = geometry.track
            if (track <= 0f) return@awaitEachGesture

            val touchX = down.position.x
            val isInHorizontalGrabZone =
                if (onLeft) touchX <= touchWidth else touchX >= size.width - touchWidth
            val isInVerticalGrabZone =
                down.position.y in (geometry.top - slop)..(geometry.top + geometry.height + slop)

            // Not on the thumb: consume nothing, the list handles it.
            if (!isInHorizontalGrabZone || !isInVerticalGrabZone) return@awaitEachGesture

            // Keep the finger's position inside the thumb so it never jumps.
            val grabOffset = (down.position.y - geometry.top).coerceIn(0f, geometry.height)
            fun progressAt(fingerY: Float) =
                ((fingerY - grabOffset - padding) / track).coerceIn(0f, 1f)

            try {
                controller.beginDrag(metrics.progress)
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                down.consume()
                scope.launch { listState.stopScroll() } // cancel any running fling

                var lastKey = Long.MIN_VALUE // no request yet
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    var change: PointerInputChange? = null
                    for (changeIndex in event.changes.indices) {
                        if (event.changes[changeIndex].id == down.id) {
                            change = event.changes[changeIndex]
                            break
                        }
                    }
                    if (change == null) break
                    if (!change.pressed) {
                        change.consume()
                        break
                    }
                    controller.dragProgress = progressAt(change.position.y)
                    lastKey = listState.scrollToProgress(metrics, controller.dragProgress, lastKey)
                    change.consume()
                }
            } finally {
                // Runs on lift, cancel, and pointer-input restart, so no stuck state.
                controller.endDrag()
            }
        }
    }

    return this
        .then(pointerModifier)
        .drawWithContent {
            drawContent()

            val alpha: Float
            val fraction: Float
            val progress: Float

            if (controller.dragging) {
                // Frozen snapshot only: don't read live scroll state while dragging.
                alpha = 1f
                fraction = controller.frozen.thumbFraction
                progress = controller.dragProgress
            } else {
                val indicator = listState.scrollIndicatorState ?: return@drawWithContent
                val content = indicator.contentSize.toFloat()
                val viewport = indicator.viewportSize.toFloat()
                if (viewport <= 0f || content <= viewport) return@drawWithContent
                if (listState.layoutInfo.totalItemsCount <= minItemsToShow) return@drawWithContent

                // Before the alpha check, otherwise a hidden bar could never wake up.
                val offset = indicator.scrollOffset
                controller.onLiveOffset(offset)

                alpha = controller.fade.value
                if (alpha <= 0f) return@drawWithContent

                fraction = viewport / content
                val liveProgress = listState.pinnedProgress(
                    offset.toFloat(),
                    (content - viewport).coerceAtLeast(1f),
                )
                // After release, glide from where the finger left to the real position.
                progress = lerp(liveProgress, controller.settleFrom, controller.settle.value)
            }

            val geometry = controller.geometry
            geometry.set(
                size.height, trackPadding.toPx(), minHandleHeight.toPx(),
                maxHandleHeightFraction, fraction, progress,
            )
            if (geometry.height <= 0f) return@drawWithContent

            val pressed = controller.press.value
            val capsuleWidthPx = capsuleWidth.toPx()
            val capsuleX =
                if (onLeft) edgePadding.toPx() else size.width - capsuleWidthPx - edgePadding.toPx()

            drawHandle(
                geometry = geometry,
                alpha = alpha,
                thumbColor = lerpColor(color, pressedColor, pressed),
                thumbWidth = lerp(handleWidth.toPx(), pressedHandleWidth.toPx(), pressed),
                capsuleX = capsuleX,
                capsuleWidth = capsuleWidthPx,
                capsuleExtraHeight = capsuleExtraHeight.toPx(),
                capsuleColor = capsuleColor,
            )
        }
}

/**
 * Owns all scrollbar state. pointerInput and draw read parameters through this object,
 * so they never see stale values. The timing fields are refreshed on every recomposition.
 */
private class ScrollbarController(private val scope: CoroutineScope) {
    var hideDelayMillis = 1_500L
    var fadeInMillis = 150
    var fadeOutMillis = 300

    var dragging by mutableStateOf(false)
    var dragProgress by mutableFloatStateOf(0f)
    var settleFrom by mutableFloatStateOf(0f)

    val frozen = ScrollMetrics()
    val geometry = ThumbGeometry()
    val grabGeometry = ThumbGeometry()

    val fade = Animatable(0f)
    val press = Animatable(0f)
    val settle = Animatable(0f) // 1 = at release position, 0 = at live position

    private val events = Channel<Unit>(Channel.CONFLATED)
    private var fadeJob: Job? = null
    private var lastScrollOffset = Int.MIN_VALUE

    /** Called from draw with the live offset. Signals activity only on real movement. */
    fun onLiveOffset(offset: Int) {
        if (lastScrollOffset == Int.MIN_VALUE) {
            lastScrollOffset = offset // baseline: don't show just because we attached
        } else if (offset != lastScrollOffset) {
            lastScrollOffset = offset
            signalActivity()
        }
    }

    /**
     * Shows the bar and restarts the hide timer. A single job lives for the whole active
     * period; later calls just feed it an event. Events are conflated, so at most one is pending.
     */
    fun signalActivity() {
        events.trySend(Unit)
        // A running, visible job consumes the event itself and extends the timer.
        val job = fadeJob
        if (job != null && job.isActive && fade.targetValue != 0f) return

        job?.cancel() // also restarts immediately if we were mid fade-out
        fadeJob = scope.launch {
            fade.animateTo(1f, tween(fadeInMillis))
            while (true) {
                events.tryReceive() // drop stale events
                if (dragging) {
                    // Held: stay visible until the drag ends and sends an event.
                    // A closed channel (disposed) returns null, which stops the job.
                    events.receiveCatching().getOrNull() ?: return@launch
                    continue
                }
                val nextEvent = withTimeoutOrNull(hideDelayMillis) {
                    events.receiveCatching().getOrNull()
                }
                if (nextEvent == null) break
            }
            // Defensive: a drag may have started just as the timeout fired.
            if (!dragging) fade.animateTo(0f, tween(fadeOutMillis))
        }
    }

    fun beginDrag(progress: Float) {
        dragProgress = progress
        dragging = true
        signalActivity()
    }

    fun endDrag() {
        settleFrom = dragProgress
        // UNDISPATCHED so settle is 1f before the next draw (no one-frame flash).
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            settle.snapTo(1f)
            settle.animateTo(0f, tween(150))
        }
        dragging = false // before signaling, so the fade job sees the drag has ended
        signalActivity()
    }

    fun close() {
        fadeJob?.cancel()
        fadeJob = null
        events.close()
    }
}

private class ThumbGeometry {
    var top = 0f      // y of the thumb's top edge
    var height = 0f   // thumb height
    var track = 0f    // distance the thumb can travel (usable height - thumb height)

    fun set(
        containerHeight: Float,
        padding: Float,
        minHeight: Float,
        maxHeightFraction: Float,
        fraction: Float,
        progress: Float,
    ) {
        val usable = (containerHeight - 2f * padding).coerceAtLeast(0f)
        if (usable <= 0f) {
            top = padding; height = 0f; track = 0f
            return
        }
        // Don't use coerceIn here: it throws when min > max, which happens if usable < minHeight.
        val maxHeight = (usable * maxHeightFraction).coerceAtLeast(minHeight).coerceAtMost(usable)
        height = (usable * fraction).coerceAtLeast(minHeight).coerceAtMost(maxHeight)
        track = (usable - height).coerceAtLeast(0f)
        top = padding + track * progress
    }
}

/**
 * Scroll metrics snapshot. Taken at grab time and used for the whole drag, so the
 * thumb-to-list mapping can't shift while LazyColumn remeasures items.
 * Mutable to avoid allocating per gesture.
 */
private class ScrollMetrics {
    var viewport = 1f
    var content = 1f
    var averageItemSize = 1f // estimated: content / itemCount
    var progress = 0f        // 0 = top, 1 = bottom
    var itemCount = 0

    val maxScroll: Float get() = (content - viewport).coerceAtLeast(1f)
    val thumbFraction: Float
        get() = if (content <= 0f) 1f else (viewport / content).coerceIn(0f, 1f)

    fun update(state: LazyListState): Boolean {
        val indicator = state.scrollIndicatorState ?: return false
        val totalItems = state.layoutInfo.totalItemsCount
        val contentSize = indicator.contentSize.toFloat()
        val viewportSize = indicator.viewportSize.toFloat()
        if (totalItems <= 0 || contentSize <= 0f || viewportSize <= 0f) return false

        itemCount = totalItems
        content = contentSize
        viewport = viewportSize
        averageItemSize = (contentSize / totalItems).coerceAtLeast(1f)
        progress = state.pinnedProgress(indicator.scrollOffset.toFloat(), maxScroll)
        return true
    }
}

/** Pins the ends exactly, because the list's own estimate drifts slightly. */
private fun LazyListState.pinnedProgress(offset: Float, maxScroll: Float): Float = when {
    !canScrollBackward -> 0f
    !canScrollForward -> 1f
    else -> (offset / maxScroll).coerceIn(0f, 1f)
}

/**
 * Scrolls the list to [progress] (0..1). Non-suspending because the gesture scope is
 * restricted-suspension and can't call scrollToItem. Skips the request when the target
 * equals [lastKey]. Returns the new key (packed index + offset) to pass back next call.
 */
private fun LazyListState.scrollToProgress(
    metrics: ScrollMetrics,
    progress: Float,
    lastKey: Long,
): Long {
    val index: Int
    val offset: Int
    when {
        progress <= 0f -> { index = 0; offset = 0 }
        progress >= 1f -> { index = metrics.itemCount - 1; offset = 0 } // list clamps to the true end
        else -> {
            val target = progress * metrics.maxScroll
            index = (target / metrics.averageItemSize).toInt().coerceIn(0, metrics.itemCount - 1)
            offset = (target - index * metrics.averageItemSize).roundToInt().coerceAtLeast(0)
        }
    }
    // Pack index and offset into one Long so the unchanged-target check doesn't allocate.
    val key = (index.toLong() shl 32) or (offset.toLong() and 0xFFFFFFFFL)
    if (key != lastKey) requestScrollToItem(index, offset)
    return key
}

/** Draws the capsule background, then the thumb centered horizontally inside it. */
private fun DrawScope.drawHandle(
    geometry: ThumbGeometry,
    alpha: Float,
    thumbColor: Color,
    thumbWidth: Float,
    capsuleX: Float,
    capsuleWidth: Float,
    capsuleExtraHeight: Float,
    capsuleColor: Color,
) {
    val capsuleHeight = (geometry.height + capsuleExtraHeight).coerceAtMost(size.height)
    // Center the capsule on the thumb, but keep it inside the view.
    val capsuleY = (geometry.top + geometry.height / 2f - capsuleHeight / 2f)
        .coerceIn(0f, (size.height - capsuleHeight).coerceAtLeast(0f))

    drawRoundRect(
        color = capsuleColor,
        topLeft = Offset(capsuleX, capsuleY),
        size = Size(capsuleWidth, capsuleHeight),
        cornerRadius = CornerRadius(capsuleWidth / 2f),
        alpha = alpha,
    )
    drawRoundRect(
        color = thumbColor,
        topLeft = Offset(capsuleX + (capsuleWidth - thumbWidth) / 2f, geometry.top),
        size = Size(thumbWidth, geometry.height),
        cornerRadius = CornerRadius(thumbWidth / 2f),
        alpha = alpha,
    )
}
