package de.westnordost.streetcomplete.user

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity(R.layout.activity_user) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = PAGES.size
            override fun createFragment(position: Int) = PAGES[position].creator()
        }

        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            val page = PAGES[position]
            tab.setIcon(page.icon)
            if (resources.getBoolean(R.bool.show_user_tabs_text)) {
                tab.setText(page.title)
            } else {
                tab.text = ""
            }
        }.attach()
    }
}

private data class UserPage(@StringRes val title: Int, @DrawableRes val icon: Int, val creator: () -> Fragment)

private val PAGES = listOf(
    // TODO proper icons
    UserPage(R.string.user_quests_title, R.drawable.ic_star_48dp) { QuestStatisticsFragment() },
    UserPage(R.string.user_achievements_title, R.drawable.ic_star_48dp) { AchievementsFragment() },
    UserPage(R.string.user_links_title, R.drawable.ic_star_48dp) { LinksFragment() }
)
