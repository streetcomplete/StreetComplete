package de.westnordost.streetcomplete.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.row_link_item.view.*

/** Adapter for a list of links */
class LinksAdapter(links: List<Link>, private val onClickLink: (url: String) -> Unit)
    : ListAdapter<Link>(links) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_link_item, parent, false))

    inner class ViewHolder(itemView: View) : ListAdapter.ViewHolder<Link>(itemView) {
        override fun onBind(with: Link) {
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
}
