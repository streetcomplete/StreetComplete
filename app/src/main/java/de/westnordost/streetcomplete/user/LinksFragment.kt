package de.westnordost.streetcomplete.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.UserStore
import de.westnordost.streetcomplete.data.user.achievements.UserLinksSource
import de.westnordost.streetcomplete.ktx.awaitPreDraw
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.ktx.tryStartActivity
import de.westnordost.streetcomplete.view.GridLayoutSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_links.*
import kotlinx.coroutines.*
import javax.inject.Inject

/** Shows the user's unlocked links */
class LinksFragment : Fragment(R.layout.fragment_links),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var userLinksSource: UserLinksSource
    @Inject internal lateinit var userStore: UserStore

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val minCellWidth = 280f
        val itemSpacing = ctx.resources.getDimensionPixelSize(R.dimen.links_item_margin)

        launch {
            view.awaitPreDraw()

            emptyText.visibility = View.GONE

            val viewWidth = view.width.toFloat().toDp(ctx)
            val spanCount = (viewWidth / minCellWidth).toInt()

            val links = withContext(Dispatchers.IO) {
                userLinksSource.getLinks()
            }
            val adapter = GroupedLinksAdapter(links, this@LinksFragment::openUrl)
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
            linksList.addItemDecoration(GridLayoutSpacingItemDecoration(itemSpacing))
            linksList.layoutManager = layoutManager
            linksList.adapter = adapter
            linksList.clipToPadding = false

            emptyText.visibility = if (links.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onStart() {
        super.onStart()

        if (userStore.isSynchronizingStatistics) {
            emptyText.setText(R.string.stats_are_syncing)
        } else {
            emptyText.setText(R.string.links_empty)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        tryStartActivity(intent)
    }
}
