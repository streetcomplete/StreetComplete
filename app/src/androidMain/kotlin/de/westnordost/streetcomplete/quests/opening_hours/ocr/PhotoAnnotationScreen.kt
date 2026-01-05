package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_check_circle_24
import de.westnordost.streetcomplete.resources.ic_undo_24
import de.westnordost.streetcomplete.ui.common.BackIcon
import org.jetbrains.compose.resources.painterResource
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class AnnotationMode { OPEN, CLOSE, CLOSED }

private typealias StrokePoint = RegionCalculator.Point

private fun loadBitmapWithRotation(path: String): Bitmap? {
    val bitmap = BitmapFactory.decodeFile(path) ?: return null
    val exif = ExifInterface(path)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    return if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

@Composable
fun PhotoAnnotationScreen(
    state: OcrFlowState,
    photoPath: String?,
    onPhotoPathChange: (String?) -> Unit,
    onStateChange: (OcrFlowState) -> Unit,
    onDebugDataReady: (List<OcrDebugData>) -> Unit,
    onContinueToDebug: () -> Unit,
    onContinueToVerification: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Current annotation mode (open = green, close = red, closed = gray/marking day as closed)
    var annotationMode by rememberSaveable { mutableStateOf(AnnotationMode.OPEN) }

    // Whether the current day group is marked as closed (no hours, just "off")
    var isMarkedClosed by rememberSaveable { mutableStateOf(false) }

    // Current strokes for open and close regions
    val openStrokes = remember { mutableStateListOf<StrokePoint>() }
    val closeStrokes = remember { mutableStateListOf<StrokePoint>() }

    // Accumulated debug data for all day groups
    val debugDataList = remember { mutableStateListOf<OcrDebugData>() }

    // Brush width in dp, converted to pixels for drawing
    val brushWidthDp = 18.dp  // 50% larger for easier highlighting
    val brushWidthPx = with(density) { brushWidthDp.toPx() }

    val currentDayGroup = state.currentDayGroup
    val ocrProcessor = remember { OcrProcessor() }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            onPhotoPathChange(photoFile!!.absolutePath)
            scope.launch {
                bitmap = withContext(Dispatchers.IO) {
                    loadBitmapWithRotation(photoFile!!.absolutePath)
                }
            }
        }
    }

    // Launch camera if no photo yet
    LaunchedEffect(photoPath) {
        if (photoPath == null) {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFileName = "ocr_photo_${nowAsEpochMilliseconds()}.jpg"
            val file = File(directory, imageFileName)
            file.createNewFile()
            photoFile = file

            val photoUri = FileProvider.getUriForFile(
                context,
                context.getString(R.string.fileprovider_authority),
                file
            )
            cameraLauncher.launch(photoUri)
        } else {
            scope.launch {
                bitmap = withContext(Dispatchers.IO) {
                    loadBitmapWithRotation(photoPath)
                }
            }
        }
    }

    // Function to calculate bounding box from strokes using RegionCalculator
    fun calculateBoundingBox(strokes: List<StrokePoint>, imageWidth: Int, imageHeight: Int): RectF? {
        return RegionCalculator.calculateBoundingBox(
            points = strokes,
            canvasWidth = canvasSize.width,
            canvasHeight = canvasSize.height,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            brushRadiusPx = brushWidthPx / 2f  // Expand by brush radius to include full stroke
        )
    }

    // Function to crop a bitmap to the specified region using RegionCalculator
    fun cropBitmap(sourceBitmap: Bitmap, region: RectF): Bitmap? {
        return RegionCalculator.cropBitmap(sourceBitmap, region)
    }

    // Function to process current annotation and move to next
    fun confirmCurrentAnnotation(skipRemaining: Boolean, showDebug: Boolean = false) {
        val loadedBitmap = bitmap ?: return
        val currentGroup = currentDayGroup ?: return

        scope.launch {
            // Variables to collect debug data
            var openCroppedBitmap: Bitmap? = null
            var closeCroppedBitmap: Bitmap? = null
            var openRawText = ""
            var closeRawText = ""
            var openParsedText = ""
            var closeParsedText = ""
            var debugIsOpenAm: Boolean? = null
            var debugIsCloseAm: Boolean? = null

            val currentAnnotation = if (isMarkedClosed) {
                // Day is marked as closed - no OCR needed
                DayAnnotation(
                    dayGroup = currentGroup,
                    isClosed = true
                )
            } else {
                // Run OCR on highlighted regions
                val openRegion = calculateBoundingBox(openStrokes.toList(), loadedBitmap.width, loadedBitmap.height)
                val closeRegion = calculateBoundingBox(closeStrokes.toList(), loadedBitmap.width, loadedBitmap.height)

                // Crop bitmaps for debug display
                openCroppedBitmap = openRegion?.let { cropBitmap(loadedBitmap, it) }
                closeCroppedBitmap = closeRegion?.let { cropBitmap(loadedBitmap, it) }

                // Extract raw text (including AM/PM) for each region
                val openTextRaw = openRegion?.let {
                    ocrProcessor.extractTextFromRegion(loadedBitmap, it)
                }
                val closeTextRaw = closeRegion?.let {
                    ocrProcessor.extractTextFromRegion(loadedBitmap, it)
                }

                // Store raw text for debug
                openRawText = openTextRaw ?: ""
                closeRawText = closeTextRaw ?: ""

                // Detect AM/PM from raw text
                val isOpenAm = openTextRaw?.let { ocrProcessor.detectAmPm(it) }
                val isCloseAm = closeTextRaw?.let { ocrProcessor.detectAmPm(it) }

                // Store for debug
                debugIsOpenAm = isOpenAm
                debugIsCloseAm = isCloseAm

                // Parse time numbers from raw text
                val openTimeRaw = openTextRaw?.let { ocrProcessor.parseTimeFromText(it) }
                val closeTimeRaw = closeTextRaw?.let { ocrProcessor.parseTimeFromText(it) }

                // Store parsed text for debug
                openParsedText = openTimeRaw ?: ""
                closeParsedText = closeTimeRaw ?: ""

                DayAnnotation(
                    dayGroup = currentGroup,
                    openRegion = openRegion,
                    closeRegion = closeRegion,
                    openTimeRaw = openTimeRaw,
                    closeTimeRaw = closeTimeRaw,
                    isClosed = false,
                    isOpenAm = isOpenAm,
                    isCloseAm = isCloseAm
                )
            }

            // Add debug data for this group
            debugDataList.add(
                OcrDebugData(
                    dayGroup = currentGroup,
                    openCroppedBitmap = openCroppedBitmap,
                    closeCroppedBitmap = closeCroppedBitmap,
                    openRawText = openRawText,
                    closeRawText = closeRawText,
                    openParsedText = openParsedText,
                    closeParsedText = closeParsedText,
                    isOpenAm = debugIsOpenAm,
                    isCloseAm = debugIsCloseAm,
                    isClosed = isMarkedClosed
                )
            )

            // Update annotation for current group
            val updatedAnnotations = state.annotations.toMutableList()

            if (state.currentGroupIndex < updatedAnnotations.size) {
                updatedAnnotations[state.currentGroupIndex] = currentAnnotation
            } else {
                updatedAnnotations.add(currentAnnotation)
            }

            if (skipRemaining || state.isLastGroup) {
                // Go to verification (or debug screen if long-pressed)
                onStateChange(state.copy(annotations = updatedAnnotations))
                onDebugDataReady(debugDataList.toList())
                if (showDebug) {
                    onContinueToDebug()
                } else {
                    onContinueToVerification()
                }
            } else {
                // Move to next group
                onStateChange(
                    state.copy(
                        annotations = updatedAnnotations,
                        currentGroupIndex = state.currentGroupIndex + 1
                    )
                )
                // Clear strokes for next group and reset closed state
                openStrokes.clear()
                closeStrokes.clear()
                isMarkedClosed = false
                annotationMode = AnnotationMode.OPEN
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.quest_openingHours_ocr_annotate_title)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onBack) { BackIcon() } },
            actions = {
                // Retake photo button
                IconButton(onClick = {
                    onPhotoPathChange(null)
                    bitmap = null
                    openStrokes.clear()
                    closeStrokes.clear()
                }) {
                    Icon(painterResource(Res.drawable.ic_undo_24), contentDescription = stringResource(R.string.quest_openingHours_ocr_retake))
                }
            }
        )

        if (bitmap != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            ) {
                val loadedBitmap = bitmap!!

                // Full-screen photo with annotation canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                ) {
                    // Photo background
                    androidx.compose.foundation.Image(
                        bitmap = loadedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Annotation canvas overlay
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(annotationMode) {
                                detectDragGestures(
                                    onDrag = { change, _ ->
                                        val point = StrokePoint(
                                            change.position.x,
                                            change.position.y
                                        )
                                        when (annotationMode) {
                                            AnnotationMode.OPEN -> openStrokes.add(point)
                                            AnnotationMode.CLOSE -> closeStrokes.add(point)
                                            AnnotationMode.CLOSED -> { /* No strokes in closed mode */ }
                                        }
                                    }
                                )
                            }
                    ) {
                        // Draw open strokes (green)
                        if (openStrokes.isNotEmpty()) {
                            val openPath = Path().apply {
                                openStrokes.forEachIndexed { index, point ->
                                    if (index == 0) {
                                        moveTo(point.x, point.y)
                                    } else {
                                        lineTo(point.x, point.y)
                                    }
                                }
                            }
                            drawPath(
                                path = openPath,
                                color = TrafficSignColor.Green.copy(alpha = 0.6f),
                                style = Stroke(
                                    width = brushWidthPx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // Draw close strokes (red)
                        if (closeStrokes.isNotEmpty()) {
                            val closePath = Path().apply {
                                closeStrokes.forEachIndexed { index, point ->
                                    if (index == 0) {
                                        moveTo(point.x, point.y)
                                    } else {
                                        lineTo(point.x, point.y)
                                    }
                                }
                            }
                            drawPath(
                                path = closePath,
                                color = TrafficSignColor.Red.copy(alpha = 0.6f),
                                style = Stroke(
                                    width = brushWidthPx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Debug button - small, positioned top-left
                    Button(
                        onClick = {
                            val debugEnabled = isMarkedClosed || openStrokes.isNotEmpty() || closeStrokes.isNotEmpty()
                            if (debugEnabled) {
                                confirmCurrentAnnotation(skipRemaining = false, showDebug = true)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        enabled = isMarkedClosed || openStrokes.isNotEmpty() || closeStrokes.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Debug", style = MaterialTheme.typography.caption)
                    }
                }

                // Floating controls at bottom
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Day group indicator row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentDayGroup?.toDisplayString() ?: "",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.progress,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Open/Close/Closed toggle buttons in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Open time button (green)
                            Button(
                                onClick = {
                                    annotationMode = AnnotationMode.OPEN
                                    isMarkedClosed = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (annotationMode == AnnotationMode.OPEN && !isMarkedClosed) {
                                        TrafficSignColor.Green
                                    } else {
                                        MaterialTheme.colors.surface
                                    },
                                    contentColor = if (annotationMode == AnnotationMode.OPEN && !isMarkedClosed) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    }
                                )
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_open_time))
                            }

                            // Close time button (red)
                            Button(
                                onClick = {
                                    annotationMode = AnnotationMode.CLOSE
                                    isMarkedClosed = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (annotationMode == AnnotationMode.CLOSE && !isMarkedClosed) {
                                        TrafficSignColor.Red
                                    } else {
                                        MaterialTheme.colors.surface
                                    },
                                    contentColor = if (annotationMode == AnnotationMode.CLOSE && !isMarkedClosed) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    }
                                )
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_close_time))
                            }

                            // Closed button (gray) - marks day as closed
                            Button(
                                onClick = {
                                    annotationMode = AnnotationMode.CLOSED
                                    isMarkedClosed = true
                                    // Clear any strokes since day is closed
                                    openStrokes.clear()
                                    closeStrokes.clear()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isMarkedClosed) {
                                        Color.DarkGray
                                    } else {
                                        MaterialTheme.colors.surface
                                    },
                                    contentColor = if (isMarkedClosed) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    }
                                )
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_closed))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Clear and Next/Done buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Clear button (only show when not in closed mode)
                            if (!isMarkedClosed) {
                                Button(
                                    onClick = {
                                        when (annotationMode) {
                                            AnnotationMode.OPEN -> openStrokes.clear()
                                            AnnotationMode.CLOSE -> closeStrokes.clear()
                                            AnnotationMode.CLOSED -> { /* No strokes to clear */ }
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(stringResource(R.string.quest_openingHours_ocr_clear))
                                }
                            }

                            // Next/Done button
                            val isButtonEnabled = isMarkedClosed || openStrokes.isNotEmpty() || closeStrokes.isNotEmpty()

                            Button(
                                onClick = {
                                    if (isButtonEnabled) {
                                        confirmCurrentAnnotation(skipRemaining = false, showDebug = false)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = isButtonEnabled
                            ) {
                                Icon(painterResource(Res.drawable.ic_check_circle_24), contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (state.isLastGroup) {
                                        stringResource(R.string.quest_openingHours_ocr_done)
                                    } else {
                                        stringResource(R.string.quest_openingHours_ocr_next)
                                    }
                                )
                            }

                            // Skip button (only show if not last group)
                            if (!state.isLastGroup) {
                                Button(
                                    onClick = { confirmCurrentAnnotation(skipRemaining = true) },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(stringResource(R.string.quest_openingHours_ocr_skip))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Loading or waiting for camera
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.quest_openingHours_ocr_waiting_for_photo))
            }
        }
    }
}
