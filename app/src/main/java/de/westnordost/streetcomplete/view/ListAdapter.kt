package de.westnordost.streetcomplete.view

import androidx.recyclerview.widget.RecyclerView
import android.view.View

/** Adapter based on a list */
abstract class ListAdapter<T>(list: List<T> = listOf()) :
    RecyclerView.Adapter<ListAdapter.ViewHolder<T>>() {

    var list: MutableList<T> = list.toMutableList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount() = list.size

    abstract class ViewHolder<U>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBind(with: U)
    }
}
