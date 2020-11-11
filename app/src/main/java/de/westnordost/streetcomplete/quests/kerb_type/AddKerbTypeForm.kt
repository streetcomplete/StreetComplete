package de.westnordost.streetcomplete.quests.kerb_type

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item
import kotlinx.android.synthetic.main.quest_kerb_type.*
import kotlin.math.abs

class AddKerbTypeForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_kerb_type

    private val valueItems = listOf(
        Item("raised", R.drawable.kerb_raised, R.string.quest_kerb_raised, R.string.quest_kerb_raised_description, null),
        Item("lowered", R.drawable.kerb_lowered, R.string.quest_kerb_lowered, R.string.quest_kerb_lowered_description, null),
        Item("flush", R.drawable.kerb_flush, R.string.quest_kerb_flush, R.string.quest_kerb_flush_description, null),
        Item("rolled", R.drawable.kerb_rolled, R.string.quest_kerb_rolled, R.string.quest_kerb_rolled_description, null))
    private val initialValueIndex = 1

    private var carouselMoved = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPager()
        initButtons()
    }

    private fun initPager() {
        pager.adapter = KerbTypePagerAdapter(requireContext(), valueItems)
        pager.setCurrentItem(initialValueIndex, false)

        pager.setPageTransformer(false) { page, position ->
            // Change scale (zoom)
            page.scaleX = 1.0F - 0.33f * abs(position)
            page.scaleY = 1.0F - 0.33f * abs(position)

            // Makes the page zoom from the center
            page.pivotX = page.width / 2.0f
            page.pivotY = page.height / 2.0f
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // NOP
            }

            override fun onPageSelected(position: Int) {
                carouselMoved = true
                checkIsFormComplete()

                if (pager.currentItem >= valueItems.size - 1) {
                    nextButton.visibility = View.INVISIBLE
                } else {
                    nextButton.visibility = View.VISIBLE
                }

                if (pager.currentItem <= 0) {
                    beforeButton.visibility = View.INVISIBLE
                } else {
                    beforeButton.visibility = View.VISIBLE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // NOP
            }
        })
    }


    private fun initButtons() {
        beforeButton.setOnClickListener {
            if (pager.currentItem < 1) {
                return@setOnClickListener
            }
            pager.setCurrentItem(pager.currentItem - 1, true)
        }

        nextButton.setOnClickListener {
            if (pager.currentItem >= valueItems.size - 1) {
                return@setOnClickListener
            }
            pager.setCurrentItem(pager.currentItem + 1, true)
        }
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
