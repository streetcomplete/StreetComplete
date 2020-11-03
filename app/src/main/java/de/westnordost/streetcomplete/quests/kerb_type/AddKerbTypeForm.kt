package de.westnordost.streetcomplete.quests.kerb_type

import `in`.goodiebag.carouselpicker.CarouselPicker
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item
import kotlinx.android.synthetic.main.quest_kerb_type.*
import kotlinx.android.synthetic.main.quest_surface_new.carousel

class AddKerbTypeForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_kerb_type

    private val valueItems = listOf(
        Item("raised", R.drawable.kerb_raised, R.string.quest_kerb_raised, R.string.quest_kerb_raised_description, null),
        Item("lowered", R.drawable.kerb_lowered, R.string.quest_kerb_lowered, R.string.quest_kerb_lowered_description, null),
        Item("flush", R.drawable.kerb_flush, R.string.quest_kerb_flush, R.string.quest_kerb_flush_description, null),
        Item("rolled", R.drawable.kerb_rolled, R.string.quest_kerb_rolled, R.string.quest_kerb_rolled_description, null))
    private val initialValueIndex = 1

    private val carouselItems = valueItems.map {
        CarouselPicker.DrawableItem((it.drawableId!!))
    }
    private var carouselMoved = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCarousel()
    }

    private fun initCarousel() {
        val adapter = CarouselPicker.CarouselViewAdapter(context, carouselItems, 0)
        carousel.adapter = adapter
        carousel.setCurrentItem(initialValueIndex, false)

        setValueInformation(valueItems[initialValueIndex])
        carousel.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // NOP
            }

            override fun onPageSelected(position: Int) {
                carouselMoved = true
                setValueInformation(valueItems[position])
                checkIsFormComplete()
            }

            override fun onPageScrollStateChanged(state: Int) {
                // NOP
            }
        })

        checkIsFormComplete()
    }

    private fun setValueInformation(item: Item<String>) {
        valueName.text = resources.getText(item.titleId!!)
        valueDescription.text = resources.getText(item.descriptionId!!)
    }

    override fun onClickOk() {
        applyAnswer("test")
    }

    override fun isFormComplete(): Boolean {
        return true
    }

    override fun isRejectingClose(): Boolean {
        return carouselMoved
    }
}
