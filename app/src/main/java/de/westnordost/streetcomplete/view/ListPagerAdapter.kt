package de.westnordost.streetcomplete.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ListPagerAdapter(fragmentActivity: FragmentActivity, var pages: List<Page>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int) = pages[position].creator()

    data class Page(@StringRes val title: Int, @DrawableRes val icon: Int, val creator: () -> Fragment)
}
