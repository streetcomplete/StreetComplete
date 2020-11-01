package de.westnordost.streetcomplete.measurement

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.activity_measurement.*
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt
import com.google.ar.sceneform.rendering.Color as arColor

class ARCoreMeasurementActivity : AppCompatActivity(), Scene.OnUpdateListener {

    @Inject internal lateinit var prefs: SharedPreferences

    private var arFragment: ArFragment? = null

    private var cubeRenderable: ModelRenderable? = null
    private var distanceCardViewRenderable: ViewRenderable? = null

    private val placedAnchors = ArrayList<Anchor>()
    private val placedAnchorNodes = ArrayList<AnchorNode>()

    private var showingTutorialHint = true

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast
                .makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG)
                .show()
        }

        setContentView(R.layout.activity_measurement)
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneformFragment) as ArFragment?

        initRenderable()
        initButtons()
        initHint()

        arFragment!!.activity
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
            if (cubeRenderable == null || distanceCardViewRenderable == null) {
                return@setOnTapArPlaneListener
            }
            handleTapMeasurement(hitResult)
        }
    }

    private fun initRenderable() {
        MaterialFactory.makeOpaqueWithColor(this, arColor(Color.WHITE))
            .thenAccept { material: Material? ->
                cubeRenderable = ShapeFactory.makeCylinder(
                    0.02f,
                    0f,
                    Vector3.zero(),
                    material
                )
                cubeRenderable!!.isShadowCaster = false
                cubeRenderable!!.isShadowReceiver = false
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error") // TODO sst: what to do with this?
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

        ViewRenderable
            .builder()
            .setView(this, R.layout.distance_text_layout)
            .build()
            .thenAccept { viewRenderable ->
                distanceCardViewRenderable = viewRenderable
                distanceCardViewRenderable!!.isShadowCaster = false
                distanceCardViewRenderable!!.isShadowReceiver = false
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error") // TODO sst: what to do with this?
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun initButtons() {
        clearButton.setOnClickListener { clearAllAnchors() }
        okButton.setOnClickListener {
            prefs.edit().putBoolean(Prefs.HAS_COMPLETED_ARCORE_MEASUREMENT, true).apply()

            val data = Intent()
            data.putExtra(RESULT_ATTRIBUTE_DISTANCE, measureDistance())
            setResult(RESULT_OK, data)

            finish()
        }
    }

    private fun initHint() {
        var hasCompletedMeasurementAlready = prefs.getBoolean(Prefs.HAS_COMPLETED_ARCORE_MEASUREMENT, false)
        hasCompletedMeasurementAlready = false // TODO sst: remove after testing
        if (!hasCompletedMeasurementAlready) {
            setTutorialHint()
        } else {
            setHintFromIntent()
        }

        hintActionButton.setOnClickListener {
            if (showingTutorialHint) {
                setHintFromIntent()
            } else {
                val hideAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out_to_top)
                hideAnimation.fillAfter = true
                hintLayout.startAnimation(hideAnimation)
            }
        }
    }

    private fun setTutorialHint() {
        hintTitleView.text = "How to Measure?" // TODO sst: load from resources.
        hintTextView.text = "To perform a measurement, start moving the camera around and fixate two points in the environment with a simple tap."
        hintActionButton.text = "Next"
        showingTutorialHint = true
    }

    private fun setHintFromIntent() {
        hintTitleView.text = "Measurement Instructions" // TODO sst: load from resources.
        hintTextView.text = "Please measure the width of the sidewalk at its narrowest section. Please also consider permanent objects such as trash cans or street lights when you determine the narrowest point."
        hintActionButton.text = "Hide"
        showingTutorialHint = false
    }

    private fun clearAllAnchors() {
        placedAnchors.clear()
        for (anchorNode in placedAnchorNodes) {
            arFragment!!.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)
        }
        placedAnchorNodes.clear()

        okButton.visibility = View.INVISIBLE
        clearButton.visibility = View.INVISIBLE
    }

    private fun placeAnchor(hitResult: HitResult, renderable: Renderable?) {
        val anchor = hitResult.createAnchor()
        placedAnchors.add(anchor)

        val anchorNode = AnchorNode(anchor).apply {
            isSmoothed = true
            setParent(arFragment!!.arSceneView.scene)
        }
        placedAnchorNodes.add(anchorNode)

        if (renderable != null) {
            TransformableNode(arFragment!!.transformationSystem)
                .apply {
                    this.rotationController.isEnabled = false
                    this.scaleController.isEnabled = false
                    this.translationController.isEnabled = true
                    this.renderable = renderable
                    setParent(anchorNode)
                    select()
                }
        }

        arFragment!!.arSceneView.scene.addOnUpdateListener(this)
        arFragment!!.arSceneView.scene.addChild(anchorNode)
    }

    private fun handleTapMeasurement(hitResult: HitResult) {
        when (placedAnchorNodes.size) {
            0 -> {
                placeAnchor(hitResult, cubeRenderable!!)
                clearButton.visibility = View.VISIBLE
            }
            1 -> {
                placeAnchor(hitResult, null)
                drawLine(placedAnchorNodes[0], placedAnchorNodes[1])

                clearButton.visibility = View.VISIBLE
                okButton.visibility = View.VISIBLE
            }
            else -> {
                clearAllAnchors()
                placeAnchor(hitResult, cubeRenderable!!)
            }
        }
    }

    private fun drawLine(node1: AnchorNode, node2: AnchorNode) {
        val point1: Vector3 = node1.worldPosition
        val point2: Vector3 = node2.worldPosition

        val difference = Vector3.subtract(point1, point2)
        val directionFromTopToBottom = difference.normalized()
        val rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())

        MaterialFactory.makeOpaqueWithColor(
            applicationContext,
            arColor(Color.WHITE)
        )
            .thenAccept { material ->
                val lineNode = Node().apply {
                    val model = ShapeFactory.makeCube(
                        Vector3(0.01f, 0.0f, difference.length()),
                        Vector3.zero(),
                        material
                    ).apply {
                        this.isShadowCaster = false
                        this.isShadowReceiver = false
                    }

                    this.setParent(node1) // Do this before editing world position.
                    this.renderable = model
                    this.worldPosition = Vector3.add(point1, point2).scaled(.5f)
                    this.worldRotation = rotationFromAToB
                }

                TransformableNode(arFragment!!.transformationSystem)
                    .apply {
                        this.setParent(lineNode)
                        this.rotationController.isEnabled = false
                        this.scaleController.isEnabled = false
                        this.translationController.isEnabled = true
                        this.renderable = distanceCardViewRenderable
                        this.localRotation = Quaternion.axisAngle(Vector3.down(), 90.0f)
                        this.localPosition = Vector3(0.0f, 0.01f, 0.0f)
                    }

                // End point is drawn together with the line (and the same parent)
                // so that they do not come out of sync if the plane detection struggles...
                TransformableNode(arFragment!!.transformationSystem)
                    .apply {
                        this.setParent(node1) // Do this before editing world position.
                        this.rotationController.isEnabled = false
                        this.scaleController.isEnabled = false
                        this.translationController.isEnabled = true
                        this.renderable = cubeRenderable
                        this.worldPosition = point2
                    }
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onUpdate(frameTime: FrameTime) {
        measureDistance()
    }

    private fun measureDistance(): Float? {
        if (placedAnchorNodes.size == 2) {
            val distanceMeter = calculateDistance(
                placedAnchorNodes[0].worldPosition,
                placedAnchorNodes[1].worldPosition
            )
            val distanceCentimeter = distanceMeter * 100
            updateDistanceCardText(distanceCentimeter)
            return distanceMeter
        } else {
            return null
        }
    }

    private fun calculateDistance(objectPose0: Vector3, objectPose1: Vector3): Float {
        return calculateDistance(
            objectPose0.x - objectPose1.x,
            objectPose0.y - objectPose1.y,
            objectPose0.z - objectPose1.z
        )
    }

    private fun calculateDistance(x: Float, y: Float, z: Float): Float {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }

    private fun updateDistanceCardText(distanceCentimeter: Float) {
        val distanceFloor = "%.0f".format(distanceCentimeter)
        val distanceText = "$distanceFloor cm"

        val textView = (distanceCardViewRenderable!!.view as LinearLayout)
            .findViewById<TextView>(R.id.distanceCard)
        textView.text = distanceText
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        val activityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val openGlVersionString = activityManager.deviceConfigurationInfo.glEsVersion

        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES $MIN_OPENGL_VERSION later")
            Toast.makeText(
                activity,
                "Sceneform requires OpenGL ES $MIN_OPENGL_VERSION or later",
                Toast.LENGTH_LONG
            ).show()
            activity.finish()
            return false
        }
        return true
    }

    companion object {
        const val REQUEST_CODE_MEASURE_DISTANCE = 0
        const val RESULT_ATTRIBUTE_DISTANCE = "DISTANCE"

        private const val TAG = "ARCoreMeasurement"
        private const val MIN_OPENGL_VERSION = 3.0
    }
}
