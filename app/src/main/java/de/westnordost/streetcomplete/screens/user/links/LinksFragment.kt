package de.westnordost.streetcomplete.screens.user.links

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentLinksBinding
import de.westnordost.streetcomplete.util.ktx.awaitLayout
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.GridLayoutSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** Shows the user's unlocked links */
class LinksFragment : Fragment(R.layout.fragment_links) {

    private val achievementsSource: AchievementsSource by inject()
    private val statisticsSource: StatisticsSource by inject()

    private val binding by viewBinding(FragmentLinksBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val minCellWidth = 280f
        val itemSpacing = ctx.resources.getDimensionPixelSize(R.dimen.links_item_margin)

        viewLifecycleScope.launch {
            view.awaitLayout()

            binding.emptyText.visibility = View.GONE

            val viewWidth = ctx.pxToDp(view.width)
            val spanCount = (viewWidth / minCellWidth).toInt()

            val links = withContext(Dispatchers.IO) { achievementsSource.getLinks() }
            val adapter = GroupedLinksAdapter(links, ::openUri)
            // headers should span the whole width
            val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    if (adapter.shouldItemSpanFullWidth(position)) spanCount else 1
            }
            spanSizeLookup.isSpanGroupIndexCacheEnabled = true
            spanSizeLookup.isSpanIndexCacheEnabled = true
            // vertical grid layout
            val layoutManager = GridLayoutManager(ctx, spanCount, RecyclerView.VERTICAL, false)
            layoutManager.spanSizeLookup = spanSizeLookup
            // spacing *between* the items
            binding.linksList.addItemDecoration(GridLayoutSpacingItemDecoration(itemSpacing))
            binding.linksList.layoutManager = layoutManager
            binding.linksList.adapter = adapter
            binding.linksList.clipToPadding = false

            binding.emptyText.isGone = links.isNotEmpty()
        }
    }

    override fun onStart() {
        super.onStart()

        if (statisticsSource.isSynchronizing) {
            binding.emptyText.setText(R.string.stats_are_syncing)
        } else {
            binding.emptyText.setText(R.string.links_empty)
        }
    }
}
