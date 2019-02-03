package de.westnordost.streetcomplete.view

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import de.westnordost.streetcomplete.R

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.imageView)
    private val textView: TextView? = itemView.findViewById(R.id.textView)
    private val descriptionView: TextView? = itemView.findViewById(R.id.descriptionView)

    var isSelected: Boolean
        get() = itemView.isSelected
        set(value) { itemView.isSelected = value }

    var onClickListener: ((index: Int) -> Unit)? = null
        set(value) {
            field = value
            if (value == null) itemView.setOnClickListener(null)
            else itemView.setOnClickListener {
                val index = adapterPosition
                if (index != RecyclerView.NO_POSITION) value.invoke(index)
            }
        }

    fun bind(item: Item<*>) {
        val drawableId = item.drawableId
        if (drawableId != null)
            imageView.setImageResource(drawableId)
        else
            imageView.setImageDrawable(null)

        val titleId = item.titleId
        if (titleId != null)
            textView?.setText(titleId)
        else
            textView?.text = null

        val descriptionId = item.descriptionId
        descriptionView?.visibility = if (descriptionId != null) View.VISIBLE else View.GONE
        if (descriptionId != null)
            descriptionView?.setText(descriptionId)
        else
            descriptionView?.text = null
    }
}
