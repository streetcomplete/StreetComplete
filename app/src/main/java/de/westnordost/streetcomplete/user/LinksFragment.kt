package de.westnordost.streetcomplete.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.achievements.AchievementsModule
import de.westnordost.streetcomplete.ktx.awaitLayout
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.ktx.tryStartActivity
import de.westnordost.streetcomplete.view.GridLayoutSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_links.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


/** Shows the user's unlocked links */
class LinksFragment : Fragment(R.layout.fragment_links),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = context!!

        launch {
            view.awaitLayout()

            val viewWidth = view.width.toFloat().toDp(ctx)
            val minCellWidth = 280f
            val itemSpacing = ctx.resources.getDimensionPixelSize(R.dimen.links_item_margin)
            val spanCount = (viewWidth / minCellWidth).toInt()

            // TODO real data...
            val adapter = GroupedLinksAdapter(AchievementsModule.links.values.toList(), this@LinksFragment::openUrl)
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
