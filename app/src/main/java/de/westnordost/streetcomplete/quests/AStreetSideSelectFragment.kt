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
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.view.image_select.Item2
import de.westnordost.streetcomplete.view.setImage
import kotlin.math.absoluteValue

abstract class AStreetSideSelectFragment<I, T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle_with_last_answer_button
    private val binding by contentViewBinding(QuestStreetSidePuzzleWithLastAnswerButtonBinding::bind)

    override val contentPadding = false

    protected var puzzleView: StreetSideSelectPuzzle? = null

    private var streetSideRotater: StreetSideRotater? = null

    protected var isDefiningBothSides: Boolean = true

    private var left: I? = null
    private var right: I? = null

    private lateinit var favs: LastPickedValuesStore<LastSelection<I>>
    private val lastSelection get() = favs.get().firstOrNull()

    open val cellLayoutId = R.layout.cell_icon_select_with_label_below

    open val defaultImage get() = ResImage(if (countryInfo.isLeftHandTraffic) R.drawable.ic_street_side_unknown_l else R.drawable.ic_street_side_unknown)

    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>

    protected abstract fun getDisplayItem(value: I): StreetSideDisplayItem<I>

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = this::serializeLastSelection,
            deserialize = this::deserializeLastSelection,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView = binding.puzzleView

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }

        binding.puzzleView.onClickSideListener = { isRight -> showSelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        val leftItem = left?.let { getDisplayItem(it) }
        if (leftItem != null) {
            binding.puzzleView.setLeftSideImage(leftItem.image)
            binding.puzzleView.setLeftSideFloatingIcon(leftItem.floatingIcon)
            binding.puzzleView.setLeftSideText(leftItem.title)
        } else {
            binding.puzzleView.setLeftSideImage(defaultImage)
        }
        val rightItem = right?.let { getDisplayItem(it) }
        if (rightItem != null) {
            binding.puzzleView.setRightSideImage(rightItem.image)
            binding.puzzleView.setRightSideFloatingIcon(rightItem.floatingIcon)
            binding.puzzleView.setRightSideText(rightItem.title)
        } else {
            binding.puzzleView.setRightSideImage(defaultImage)
        }

        initStateFromTags()
        showTapHint()
        initLastAnswerButton()
        checkIsFormComplete()
    }

    open fun initStateFromTags() {}

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
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

        ImageListPickerDialog(ctx, items.map { getDisplayItem(it).asItem() }, cellLayoutId, 2) { item ->
            onSelectedSide(items.find { it == item.value }!!, isRight)
        }.show()
    }

    open fun onSelectedSide(selection: I, isRight: Boolean) {
        replaceSide(selection, isRight)
    }

    fun replaceSide(selection: I, isRight: Boolean) {
        val item = getDisplayItem(selection)
        if (isRight) {
            binding.puzzleView.replaceRightSideImage(item.image)
            binding.puzzleView.replaceRightSideFloatingIcon(item.floatingIcon)
            binding.puzzleView.setRightSideText(item.title)
            right = selection
        } else {
            binding.puzzleView.replaceLeftSideImage(item.image)
            binding.puzzleView.replaceLeftSideFloatingIcon(item.floatingIcon)
            binding.puzzleView.setLeftSideText(item.title)
            left = selection
        }
        updateLastAnswerButtonVisibility()
        checkIsFormComplete()
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        left = savedInstanceState.getString(LEFT, null)?.let { getItemByString(it) }
        right = savedInstanceState.getString(RIGHT, null)?.let { getItemByString(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LEFT, left?.toString())
        outState.putString(RIGHT, right?.toString())
    }

    private fun getItemByString(str: String): I? =  items.find { it.toString() == str }

    /* --------------------------------- last answer button ------------------------------------- */

    private fun initLastAnswerButton() {
        updateLastAnswerButtonVisibility()

        lastSelection?.left?.let { binding.lastAnswerButton.leftSideImageView.setImage(getDisplayItem(it).icon) }
        lastSelection?.right?.let { binding.lastAnswerButton.rightSideImageView.setImage(getDisplayItem(it).icon) }

        binding.lastAnswerButton.root.setOnClickListener { applyLastSelection() }
    }

    private fun updateLastAnswerButtonVisibility() {
        binding.lastAnswerButton.root.isGone = lastSelection == null || !isDefiningBothSides
    }

    private fun saveLastSelection() {
        val isUpsideDown = isRoadDisplayedUpsideDown()
        val l = if (isUpsideDown) right else left
        val r = if (isUpsideDown) left else right
        if (shouldSaveSelection(l, r)) {
            favs.add(LastSelection(l, r))
        }
    }

    /** by default only save selection if both sides were filled because cases where only one side
     *  may be filled are usually rare and pre-filling just one side is less of a time-saving */
    open fun shouldSaveSelection(left: I?, right: I?): Boolean =
        left != null && right != null

    private fun applyLastSelection() {
        val lastSelection = lastSelection ?: return
        val isUpsideDown = isRoadDisplayedUpsideDown()
        val l = if (isUpsideDown) lastSelection.right else lastSelection.left
        val r = if (isUpsideDown) lastSelection.left else lastSelection.right
        if (l != null) replaceSide(l, false)
        if (r != null) replaceSide(r, true)
    }

    private fun isRoadDisplayedUpsideDown(): Boolean =
        normalizeDegrees(binding.puzzleView.streetRotation, -180f).absoluteValue > 90f

    private fun serializeLastSelection(selection: LastSelection<I>): String =
        "${selection.left}#${selection.right}"

    private fun deserializeLastSelection(str: String): LastSelection<I> {
        val split = str.split('#')
        val left = getItemByString(split[0])
        val right = getItemByString(split[1])
        return LastSelection(left, right)
    }

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        onClickOk(left, right)
        saveLastSelection()
    }

    abstract fun onClickOk(leftSide: I?, rightSide: I?)

    override fun isFormComplete() = (
        if (isDefiningBothSides) left != null && right != null
        else                     left != null || right != null
    )

    override fun isRejectingClose() = left != null || right != null

    companion object {
        private const val LEFT = "left"
        private const val RIGHT = "right"

        private var HAS_SHOWN_TAP_HINT = false
    }
}

private data class LastSelection<T>(val left: T?, val right: T?)

interface StreetSideDisplayItem<T> {
    val value: T
    /** shown on road view */
    val image: Image
    val title: Text?
    /** shown in "last used" popup and during object selection */
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
