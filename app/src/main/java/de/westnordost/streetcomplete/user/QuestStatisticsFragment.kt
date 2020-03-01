package de.westnordost.streetcomplete.user

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.ktx.awaitLayout
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import kotlinx.android.synthetic.main.fragment_quest_statistics.*
import kotlinx.coroutines.*
import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Shows the user's solved quests of each type in some kind of ball pit. Clicking on each opens
 *  a QuestTypeInfoFragment that shows the quest's details. */
class QuestStatisticsFragment :
    Fragment(R.layout.fragment_quest_statistics),
    CoroutineScope by CoroutineScope(Dispatchers.Main),
    SensorEventListener
{
    @Inject internal lateinit var questStatisticsDao: QuestStatisticsDao
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry

    private val mainHandler = Handler(Looper.getMainLooper())
    private val physicsController: PhysicsWorldController

    private val questBodyDef: BodyDef

    private lateinit var sensorManager: SensorManager
    private lateinit var display: Display
    private var accelerometer: Sensor? = null

    private var solvedQuestsByQuestType: Map<QuestType<*>, Int> = mapOf()
    private var minPixelsPerMeter: Float = 1f

    private lateinit var worldBounds: RectF

    interface Listener {
        fun onClickedQuestType(questType: QuestType<*>, solvedCount: Int, questBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        Injector.instance.applicationComponent.inject(this)

        physicsController = PhysicsWorldController(Vec2(0f,-10f))

        questBodyDef = BodyDef()
        questBodyDef.type = BodyType.DYNAMIC
        questBodyDef.fixedRotation = false
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        physicsController.listener = object : PhysicsWorldController.Listener {
            override fun onWorldStep() {
                physicsView?.postInvalidate()
            }
        }

        physicsView.alpha = 0f
        physicsView.animate().alpha(1f).setDuration(2000L).setStartDelay(600L).start()

        emptyText.visibility = View.GONE

        launch {
            withContext(Dispatchers.IO) {
                // TODO just for testing...
                solvedQuestsByQuestType = mapOf(
                    "AddMaxHeight" to 12,
                    "AddParkingFee" to 18,
                    "AddRailwayCrossingBarrier" to 8,
                    "AddWayLit" to 4,
                    "AddToiletsFee" to 20,
                    "AddSidewalk" to 1
                )
                    //questStatisticsDao.getAll()
                        .filterKeys { questTypeRegistry.getByName(it) != null }
                        .mapKeys { questTypeRegistry.getByName(it.key)!! }
            }

            val totalSolvedQuests = solvedQuestsByQuestType.values.sum()

            emptyText.visibility = if (totalSolvedQuests == 0) View.VISIBLE else View.GONE

            var areaInMeters = ONE_QUEST_SIZE_IN_M2 * totalSolvedQuests / QUESTS_FILL_FACTOR
            if (areaInMeters == 0f) areaInMeters = 1f
            setupScene(areaInMeters)

            addQuestsToScene()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, mainHandler) }
        physicsController.resume()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        physicsController.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        physicsController.destroy()
        coroutineContext.cancel()
    }

    /* --------------------------------- Set up physics layout  --------------------------------- */

    private suspend fun setupScene(areaInMeters: Float) {
        physicsView.awaitLayout()

        val width = physicsView.width.toFloat()
        val height = physicsView.height.toFloat()
        minPixelsPerMeter = sqrt(width * height / areaInMeters)
        physicsView.pixelsPerMeter = minPixelsPerMeter

        val widthInMeters = width / minPixelsPerMeter
        val heightInMeters = height / minPixelsPerMeter
        worldBounds = RectF(0f,0f, widthInMeters, heightInMeters)

        createWorldBounds(worldBounds)
    }

    private suspend fun addQuestsToScene() {
        for ((questType, solved) in solvedQuestsByQuestType) {
            val length = (sqrt(solved * ONE_QUEST_SIZE_IN_M2/PI) * 2).toFloat()
            val spawnPos = Vec2(
                length + Math.random().toFloat() * (worldBounds.width() - 2 * length),
                length + Math.random().toFloat() * (worldBounds.height() - 2 * length))
            addQuestType(questType, solved, spawnPos)
        }
    }

    private suspend fun createWorldBounds(rect: RectF): Body {
        val bodyDef = BodyDef()
        bodyDef.type = BodyType.STATIC

        val shape = ChainShape()
        shape.createLoop(arrayOf(
            Vec2(0f, 0f),
            Vec2(rect.width(), 0f),
            Vec2(rect.width(), rect.height()),
            Vec2(0f, rect.height())
        ), 4)

        return physicsController.createBody(bodyDef, shape, 0f)
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun addQuestType(questType: QuestType<*>, amountSolved: Int, position: Vec2) {
        val ctx = context!!

        val radius = sqrt((amountSolved * ONE_QUEST_SIZE_IN_M2)/PI)
        val body = createQuestBody(position, radius.toFloat())
        val questView = ImageView(ctx)
        questView.id = View.generateViewId()
        questView.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        questView.setImageResource(questType.icon)

        val clickableContainer = FrameLayout(ctx)
        // foreground attribute only exists on FrameLayout up until KITKAT
        clickableContainer.foreground = resources.getDrawable(R.drawable.round_pressed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clickableContainer.elevation = 4f.toDp(ctx)
            clickableContainer.outlineProvider = CircularOutlineProvider
        }
        clickableContainer.addView(questView)
        clickableContainer.setOnTouchListener(object : SimpleGestureListener(clickableContainer) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                listener?.onClickedQuestType(questType, solvedQuestsByQuestType.getValue(questType), clickableContainer)
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                val a = clickableContainer.rotation / 180.0 * PI
                val vx = (cos(a) * velocityX - sin(a) * velocityY).toFloat()
                val vy = (cos(a) * velocityY + sin(a) * velocityX).toFloat()
                onFlingQuestType(body, vx, vy)
                return true
            }
        })

        physicsView.addView(clickableContainer, body)
    }

    private suspend fun createQuestBody(position: Vec2, radius: Float): Body {
        val shape = CircleShape()
        shape.radius = radius
        // might feel better if the quest circles behave like balls:
        // So density = volume of ball / area of circle of the same radius
        val density = 4f/3f * radius
        questBodyDef.position = position
        return physicsController.createBody(questBodyDef, shape, density)
    }

    /* ---------------------------- Interaction with quest bubbles  ----------------------------- */

    private fun onFlingQuestType(body: Body, velocityX: Float, velocityY: Float) {
        val pixelsPerMeter = physicsView?.pixelsPerMeter ?: return
        val vx = FLING_SPEED_FACTOR * velocityX / pixelsPerMeter
        val vy = FLING_SPEED_FACTOR * -velocityY / pixelsPerMeter
        body.linearVelocity = Vec2(vx, vy).addLocal(body.linearVelocity)
    }

    /* --------------------------------- Sensor event listener ---------------------------------- */

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.accuracy < SENSOR_STATUS_ACCURACY_LOW) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        physicsController.gravity = when (display.rotation) {
            Surface.ROTATION_90 -> Vec2(y,-x)
            Surface.ROTATION_180 -> Vec2(x,y)
            Surface.ROTATION_270 -> Vec2(-y,x)
            else -> Vec2(-x,-y)
        }
    }

    companion object {
        private const val ONE_QUEST_SIZE_IN_M2 = 0.01f
        private const val QUESTS_FILL_FACTOR = 0.5f
        private const val FLING_SPEED_FACTOR = 0.3f
    }
}

private open class SimpleGestureListener(private val view: View) : GestureDetector.SimpleOnGestureListener(), View.OnTouchListener {
    private val gestureDetector = GestureDetector(view.context, this)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.actionMasked) {
            ACTION_DOWN -> view.isPressed = true
            ACTION_UP -> view.isPressed = false
        }
        v?.parent?.requestDisallowInterceptTouchEvent(true)
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent?): Boolean = true
}
