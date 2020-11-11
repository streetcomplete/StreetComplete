package de.westnordost.streetcomplete.quests.kerb_type

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item

class KerbTypePagerAdapter(private val context: Context, private val valueItems: List<Item<String>>) : PagerAdapter() {

    override fun getPageWidth(position: Int): Float {
        return 1f
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = valueItems[position]
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.kerb_type_pager_item, container, false)

        val title = layout.findViewById<TextView>(R.id.itemTitle)
        val description = layout.findViewById<TextView>(R.id.itemDescription)
        val imageView = layout.findViewById<ImageView>(R.id.itemImageView)
        title.text = context.resources.getText(item.titleId!!)
        description.text = context.resources.getText(item.descriptionId!!)
        imageView.setImageResource(item.drawableId!!)

        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int {
        return valueItems.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }
}
