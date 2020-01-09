package de.westnordost.streetcomplete.user

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
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import kotlinx.android.synthetic.main.fragment_quest_statistics.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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

    init {
        Injector.instance.applicationComponent.inject(this)

        physicsController = PhysicsWorldController(Vec2(0f,-5f))

        questBodyDef = BodyDef()
        questBodyDef.type = BodyType.DYNAMIC
        questBodyDef.fixedRotation = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



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

    private suspend fun addQuestType(questType: QuestType<*>, amount: Int, position: Vec2) {
        val radius = sqrt(amount/PI)
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
}
