package de.westnordost.streetcomplete.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.LinkCategory
import kotlinx.android.synthetic.main.card_link_item.view.*
import kotlinx.android.synthetic.main.row_link_category_item.view.*

/** Adapter for a list of links, grouped by category */
class GroupedLinksAdapter(links: List<Link>, private val onClickLink: (url: String) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val groupedLinks: List<Item> = links
        .groupBy { it.category }
        .flatMap { entry ->
            val category = entry.key
            val linksInCategory = entry.value
            listOf(CategoryItem(category)) + linksInCategory.map { LinkItem(it) }
        }

    private val itemCount = groupedLinks.size

    override fun getItemCount(): Int = itemCount

    override fun getItemViewType(position: Int): Int = when (groupedLinks[position]) {
        is CategoryItem -> CATEGORY
        is LinkItem -> LINK
    }

    fun shouldItemSpanFullWidth(position: Int): Boolean = groupedLinks[position] is CategoryItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            CATEGORY -> CategoryViewHolder(inflater.inflate(R.layout.row_link_category_item, parent, false))
            LINK -> LinkViewHolder(inflater.inflate(R.layout.card_link_item, parent, false))
            else -> throw IllegalStateException("Unexpected viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = groupedLinks[position]) {
            is CategoryItem -> (holder as CategoryViewHolder).onBind(item.category)
            is LinkItem ->  (holder as LinkViewHolder).onBind(item.link)
        }
    }

    inner class LinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(with: Link) {
            if (with.icon != null) {
                itemView.linkIconImageView.setImageResource(with.icon)
            } else {
                itemView.linkIconImageView.setImageDrawable(null)
            }
            itemView.linkTitleTextView.text = with.title
            if (with.description != null) {
                itemView.linkDescriptionTextView.setText(with.description)
            } else {
                itemView.linkDescriptionTextView.text = ""
            }
            itemView.setOnClickListener { onClickLink(with.url) }
        }
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(with: LinkCategory) {
            itemView.linkCategoryTitleTextView.setText(with.title)
            val description = with.description
            itemView.linkCategoryDescriptionTextView.isGone = description == null
            if (description != null) {
                itemView.linkCategoryDescriptionTextView.setText(description)
            } else {
                itemView.linkCategoryDescriptionTextView.text = ""
            }
        }
    }

    companion object {
        private const val LINK = 0
        private const val CATEGORY = 1
    }
}

private sealed class Item

private data class CategoryItem(val category: LinkCategory) : Item()
private data class LinkItem(val link: Link) : Item()

private val LinkCategory.title: Int get() = when(this) {
    LinkCategory.INTRO -> R.string.link_category_intro_title
    LinkCategory.EDITORS -> R.string.link_category_editors_title
    LinkCategory.MAPS -> R.string.link_category_maps_title
    LinkCategory.SHOWCASE -> R.string.link_category_showcase_title
    LinkCategory.GOODIES -> R.string.link_category_goodies_title
}

private val LinkCategory.description: Int? get() = when(this) {
    LinkCategory.INTRO -> R.string.link_category_intro_description
    LinkCategory.EDITORS -> R.string.link_category_editors_description
    LinkCategory.SHOWCASE -> R.string.link_category_showcase_description
    LinkCategory.MAPS -> R.string.link_category_maps_description
    LinkCategory.GOODIES -> R.string.link_category_goodies_description
}
