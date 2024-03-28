package de.westnordost.streetcomplete.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import de.westnordost.streetcomplete.util.ktx.dpToPx

class ArrayImageAdapter(context: Context, private val items: List<Int>, imageSizeDp: Int) :
    ArrayAdapter<Int>(context, android.R.layout.select_dialog_item, items)
{
    private val params = ViewGroup.LayoutParams(context.resources.dpToPx(imageSizeDp).toInt(), context.resources.dpToPx(imageSizeDp).toInt())
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View { // for non-dropdown
        val view = super.getView(position, convertView, parent)
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = ""
        tv.background = context.getDrawable(items[position])
        tv.layoutParams = params
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = (convertView as? ImageView) ?: ImageView(context)
        v.setImageResource(items[position])
        v.layoutParams = params
        return v
    }
}
