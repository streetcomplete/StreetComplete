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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.data.user.UserStore
import de.westnordost.streetcomplete.ktx.awaitLayout
import de.westnordost.streetcomplete.ktx.sumByFloat
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
import kotlin.math.*

/** Shows the user's solved quests of each type in some kind of ball pit. Clicking on each opens
 *  a QuestTypeInfoFragment that shows the quest's details. */
class QuestStatisticsFragment :
    Fragment(R.layout.fragment_quest_statistics)
{
    @Inject internal lateinit var questStatisticsDao: QuestStatisticsDao
    @Inject internal lateinit var userStore: UserStore
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry

    interface Listener {
        fun onClickedQuestType(questType: QuestType<*>, solvedCount: Int, questBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(ballPitView)

        val solvedQuestsByQuestType = questStatisticsDao.getAll()
                    .filterKeys { questTypeRegistry.getByName(it) != null }
                    .mapKeys { questTypeRegistry.getByName(it.key)!! }
        val totalSolvedQuests = solvedQuestsByQuestType.values.sum()

        emptyText.visibility = if (totalSolvedQuests == 0) View.VISIBLE else View.GONE
        ballPitView.setViews(solvedQuestsByQuestType.map { (questType, amount) ->
            createQuestTypeBubbleView(questType, amount) to amount
        })
    }

    override fun onStart() {
        super.onStart()

        if (userStore.isSynchronizingStatistics) {
            emptyText.setText(R.string.stats_are_syncing)
        } else {
            emptyText.setText(R.string.quests_empty)
        }
    }

    private fun createQuestTypeBubbleView(questType: QuestType<*>, solvedCount: Int): View {
        val ctx = requireContext()
        val questView = ImageView(ctx)
        questView.id = View.generateViewId()
        questView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        questView.scaleType = ImageView.ScaleType.FIT_XY
        questView.setImageResource(questType.icon)

        val clickableContainer = FrameLayout(ctx)
        clickableContainer.layoutParams = ViewGroup.LayoutParams(256,256)
        // foreground attribute only exists on FrameLayout up until KITKAT
        clickableContainer.foreground = resources.getDrawable(R.drawable.round_pressed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clickableContainer.elevation = 4f.toDp(ctx)
            clickableContainer.outlineProvider = CircularOutlineProvider
        }
        clickableContainer.addView(questView)
        clickableContainer.setOnClickListener { v ->
            listener?.onClickedQuestType(questType, solvedCount, v)
        }

        return clickableContainer
    }
}

