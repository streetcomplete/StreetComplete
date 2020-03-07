package de.westnordost.streetcomplete.user

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.achievements.Achievement
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.ktx.awaitLayout
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import de.westnordost.streetcomplete.view.GridLayoutSpacingItemDecoration
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.cell_achievement.view.*
import kotlinx.android.synthetic.main.fragment_achievements.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shows the icons for all achieved achievements and opens a AchievementInfoFragment to show the
 *  details on click. */
class AchievementsFragment : Fragment(R.layout.fragment_achievements),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var userController: UserController

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    interface Listener {
        fun onClickedAchievement(achievement: Achievement, level: Int, achievementBubbleView: View)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val minCellWidth = 128f
        val itemSpacing = ctx.resources.getDimensionPixelSize(R.dimen.achievements_item_margin)

        launch {
            view.awaitLayout()

            emptyText.visibility = View.GONE

            val viewWidth = view.width.toFloat().toDp(ctx)
            val spanCount = (viewWidth / minCellWidth).toInt()

            val layoutManager = GridLayoutManager(ctx, spanCount, RecyclerView.VERTICAL, false)
            achievementsList.layoutManager = layoutManager
            achievementsList.addItemDecoration(GridLayoutSpacingItemDecoration(itemSpacing))
            achievementsList.clipToPadding = false
            val achievements = userController.getAchievements()
            achievementsList.adapter = AchievementsAdapter(achievements)

            emptyText.visibility = if (achievements.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    /* -------------------------------------- Interaction --------------------------------------- */

    private inner class AchievementsAdapter(achievements: List<Pair<Achievement, Int>>
    ) : ListAdapter<Pair<Achievement, Int>>(achievements) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_achievement, parent, false))

        inner class ViewHolder(itemView: View) : ListAdapter.ViewHolder<Pair<Achievement, Int>>(itemView) {
            override fun onBind(with: Pair<Achievement, Int>) {
                val achievement = with.first
                val level = with.second
                itemView.achievementIconView.setImageResource(achievement.icon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.outlineProvider = CircularOutlineProvider
                    itemView.elevation = 4f.toDp(itemView.context)
                }
                if (level == 1) {
                    itemView.achievementLevelText.visibility = View.INVISIBLE
                    itemView.achievementLevelText.text = ""
                } else {
                    itemView.achievementLevelText.visibility = View.VISIBLE
                    itemView.achievementLevelText.text = level.toString()
                }
                itemView.setOnClickListener {
                    listener?.onClickedAchievement(achievement, level, itemView.achievementIconView)
                }
            }
        }
    }
}
