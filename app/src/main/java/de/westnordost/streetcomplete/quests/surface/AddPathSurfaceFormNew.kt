package de.westnordost.streetcomplete.quests.surface

import `in`.goodiebag.carouselpicker.CarouselPicker
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.viewpager.widget.ViewPager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_surface_new.*

class AddPathSurfaceFormNew : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_surface_new

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        okButton.text = "Next"
        listener?.onHighlightSidewalkSide(questId, questGroup, Listener.SidewalkSide.LEFT)

        val items: MutableList<CarouselPicker.PickerItem> = ArrayList()
        items.add(CarouselPicker.DrawableItem((R.drawable.produce_guava)))
        items.add(CarouselPicker.DrawableItem((R.drawable.produce_guava)))
        items.add(CarouselPicker.DrawableItem((R.drawable.produce_guava)))
        items.add(CarouselPicker.DrawableItem((R.drawable.produce_guava)))
        items.add(CarouselPicker.DrawableItem((R.drawable.produce_guava)))
        val mixAdapter = CarouselPicker.CarouselViewAdapter(context, items, 0)
        carousel.adapter = mixAdapter

        carousel.setCurrentItem(4, false)
        checkIsFormComplete()

        carousel.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // NOP
            }

            override fun onPageSelected(position: Int) {
                // NOP
            }

            override fun onPageScrollStateChanged(state: Int) {
                // NOP
            }
        })
    }

    override fun onClickOk() {
        bottomSheetContainer.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.enter_from_right)
        )
        listener?.onHighlightSidewalkSide(questId, questGroup, Listener.SidewalkSide.RIGHT)
    }

    override fun isFormComplete(): Boolean {
        return true
    }

    override fun isRejectingClose(): Boolean {
        return false
    }
}
