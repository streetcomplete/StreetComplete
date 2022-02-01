package de.westnordost.streetcomplete.measure

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
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ActivityMeasureBinding
import de.westnordost.streetcomplete.ktx.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Activity to measure distances. Can be started as activity for result, see [createIntent] */
class MeasureActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val createArCoreSession = ArCoreSessionCreator(this, ::askUserToAcknowledgeCameraPermissionRationale)

    private lateinit var binding : ActivityMeasureBinding
    private var arSceneView: ArSceneView? = null

    private var cursorRenderable: Renderable? = null
    private var pointRenderable: Renderable? = null
    private var lineRenderable: Renderable? = null

    private var lineNode: Node? = null
    private var firstNode: AnchorNode? = null
    private var secondNode: AnchorNode? = null
    private var cursorNode: AnchorNode? = null

    private var measureMode: MeasureMode = MeasureMode.BOTH
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
                binding.trackingErrorTextView.isGone = true
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
        measureMode = intent.getStringExtra(PARAM_MEASURE_MODE)?.let { MeasureMode.valueOf(it) } ?: MeasureMode.BOTH
        displayUnit = when (intent.getStringExtra(PARAM_DISPLAY_UNIT)) {
            METER -> MeasureDisplayUnitMeter(intent.getIntExtra(PARAM_DISPLAY_UNIT_PRECISION, 2))
            FOOT_AND_INCH -> MeasureDisplayUnitFeetInch(intent.getIntExtra(PARAM_DISPLAY_UNIT_PRECISION, 1))
            else -> MeasureDisplayUnitMeter(2)
        }
        requestResult = intent.getBooleanExtra(PARAM_REQUEST_RESULT, false)
    }

    /* --------------------------------- Scene.OnUpdateListener --------------------------------- */

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView?.arFrame ?: return

        if (frame.hasFoundPlane()) {
            binding.handMotionView.isGone = true
        }

        val textResId = frame.camera.trackingFailureReason.messageResId
        binding.trackingErrorTextView.isGone = textResId == null
        textResId?.let { binding.trackingErrorTextView.setText(textResId) }

        if (frame.camera.trackingState == TrackingState.TRACKING) {
            val centerX = binding.arSceneViewContainer.width / 2f
            val centerY = binding.arSceneViewContainer.height / 2f
            val hitResult = frame.hitPlane(centerX, centerY)

            if (hitResult != null) {
                updateCursor(hitResult)
            }
        }
    }

    /* ------------------------------------------ Session --------------------------------------- */

    private suspend fun initializeSession() {
        val result = createArCoreSession()
        if (result is ArCoreSessionCreator.Success) {
            val session = result.session
            val config = Config(session)
            configureSession(config)
            session.configure(config)
            addArSceneView(session)
        } else if (result is ArCoreSessionCreator.Failure) {
            val reason = result.reason
            if (reason == ArNotAvailableReason.AR_CORE_SDK_TOO_OLD) {
                toast(R.string.ar_core_error_sdk_too_old)
            }
            // otherwise nothing we can do here...
            finish()
        }
    }

    private fun configureSession(config: Config) {
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
        config.planeFindingMode = when(measureMode) {
            MeasureMode.BOTH -> Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            MeasureMode.HORIZONTAL -> Config.PlaneFindingMode.HORIZONTAL
            MeasureMode.VERTICAL -> Config.PlaneFindingMode.VERTICAL
        }
        config.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        config.focusMode = Config.FocusMode.AUTO
        config.depthMode = Config.DepthMode.AUTOMATIC
    }

    private fun addArSceneView(session: Session) {
        val arSceneView = ArSceneView(this)
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
                if (!requestResult) clearMeasuring()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun initRenderables() {
        // takes about half a second on a high-end device(!)
        val materialBlue = MaterialFactory.makeOpaqueWithColor(this, Color(0.1f, 0.4f, 0.9f)).await()
        cursorRenderable = ViewRenderable.builder().setView(this, R.layout.view_ar_cursor).build().await()
        pointRenderable = ShapeFactory.makeCylinder(0.03f, 0.001f, Vector3.zero(), materialBlue)
        lineRenderable = ShapeFactory.makeCube(Vector3(0.02f, 0.001f, 1f), Vector3.zero(), materialBlue)
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
        secondNode = AnchorNode().apply {
            renderable = pointRenderable
            setParent(arSceneView!!.scene)
            setAnchor(anchor)
        }
    }

    private fun measuringDone() {
        binding.arSceneViewContainer.performHapticFeedback(VIRTUAL_KEY)
        if (requestResult) binding.acceptResultContainer.isGone = false
        measureState = MeasureState.DONE
    }

    private fun clearMeasuring() {
        measureState = MeasureState.READY
        binding.arSceneViewContainer.performHapticFeedback(VIRTUAL_KEY)
        binding.measurementSpeechBubble.isInvisible = true
        binding.acceptResultContainer.isGone = true
        distance = 0f
        firstNode?.anchor?.detach()
        firstNode?.setParent(null)
        firstNode = null
        secondNode?.anchor?.detach()
        secondNode?.setParent(null)
        secondNode = null
        lineNode?.setParent(null)
        lineNode = null
    }

    private fun returnMeasuringResult() {
        val resultIntent = Intent(RESULT_ACTION)
        resultIntent.putExtra(RESULT_MEASURE_IN_METERS, distance)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun updateCursor(hitResult: HitResult) {
        // release previous anchor only if it is not used by any other node
        val anchor = cursorNode?.anchor
        if (anchor != null && anchor != firstNode?.anchor && anchor != secondNode?.anchor) {
            anchor.detach()
        }

        try {
            val newAnchor = hitResult.createAnchor()
            val cursorNode = getCursorNode()
            cursorNode.anchor = newAnchor

            if (measureState == MeasureState.MEASURING) {
                secondNode?.anchor = newAnchor
            }
            /* update distance should always be called because the world position could be adjusted
             *  for already existing anchors */
            updateDistance()
        } catch (e: Exception) {
            Log.e("MeasureActivity", "Error", e)
        }
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

        binding.measurementTextView.text = displayUnit.format(distance.toDouble())

        val line = getLineNode()
        line.worldPosition = Vector3.add(pos1, pos2).scaled(.5f)
        line.worldRotation = Quaternion.lookRotation(difference, up)
        line.localScale = Vector3(1f, 1f, distance)
    }

    private fun getCursorNode(): AnchorNode {
        var node = cursorNode
        if (node == null)  {
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
        if (node == null)  {
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
        private const val PARAM_MEASURE_MODE = "measure_mode"
        private const val PARAM_DISPLAY_UNIT = "display_unit"
        private const val PARAM_DISPLAY_UNIT_PRECISION = "display_unit_precision"
        private const val PARAM_REQUEST_RESULT = "request_result"

        private const val METER = "meter"
        private const val FOOT_AND_INCH = "foot_inch"

        /** The action to identify a result */
        const val RESULT_ACTION = "de.westnordost.streetcomplete.measure.RESULT_ACTION"
        /** The exact floating point measure result. It is up to the caller to round and/or convert
         *  it according to his preference */
        const val RESULT_MEASURE_IN_METERS = "measure_result"

        /** Create the intent for starting this activity, with optional parameters:
         * @param mode specifies whether to measure distances on the ground, on walls or both
         * @param unit specifies which unit (meters or foot/inch) should be used for display and
         *             with which precision the measure should be shown (where to round to).
         * @param requestResult whether this activity should return a result. If yes, the activity
         *                      will return the raw measure result (not rounded) in
         *                      RESULT_MEASURE_IN_METERS when the user confirmed it. */
        fun createIntent(
            context: Context,
            mode: MeasureMode? = null,
            unit: MeasureDisplayUnit? = null,
            requestResult: Boolean? = null
        ): Intent {
            val intent = Intent(context, MeasureActivity::class.java)
            mode?.let { intent.putExtra(PARAM_MEASURE_MODE, it.name) }
            when(unit) {
                is MeasureDisplayUnitFeetInch -> {
                    intent.putExtra(PARAM_DISPLAY_UNIT, FOOT_AND_INCH)
                    intent.putExtra(PARAM_DISPLAY_UNIT_PRECISION, unit.inchStep)
                }
                is MeasureDisplayUnitMeter -> {
                    intent.putExtra(PARAM_DISPLAY_UNIT, METER)
                    intent.putExtra(PARAM_DISPLAY_UNIT_PRECISION, unit.decimals)
                }
                null -> {}
            }
            requestResult?.let { intent.putExtra(PARAM_REQUEST_RESULT, it) }
            return intent
        }
    }

    /** Whether to be able to measure on the ground, on walls or both */
    enum class MeasureMode { HORIZONTAL, VERTICAL, BOTH }
}
