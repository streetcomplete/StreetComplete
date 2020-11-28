package de.westnordost.streetcomplete.quests.shop_gone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

class SearchAdapter(
    context: Context,
    private val filterQuery: (term: String) -> List<String>,
) : BaseAdapter(), Filterable {

    private val inflater = LayoutInflater.from(context)
    private val filter = SearchFilter()

    private var items: List<String> = emptyList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): String = items[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getFilter() = filter

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        (view as TextView).text = getItem(position)
        return view
    }

    inner class SearchFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults().also {
            val term = constraint?.toString() ?: ""
            val features = filterQuery(term)
            it.count = features.size
            it.values = features
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            items = results.values as List<String>
        }
    }
}
