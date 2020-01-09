package de.westnordost.streetcomplete.user

import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.ktx.awaitLayout
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
import kotlin.math.sqrt


class QuestStatisticsFragment :
        Fragment(R.layout.fragment_quest_statistics),
        CoroutineScope by CoroutineScope(Dispatchers.Main)
{
    @Inject internal lateinit var questStatisticsDao: QuestStatisticsDao
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry

    private val physicsController: PhysicsWorldController
    private val questBodyDef: BodyDef

    private var solvedQuestsByQuestType: Map<QuestType<*>, Int> = mapOf()
    private var minPixelsPerMeter: Float = 1f

    init {
        Injector.instance.applicationComponent.inject(this)

        physicsController = PhysicsWorldController(Vec2(0f,-10f))

        questBodyDef = BodyDef()
        questBodyDef.type = BodyType.DYNAMIC
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        physicsController.listener = object : PhysicsWorldController.Listener {
            override fun onWorldStep() {
                physicsView.invalidate()
            }
        }

        launch {
            withContext(Dispatchers.IO) {
                solvedQuestsByQuestType = questStatisticsDao.getAll()
                        .filterKeys { questTypeRegistry.getByName(it) != null }
                        .mapKeys { questTypeRegistry.getByName(it.key)!! }
            }

            val totalSolvedQuests = solvedQuestsByQuestType.values.sum()

            setupScene(totalSolvedQuests * 2f)
        }
    }

    override fun onResume() {
        super.onResume()
        physicsController.resume()
    }

    override fun onPause() {
        super.onPause()
        physicsController.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        physicsController.destroy()
        coroutineContext.cancel()
    }

    private suspend fun setupScene(areaInMeters: Float) {
        physicsView.awaitLayout()

        val width = physicsView.width.toFloat()
        val height = physicsView.height.toFloat()
        minPixelsPerMeter = sqrt(width * height / areaInMeters)
        physicsView.pixelsPerMeter = minPixelsPerMeter

        val widthInMeters = width / minPixelsPerMeter
        val heightInMeters = height / minPixelsPerMeter
        val worldAreaInMeters = RectF(0f,0f, widthInMeters, heightInMeters)
        createWorldBounds(worldAreaInMeters)

        for ((questType, solved) in solvedQuestsByQuestType) {
            val length = (sqrt(solved/PI) * 2).toFloat()
            val spawnPos = Vec2(
                    length + Math.random().toFloat() * (widthInMeters - 2 * length),
                    length + Math.random().toFloat() * (heightInMeters - 2 * length))
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

    private suspend fun addQuestType(questType: QuestType<*>, amountSolved: Int, position: Vec2) {
        val radius = sqrt(amountSolved/PI)
        val body = createQuestBody(position, radius.toFloat())
        val questView = ImageView(context)
        questView.id = View.generateViewId()
        questView.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        questView.setImageResource(questType.icon)
        questView.setOnClickListener { view -> onClickedQuestType(view, questType) }

        physicsView.addView(questView, body)
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

    private fun onClickedQuestType(view: View, questType: QuestType<*>) {
        // TODO

    }

    // TODO zooming
}
