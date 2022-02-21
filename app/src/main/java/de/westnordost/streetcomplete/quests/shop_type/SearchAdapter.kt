package de.westnordost.streetcomplete.quests.shop_type

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

class SearchAdapter<T>(
    private val context: Context,
    private val filterQuery: (term: String) -> List<T>,
    private val convertToString: (T) -> String
) : BaseAdapter(), Filterable {

    private val filter = SearchFilter()

    private var items: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): T = items[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getFilter() = filter

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        (view as TextView).text = filter.convertResultToString(getItem(position))
        return view
    }

    inner class SearchFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults().also {
            val term = constraint?.toString() ?: ""
            val results = filterQuery(term)
            it.count = results.size
            it.values = results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            items = results.values as List<T>
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as? T?)?.let(convertToString) ?: super.convertResultToString(resultValue)
        }
    }
}
