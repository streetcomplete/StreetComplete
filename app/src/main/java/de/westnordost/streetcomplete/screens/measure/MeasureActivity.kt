package de.westnordost.streetcomplete.screens.measure

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants.VIRTUAL_KEY
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState.TRACKING
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ActivityMeasureBinding
import de.westnordost.streetcomplete.screens.measure.MeasureActivity.Companion.createIntent
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.math.normalizeRadians
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

/** Activity to measure distances. Can be started as activity for result, see [createIntent] */
class MeasureActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val createArCoreSession = ArCoreSessionCreator(this, ::askUserToAcknowledgeCameraPermissionRationale)

    private lateinit var binding: ActivityMeasureBinding
    private var arSceneView: ArSceneView? = null

    private var cursorRenderable: Renderable? = null
    private var pointRenderable: Renderable? = null
    private var lineRenderable: Renderable? = null

    private var lineNode: Node? = null
    private var firstNode: AnchorNode? = null
    private var secondNode: Node? = null
    private var cursorNode: AnchorNode? = null

    private var measureVertical: Boolean = false
    private var displayUnit: MeasureDisplayUnit = MeasureDisplayUnitMeter(2)
    private var requestResult: Boolean = false

    private enum class MeasureState { READY, MEASURING, DONE }
    private var measureState: MeasureState = MeasureState.READY

    private var distance: Float = 0f

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // no turning off screen automatically while measuring, also no colored navbar
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        readIntent()
        distance = 0f

        try {
            binding = ActivityMeasureBinding.inflate(layoutInflater)
        } catch (e: Exception) {
            /* layout inflation may fail for the ArSceneView for some old devices that don't support
               AR anyway. So we can just exit */
            finish()
            return
        }

        setContentView(binding.root)
        binding.startOverButton.setOnClickListener { clearMeasuring() }
        binding.acceptButton.setOnClickListener { returnMeasuringResult() }

        lifecycleScope.launch {
            initializeSession()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                initRenderables()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (arSceneView != null) {
            try {
                arSceneView?.resume()
                binding.handMotionView.isGone = false
                binding.trackingMessageTextView.isGone = true
            } catch (e: CameraNotAvailableException) {
                // without camera, we can't do anything, might as well quit
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        arSceneView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView?.pause()
        arSceneView?.destroy()
        // closing can take several seconds, should be done one background thread that outlives this activity
        GlobalScope.launch(Dispatchers.Default) {
            arSceneView?.session?.close()
        }
    }

    private fun readIntent() {
        measureVertical = intent.getBooleanExtra(PARAM_MEASURE_VERTICAL, measureVertical)
        displayUnit = intent.getStringExtra(PARAM_DISPLAY_UNIT)?.let { Json.decodeFromString(it) }
            ?: MeasureDisplayUnitMeter(2)
        requestResult = intent.getBooleanExtra(PARAM_REQUEST_RESULT, false)
    }

    /* --------------------------------- Scene.OnUpdateListener --------------------------------- */

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView?.arFrame ?: return

        if (frame.hasFoundPlane()) {
            binding.handMotionView.isGone = true
        }

        setTrackingMessage(frame.camera.trackingFailureReason.messageResId)

        if (frame.camera.trackingState == TRACKING) {
            if (measureVertical) {
                if (measureState == MeasureState.READY) {
                    hitPlaneAndUpdateCursor(frame)
                } else if (measureState == MeasureState.MEASURING) {
                    updateVerticalMeasuring(frame.camera.displayOrientedPose)
                }
            } else {
                hitPlaneAndUpdateCursor(frame)
            }
        }
    }

    private fun hitPlaneAndUpdateCursor(frame: Frame) {
        val centerX = binding.arSceneViewContainer.width / 2f
        val centerY = binding.arSceneViewContainer.height / 2f
        val hitResults = frame.hitTest(centerX, centerY).filter {
            (it.trackable as? Plane)?.isPoseInPolygon(it.hitPose) == true
        }
        val firstNode = firstNode
        val hitResult = if (firstNode == null) {
            hitResults.firstOrNull()
        } else {
            /* after first node is placed on the plane, only accept hits with (other) planes
               that are more or less on the same height */
            hitResults.find { abs(it.hitPose.ty() - firstNode.worldPosition.y) < 0.1 }
        }

        if (hitResult != null) {
            updateCursor(hitResult)
            setTrackingMessage(
                if (measureState == MeasureState.READY) R.string.ar_core_tracking_hint_tap_to_measure else null
            )
        } else {
            /* when no plane can be found at the cursor position and the camera angle is
               shallow enough, display a hint that user should cross street
             */
            val cursorDistanceFromCamera = cursorNode?.worldPosition?.let {
                Vector3.subtract(frame.camera.pose.position, it).length()
            } ?: 0f

            setTrackingMessage(
                if (cursorDistanceFromCamera > 3f) R.string.ar_core_tracking_error_no_plane_hit else null
            )
        }
    }

    private fun setTrackingMessage(messageResId: Int?) {
        binding.trackingMessageTextView.isGone = messageResId == null
        messageResId?.let { binding.trackingMessageTextView.setText(messageResId) }
    }

    /* ------------------------------------------ Session --------------------------------------- */

    private suspend fun initializeSession() {
        val result = createArCoreSession()
        if (result is ArCoreSessionCreator.Success) {
            val session = result.session
            configureSession(session)
            addArSceneView(session)
        } else if (result is ArCoreSessionCreator.Failure) {
            val reason = result.reason
            if (reason == ArNotAvailableReason.AR_CORE_SDK_TOO_OLD) {
                toast(R.string.ar_core_error_sdk_too_old)
            } else if (reason == ArNotAvailableReason.NO_CAMERA_PERMISSION) {
                toast(R.string.no_camera_permission_toast)
            }
            // otherwise nothing we can do here...
            finish()
        }
    }

    private fun configureSession(session: Session) {
        val config = Config(session)

        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE // necessary for Sceneform
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        // disabling unused features should make processing faster
        config.depthMode = Config.DepthMode.DISABLED
        config.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED

        session.configure(config)
    }

    private fun addArSceneView(session: Session) {
        val arSceneView = ArSceneView(this)
        arSceneView.planeRenderer.isEnabled = false
        binding.arSceneViewContainer.addView(arSceneView, MATCH_PARENT, MATCH_PARENT)
        arSceneView.setupSession(session)
        arSceneView.scene.addOnUpdateListener(this)
        arSceneView.setOnClickListener { onTapPlane() }
        this.arSceneView = arSceneView
    }

    /* ---------------------------------------- Measuring --------------------------------------- */

    private fun onTapPlane() {
        when (measureState) {
            MeasureState.READY -> {
                startMeasuring()
            }
            MeasureState.MEASURING -> {
                measuringDone()
            }
            MeasureState.DONE -> {
                /* different behavior: When caller requests result, tapping again doesn't clear the
                 * result, instead the user needs to tap on the "start over" button, like when
                 * taking a picture with the camera */
                if (!requestResult) clearMeasuring() else continueMeasuring()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun initRenderables() {
        // takes about half a second on a high-end device(!)
        val attributes = obtainStyledAttributes(intArrayOf(android.R.attr.colorAccent))
        val argb = attributes.getColor(0, 0)
        attributes.recycle()
        val materialBlue = MaterialFactory.makeOpaqueWithColor(this, Color(argb)).await()
        cursorRenderable = ViewRenderable.builder().setView(this, R.layout.view_ar_cursor).build().await()
        pointRenderable = ShapeFactory.makeCylinder(0.03f, 0.005f, Vector3.zero(), materialBlue)
        lineRenderable = ShapeFactory.makeCube(Vector3(0.02f, 0.005f, 1f), Vector3.zero(), materialBlue)
        listOfNotNull(cursorRenderable, pointRenderable, lineRenderable).forEach {
            it.isShadowCaster = false
            it.isShadowReceiver = false
        }
        // in case they have been initialized already, (re)set renderables...
        cursorNode?.renderable = cursorRenderable
        firstNode?.renderable = pointRenderable
        secondNode?.renderable = pointRenderable
        lineNode?.renderable = lineRenderable
    }

    private fun startMeasuring() {
        val anchor = cursorNode?.anchor ?: return
        measureState = MeasureState.MEASURING
        binding.arSceneViewContainer.performHapticFeedback(VIRTUAL_KEY)
        firstNode = AnchorNode().apply {
            renderable = pointRenderable
            setParent(arSceneView!!.scene)
            setAnchor(anchor)
        }

        if (measureVertical) {
            secondNode = Node()
            cursorNode?.isEnabled = false
        } else {
            secondNode = AnchorNode().apply { setAnchor(anchor) }
        }
        secondNode?.apply {
            renderable = pointRenderable
            setParent(arSceneView!!.scene)
        }
    }

    private fun measuringDone() {
        binding.arSceneViewContainer.performHapticFeedback(VIRTUAL_KEY)
        if (requestResult) binding.acceptResultContainer.isGone = false
        measureState = MeasureState.DONE
    }

    private fun continueMeasuring() {
        binding.arSceneViewContainer.performHapticFeedback(VIRTUAL_KEY)
        if (requestResult) binding.acceptResultContainer.isGone = true
        measureState = MeasureState.MEASURING
    }

    private fun clearMeasuring() {
        measureState = MeasureState.READY
        binding.arSceneViewContainer.performHapticFeedback(VIRTUAL_KEY)
        binding.measurementSpeechBubble.isInvisible = true
        binding.acceptResultContainer.isGone = true
        distance = 0f
        cursorNode?.isEnabled = true
        firstNode?.anchor?.detach()
        firstNode?.setParent(null)
        firstNode = null
        (secondNode as? AnchorNode)?.anchor?.detach()
        secondNode?.setParent(null)
        secondNode = null
        lineNode?.setParent(null)
        lineNode = null
    }

    private fun returnMeasuringResult() {
        val resultIntent = Intent(RESULT_ACTION)
        when (val displayUnit = displayUnit) {
            is MeasureDisplayUnitFeetInch -> {
                val (feet, inches) = displayUnit.getRounded(distance)
                resultIntent.putExtra(RESULT_MEASURE_FEET, feet)
                resultIntent.putExtra(RESULT_MEASURE_INCHES, inches)
            }
            is MeasureDisplayUnitMeter -> {
                resultIntent.putExtra(RESULT_MEASURE_METERS, displayUnit.getRounded(distance))
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun updateCursor(hitResult: HitResult) {
        // release previous anchor only if it is not used by any other node
        val anchor = cursorNode?.anchor
        if (anchor != null && anchor != firstNode?.anchor && anchor != (secondNode as? AnchorNode)?.anchor) {
            anchor.detach()
        }

        try {
            val newAnchor = hitResult.createAnchor()
            val cursorNode = getCursorNode()
            cursorNode.anchor = newAnchor

            if (measureState == MeasureState.MEASURING) {
                (secondNode as? AnchorNode)?.anchor = newAnchor
                updateDistance()
            }
        } catch (e: Exception) {
            Log.e("MeasureActivity", "Error", e)
        }
    }

    private fun updateVerticalMeasuring(cameraPose: Pose) {
        val cameraPos = cameraPose.position
        val nodePos = firstNode!!.worldPosition

        val cameraToNodeHeightDifference = cameraPos.y - nodePos.y
        val cameraToNodeDistanceOnPlane = sqrt((cameraPos.x - nodePos.x).pow(2) + (cameraPos.z - nodePos.z).pow(2))
        val cameraAngle = cameraPose.pitch

        val normalizedCameraAngle = normalizeRadians(cameraAngle.toDouble(), -PI)
        val pi2 = PI / 2
        if (normalizedCameraAngle < -pi2 * 2 / 3 || normalizedCameraAngle > +pi2 * 1 / 2) {
            setTrackingMessage(R.string.ar_core_tracking_error_too_steep_angle)
            return
        } else {
            setTrackingMessage(null)
        }

        // don't allow negative heights (into the ground)
        val height = max(0f, cameraToNodeHeightDifference + cameraToNodeDistanceOnPlane * tan(cameraAngle))

        val pos = Vector3.add(nodePos, Vector3(0f, height, 0f))
        secondNode?.worldPosition = pos

        updateDistance()
    }

    private fun updateDistance() {
        val pos1 = firstNode?.worldPosition
        val pos2 = secondNode?.worldPosition
        val up = firstNode?.up
        val hasMeasurement = pos1 != null && pos2 != null && up != null

        binding.measurementSpeechBubble.isInvisible = !hasMeasurement
        if (!hasMeasurement) return

        val difference = Vector3.subtract(pos1, pos2)
        distance = difference.length()
        binding.measurementTextView.text = displayUnit.format(distance)

        val line = getLineNode()
        line.worldPosition = Vector3.add(pos1, pos2).scaled(.5f)
        line.worldRotation = Quaternion.lookRotation(difference, up)
        line.localScale = Vector3(1f, 1f, distance)
    }

    private fun getCursorNode(): AnchorNode {
        var node = cursorNode
        if (node == null) {
            node = AnchorNode().apply {
                renderable = cursorRenderable
                setParent(arSceneView!!.scene)
            }
            cursorNode = node
        }
        return node
    }

    private fun getLineNode(): Node {
        var node = lineNode
        if (node == null) {
            node = Node().apply {
                renderable = lineRenderable
                setParent(arSceneView!!.scene)
            }
            lineNode = node
        }
        return node
    }

    /* ----------------------------------- Permission request ----------------------------------- */

    /** Show dialog that explains why the camera permission is necessary. Returns whether the user
     *  acknowledged the rationale. */
    private suspend fun askUserToAcknowledgeCameraPermissionRationale(): Boolean =
        suspendCancellableCoroutine { cont ->
            val dlg = AlertDialog.Builder(this)
                .setTitle(R.string.no_camera_permission_warning_title)
                .setMessage(R.string.no_camera_permission_warning)
                .setPositiveButton(android.R.string.ok) { _, _ -> cont.resume(true) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> cont.resume(false) }
                .setOnCancelListener { cont.resume(false) }
                .create()
            cont.invokeOnCancellation { dlg.cancel() }
            dlg.show()
        }

    /* ----------------------------------------- Intent ----------------------------------------- */

    companion object {
        private const val PARAM_MEASURE_VERTICAL = "measure_vertical"
        private const val PARAM_DISPLAY_UNIT = "display_unit"
        private const val PARAM_REQUEST_RESULT = "request_result"

        /** The action to identify a result */
        const val RESULT_ACTION = "de.westnordost.streetcomplete.screens.measure.RESULT_ACTION"

        /** The result as displayed to the user, set if display unit was meters. Float. */
        const val RESULT_MEASURE_METERS = "measure_result_meters"

        /** The result as displayed to the user, set if display unit was feet+inches. Int. */
        const val RESULT_MEASURE_FEET = "measure_result_feet"

        /** The result as displayed to the user, set if display unit was feet+inches. Int. */
        const val RESULT_MEASURE_INCHES = "measure_result_inches"

        /** Create the intent for starting this activity, with optional parameters:
         * @param vertical whether to measure vertical distances
         * @param unit specifies which unit (meters or foot/inch) should be used for display and
         *             with which precision the measure should be shown (where to round to).
         * @param requestResult whether this activity should return a result. If yes, the activity
         *                      will return the raw measure result (not rounded) in
         *                      RESULT_MEASURE_IN_METERS when the user confirmed it. */
        fun createIntent(
            context: Context,
            vertical: Boolean? = null,
            unit: MeasureDisplayUnit? = null,
            requestResult: Boolean? = null,
        ): Intent {
            val intent = Intent(context, MeasureActivity::class.java)
            vertical?.let { intent.putExtra(PARAM_MEASURE_VERTICAL, it) }
            unit?.let { intent.putExtra(PARAM_DISPLAY_UNIT, Json.encodeToString(it)) }
            requestResult?.let { intent.putExtra(PARAM_REQUEST_RESULT, it) }
            return intent
        }
    }
}
