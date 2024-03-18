package de.westnordost.streetcomplete.screens.user.links

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentLinksBinding
import de.westnordost.streetcomplete.util.ktx.awaitLayout
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.GridLayoutSpacingItemDecoration
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the user's unlocked links */
class LinksFragment : Fragment(R.layout.fragment_links) {

    private val viewModel by viewModel<LinksViewModel>()
    private val binding by viewBinding(FragmentLinksBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emptyText.isGone = true

        observe(viewModel.isSynchronizingStatistics) { isSynchronizingStatistics ->
            binding.emptyText.setText(
                if (isSynchronizingStatistics) {
                    R.string.stats_are_syncing
                } else {
                    R.string.links_empty
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            view.awaitLayout()

            val minCellWidth = resources.dpToPx(280)
            val spanCount = (view.width / minCellWidth).toInt()

            observe(viewModel.links) { links ->
                binding.emptyText.isGone = links == null || links.isNotEmpty()
                if (links != null) {
                    val adapter = GroupedLinksAdapter(links, ::openUri)
                    // headers should span the whole width
                    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int =
                            if (adapter.shouldItemSpanFullWidth(position)) spanCount else 1
                    }
                    spanSizeLookup.isSpanGroupIndexCacheEnabled = true
                    spanSizeLookup.isSpanIndexCacheEnabled = true
                    // vertical grid layout
                    val layoutManager = GridLayoutManager(requireContext(), spanCount, RecyclerView.VERTICAL, false)
                    layoutManager.spanSizeLookup = spanSizeLookup
                    // spacing *between* the items
                    val itemSpacing = resources.getDimensionPixelSize(R.dimen.links_item_margin)
                    binding.linksList.addItemDecoration(GridLayoutSpacingItemDecoration(itemSpacing))
                    binding.linksList.layoutManager = layoutManager
                    binding.linksList.adapter = adapter
                    binding.linksList.clipToPadding = false
                }
            }
        }
    }
}
