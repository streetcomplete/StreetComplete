package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TAB_LABEL_VISIBILITY_LABELED
import com.google.android.material.tabs.TabLayout.TAB_LABEL_VISIBILITY_UNLABELED
import com.google.android.material.tabs.TabLayoutMediator
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.ListPagerAdapter
import kotlinx.android.synthetic.main.fragment_user.*

/** Shows the viewpager with the user profile, user statistics, achievements and links */
class UserFragment : Fragment(R.layout.fragment_user), HasTitle {

    override val title: String get() = getString(R.string.user_profile)

    private var tabLayoutMediator: TabLayoutMediator? = null

    /* --------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.adapter = ListPagerAdapter(requireActivity(), PAGES)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout.tabMode = TabLayout.MODE_FIXED
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            val page = PAGES[position]
            tab.setIcon(page.icon)
            tab.setText(page.title)
            tab.tabLabelVisibility =
                if (resources.getBoolean(R.bool.show_user_tabs_text)) TAB_LABEL_VISIBILITY_LABELED
                else TAB_LABEL_VISIBILITY_UNLABELED
        }
        tabLayoutMediator?.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator?.detach()
    }

    companion object {
        private val PAGES = listOf(
            ListPagerAdapter.Page(R.string.user_profile_title, R.drawable.ic_profile_white_48dp) {  ProfileFragment() },
            ListPagerAdapter.Page(R.string.user_quests_title, R.drawable.ic_star_white_48dp) { QuestStatisticsFragment() },
            ListPagerAdapter.Page(R.string.user_achievements_title, R.drawable.ic_achievements_white_48dp) { AchievementsFragment() },
            ListPagerAdapter.Page(R.string.user_links_title, R.drawable.ic_bookmarks_white_48dp) { LinksFragment() }
        )
    }
}