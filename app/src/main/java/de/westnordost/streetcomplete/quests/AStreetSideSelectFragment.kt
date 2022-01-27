package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleWithLastAnswerButtonBinding
import de.westnordost.streetcomplete.util.normalizeDegrees
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.absoluteValue

abstract class AStreetSideSelectFragment<I,T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var left: StreetSideDisplayItem<I>? = null
    private var right: StreetSideDisplayItem<I>? = null

    private lateinit var favs: LastPickedValuesStore<LastSelection<I>>
    private val lastSelection get() = favs.get().firstOrNull()

    open val cellLayoutId = R.layout.cell_icon_select_with_label_below

    open val defaultImage get() = ResImage(if (countryInfo.isLeftHandTraffic) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)

    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<StreetSideDisplayItem<I>>

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it.left.value.toString() + "#" + it.right.value.toString() },
            deserialize = { str ->
                val arr = str.split('#')
                LastSelection(
                    items.find { it.value.toString() == arr[0] }!!,
                    items.find { it.value.toString() == arr[1] }!!
                )
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }

        binding.puzzleView.onClickSideListener = { isRight -> showSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        val left = left
        if (left != null) {
            binding.puzzleView.setLeftSideImage(left.image)
            binding.puzzleView.setLeftSideFloatingIcon(left.floatingIcon)
            binding.puzzleView.setLeftSideText(left.title)
        } else {
            binding.puzzleView.setLeftSideImage(defaultImage)
        }
        val right = right
        if (right != null) {
            binding.puzzleView.setRightSideImage(right.image)
            binding.puzzleView.setRightSideFloatingIcon(right.floatingIcon)
            binding.puzzleView.setRightSideText(right.title)
        } else {
            binding.puzzleView.setRightSideImage(defaultImage)
        }

        showTapHint()
        initLastAnswerButton()
        checkIsFormComplete()
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        left = savedInstanceState.getString(LEFT, null)?.let { value ->
            items.find { it.value.toString() == value }
        }
        right = savedInstanceState.getString(RIGHT, null)?.let { value ->
            items.find { it.value.toString() == value }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LEFT,  left?.value?.toString())
        outState.putString(RIGHT, right?.value?.toString())
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    private fun showTapHint() {
        if ((left == null || right == null) && !HAS_SHOWN_TAP_HINT) {
            if (left == null) binding.puzzleView.showLeftSideTapHint()
            if (right == null) binding.puzzleView.showRightSideTapHint()
            HAS_SHOWN_TAP_HINT = true
        }
    }

    /* ---------------------------------- selection dialog -------------------------------------- */

    private fun showSelectionDialog(isRight: Boolean) {
        val ctx = context ?: return

        ImageListPickerDialog(ctx, items.map { it.asItem() }, cellLayoutId, 2) { item ->
            onSelectedSide(items.find { it.value == item.value }!!, isRight)
        }.show()
    }

    private fun onSelectedSide(selection: StreetSideDisplayItem<I>, isRight: Boolean) {
        if (isRight) {
            binding.puzzleView.replaceRightSideImage(selection.image)
            binding.puzzleView.replaceRightSideFloatingIcon(selection.floatingIcon)
            binding.puzzleView.setRightSideText(selection.title)
            right = selection
        } else {
            binding.puzzleView.replaceLeftSideImage(selection.image)
            binding.puzzleView.replaceLeftSideFloatingIcon(selection.floatingIcon)
            binding.puzzleView.setLeftSideText(selection.title)
            left = selection
        }
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()

        lastSelection?.let {
            binding.lastAnswerButton.leftSideImageView.setImage(it.left.icon)
            binding.lastAnswerButton.rightSideImageView.setImage(it.right.icon)
        }

        binding.lastAnswerButton.root.setOnClickListener { applyLastSelection() }
    }

    private fun updateLastAnswerButtonVisibility() {
        binding.lastAnswerButton.root.isGone =
            lastSelection == null || left != null || right != null
    }

    private fun saveLastSelection() {
        val left = left
        val right = right
        if (left != null && right != null) {
            favs.add(
                if (isRoadDisplayedUpsideDown())
                    LastSelection(right, left)
                else
                    LastSelection(left, right)
            )
        }
    }

    private fun applyLastSelection() {
        val lastSelection = lastSelection ?: return
        if (isRoadDisplayedUpsideDown()) {
            onSelectedSide(lastSelection.right, false)
            onSelectedSide(lastSelection.left, true)
        } else {
            onSelectedSide(lastSelection.left, false)
            onSelectedSide(lastSelection.right, true)
        }
    }

    private fun isRoadDisplayedUpsideDown(): Boolean =
        binding.puzzleView.streetRotation.normalizeDegrees(-180f).absoluteValue > 90f

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        onClickOk(left!!.value, right!!.value)
        saveLastSelection()
    }

    abstract fun onClickOk(leftSide: I, rightSide: I)

    override fun isFormComplete() = left != null && right != null

    override fun isRejectingClose() = left != null || right != null

    companion object {
        private const val LEFT = "left"
        private const val RIGHT = "right"

        private var HAS_SHOWN_TAP_HINT = false
    }
}

private data class LastSelection<T>(
    val left: StreetSideDisplayItem<T>,
    val right: StreetSideDisplayItem<T>
)

interface StreetSideDisplayItem<T> {
    val value: T
    val image: Image
    val title: Text?
    val icon: Image
    val floatingIcon: Image?

    fun asItem(): DisplayItem<T>
}

data class StreetSideItem<T>(
    override val value: T,
    @DrawableRes val imageId: Int,
    @StringRes val titleId: Int? = null,
    @DrawableRes val iconId: Int = imageId,
    @DrawableRes val floatingIconId: Int? = null
) : StreetSideDisplayItem<T> {
    override val image: Image get() = ResImage(imageId)
    override val title: Text? get() = titleId?.let { ResText(it) }
    override val icon: Image get() = ResImage(iconId)
    override val floatingIcon: Image? get() = floatingIconId?.let { ResImage(it) }

    override fun asItem() = Item(value, iconId, titleId)
}

data class StreetSideItem2<T>(
    override val value: T,
    override val image: Image,
    override val title: Text? = null,
    override val icon: Image = image,
    override val floatingIcon: Image? = null
) : StreetSideDisplayItem<T> {
    override fun asItem() = Item2(value, icon, title)
}
