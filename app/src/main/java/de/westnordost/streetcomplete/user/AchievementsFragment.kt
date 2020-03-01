package de.westnordost.streetcomplete.user

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.BackPressedListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.achievements.Achievement
import de.westnordost.streetcomplete.data.achievements.AchievementsModule
import de.westnordost.streetcomplete.ktx.awaitLayout
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.cell_achievement.view.*
import kotlinx.android.synthetic.main.fragment_achievements.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Shows the icons for all achieved achievements and opens a AchievementInfoFragment to show the
 *  details on click. */
class AchievementsFragment : Fragment(R.layout.fragment_achievements),
    CoroutineScope by CoroutineScope(Dispatchers.Main),
    BackPressedListener {

    private val detailsFragment: AchievementInfoFragment?
        get() = childFragmentManager.findFragmentById(R.id.detailsFragment) as AchievementInfoFragment

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = context!!

        launch {
            view.awaitLayout()
            val spanCount = (view.width.toFloat().toDp(ctx) / 128).toInt()
            achievementsList.layoutManager = GridLayoutManager(ctx, spanCount, RecyclerView.VERTICAL, false)
            // TODO real data...
            achievementsList.adapter = AchievementsAdapter(AchievementsModule.achievements.values.map { it to 1 })
        }
    }

    override fun onBackPressed(): Boolean {
        val detailsFragment = detailsFragment
        if (detailsFragment != null && detailsFragment.isShowing) {
            detailsFragment.dismiss()
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    /* -------------------------------------- Interaction --------------------------------------- */


    private fun onClickedAchievement(achievement: Achievement, level: Int, achievementBubbleView: View) {
        val detailsFragment = detailsFragment ?: return
        detailsFragment.show(achievement, level, achievementBubbleView)
    }

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
                    itemView.achievementIconView.outlineProvider = CircularOutlineProvider
                    itemView.achievementIconView.elevation = 4f.toDp(itemView.context)
                }
                if (level == 1) {
                    itemView.achievementLevelText.visibility = View.INVISIBLE
                    itemView.achievementLevelText.text = ""
                } else {
                    itemView.achievementLevelText.visibility = View.VISIBLE
                    itemView.achievementLevelText.text = level.toString()
                }
                itemView.setOnClickListener {
                    onClickedAchievement(achievement, level, itemView.achievementIconView)
                }
            }
        }
    }
}
