package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TAB_LABEL_VISIBILITY_LABELED
import com.google.android.material.tabs.TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
import com.google.android.material.tabs.TabLayoutMediator
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.achievements.Achievement
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity(R.layout.activity_user),
    AchievementsFragment.Listener, QuestStatisticsFragment.Listener {

    private val questTypeDetailsFragment: QuestTypeInfoFragment?
        get() = supportFragmentManager.findFragmentById(R.id.questTypeDetailsFragment) as QuestTypeInfoFragment

    private val achievementDetailsFragment: AchievementInfoFragment?
        get() = supportFragmentManager.findFragmentById(R.id.achievementDetailsFragment) as AchievementInfoFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = PAGES.size
            override fun createFragment(position: Int) = PAGES[position].creator()
        }

        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            val page = PAGES[position]
            tab.setIcon(page.icon)
            tab.setText(page.title)
            tab.tabLabelVisibility =
                if (resources.getBoolean(R.bool.show_user_tabs_text)) TAB_LABEL_VISIBILITY_LABELED
                else TAB_LABEL_VISIBILITY_UNLABELED
        }.attach()
    }


    override fun onBackPressed() {
        val questTypeDetailsFragment = questTypeDetailsFragment
        if (questTypeDetailsFragment != null && questTypeDetailsFragment.isShowing) {
            questTypeDetailsFragment.dismiss()
            return
        }
        val achievementDetailsFragment = achievementDetailsFragment
        if (achievementDetailsFragment != null && achievementDetailsFragment.isShowing) {
            achievementDetailsFragment.dismiss()
            return
        }
        super.onBackPressed()
    }

    override fun onClickedAchievement(achievement: Achievement, level: Int, achievementBubbleView: View) {
        achievementDetailsFragment?.show(achievement, level, achievementBubbleView)
    }

    override fun onClickedQuestType(questType: QuestType<*>, questCount: Int, questBubbleView: View) {
        questTypeDetailsFragment?.show(questType, questCount, questBubbleView)
    }
}

private data class UserPage(@StringRes val title: Int, @DrawableRes val icon: Int, val creator: () -> Fragment)

private val PAGES = listOf(
    // TODO proper icons
    UserPage(R.string.user_quests_title, R.drawable.ic_star_48dp) { QuestStatisticsFragment() },
    UserPage(R.string.user_achievements_title, R.drawable.ic_star_48dp) { AchievementsFragment() },
    UserPage(R.string.user_links_title, R.drawable.ic_star_48dp) { LinksFragment() }
)
