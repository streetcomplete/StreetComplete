package de.westnordost.streetcomplete.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ViewSideSelectPuzzleBinding
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.showTapHint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

/** A very custom view that conceptually shows the left and right side of a street. Both sides
 *  are clickable.<br>
 *  It is possible to set an image for the left and for the right side individually, the image set
 *  is repeated vertically (repeated along the street). Setting a text for each side is also
 *  possible.<br>
 *  The whole displayed street can be rotated and it is possible to only show the right side, for
 *  example for one-way streets. */
class StreetSideSelectPuzzle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), StreetRotateable {

    private val binding: ViewSideSelectPuzzleBinding =
        ViewSideSelectPuzzleBinding.inflate(LayoutInflater.from(context), this)

    var onClickSideListener: ((isRight: Boolean) -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                binding.leftSideContainer.setOnClickListener(null)
                binding.rightSideContainer.setOnClickListener(null)
                binding.leftSideContainer.isClickable = false
                binding.rightSideContainer.isClickable = false
            } else {
                binding.rotateContainer.isClickable = false
                binding.leftSideContainer.setOnClickListener { value.invoke(false) }
                binding.rightSideContainer.setOnClickListener { value.invoke(true) }
            }
        }

    var onClickListener: (() -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                binding.rotateContainer.setOnClickListener(null)
                binding.rotateContainer.isClickable = false
            } else {
                binding.leftSideContainer.isClickable = false
                binding.rightSideContainer.isClickable = false
                binding.rotateContainer.setOnClickListener { value.invoke() }
            }
        }

    private var leftImage: Image? = null
    private var rightImage: Image? = null
    private var isLeftImageSet: Boolean = false
    private var isRightImageSet: Boolean = false
    private var onlyShowingOneSide: Boolean = false

    init {

        doOnPreDraw {
            binding.leftSideImage.pivotX = binding.leftSideContainer.width.toFloat()
            binding.rightSideImage.pivotX = 0f
        }

        addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val width = min(bottom - top, right - left)
            val height = max(bottom - top, right - left)
            val params = binding.rotateContainer.layoutParams
            if (width != params.width || height != params.height) {
                params.width = width
                params.height = height
                binding.rotateContainer.layoutParams = params
            }

            val streetWidth = if (onlyShowingOneSide) width * 2 / 3 else width / 2
            val leftImage = leftImage
            if (!isLeftImageSet && leftImage != null) {
                setStreetDrawable(leftImage, streetWidth, binding.leftSideImage, true)
                isLeftImageSet = true
            }
            val rightImage = rightImage
            if (!isRightImageSet && rightImage != null) {
                setStreetDrawable(rightImage, streetWidth, binding.rightSideImage, false)
                isRightImageSet = true
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        foreground = if (enabled) null else context.getDrawable(R.drawable.background_transparent_grey)
        binding.leftSideContainer.isEnabled = enabled
        binding.rightSideContainer.isEnabled = enabled
    }

    override var streetRotation: Float
        get() = binding.rotateContainer.rotation
        set(value) {
            binding.rotateContainer.rotation = value
            val scale = abs(cos(value * PI / 180)).toFloat()
            binding.rotateContainer.scaleX = 1 + scale * 2 / 3f
            binding.rotateContainer.scaleY = 1 + scale * 2 / 3f
            binding.leftSideFloatingIcon.rotation = -value
            binding.rightSideFloatingIcon.rotation = -value
        }

    fun setLeftSideFloatingIcon(image: Image?) {
        binding.leftSideFloatingIcon.setImage(image)
        binding.leftSideFloatingIcon.isGone = image == null
    }

    fun setRightSideFloatingIcon(image: Image?) {
        binding.rightSideFloatingIcon.setImage(image)
        binding.rightSideFloatingIcon.isGone = image == null
    }

    fun replaceLeftSideFloatingIcon(image: Image?) {
        setLeftSideFloatingIcon(image)
        binding.leftSideFloatingIcon.animateFallDown()
    }

    fun replaceRightSideFloatingIcon(image: Image?) {
        setRightSideFloatingIcon(image)
        binding.rightSideFloatingIcon.animateFallDown()
    }

    fun setLeftSideImage(image: Image?) {
        leftImage = image
        replace(image, binding.leftSideImage, true)
    }

    fun setRightSideImage(image: Image?) {
        rightImage = image
        replace(image, binding.rightSideImage, false)
    }

    fun replaceLeftSideImage(image: Image?) {
        setLeftSideImage(image)
        binding.leftSideImage.animateFallDown()
    }

    fun replaceRightSideImage(image: Image?) {
        setRightSideImage(image)
        binding.rightSideImage.animateFallDown()
    }

    fun setLeftSideText(text: Text?) {
        binding.leftSideTextView.setText(text)
    }

    fun setRightSideText(text: Text?) {
        binding.rightSideTextView.setText(text)
    }

    fun showLeftSideTapHint() {
        if (binding.leftSideContainer.isClickable) {
            binding.leftSideContainer.showTapHint(300)
        }
    }

    fun showRightSideTapHint() {
        if (binding.rightSideContainer.isClickable) {
            binding.rightSideContainer.showTapHint(1200)
        }
    }

    fun showOnlyRightSide() {
        isRightImageSet = false
        onlyShowingOneSide = true
        binding.leftSideContainer.isGone = true
        binding.rightSideContainer.isGone = false
        binding.strut.updateLayoutParams<ConstraintLayout.LayoutParams> { guidePercent = 1 / 3f }
    }

    fun showOnlyLeftSide() {
        isLeftImageSet = false
        onlyShowingOneSide = true
        binding.leftSideContainer.isGone = false
        binding.rightSideContainer.isGone = true
        binding.strut.updateLayoutParams<ConstraintLayout.LayoutParams> { guidePercent = 2 / 3f }
    }

    fun showBothSides() {
        isRightImageSet = false
        isLeftImageSet = false
        onlyShowingOneSide = false
        binding.leftSideContainer.isGone = false
        binding.rightSideContainer.isGone = false
        binding.strut.updateLayoutParams<ConstraintLayout.LayoutParams> { guidePercent = .5f }
    }

    private fun replace(image: Image?, imgView: ImageView, flip180Degrees: Boolean) {
        val width = binding.rotateContainer.width
        val streetWidth = if (onlyShowingOneSide) width * 2 / 3 else width / 2
        if (streetWidth == 0) return
        setStreetDrawable(image, streetWidth, imgView, flip180Degrees)
    }

    private fun setStreetDrawable(image: Image?, width: Int, imageView: ImageView, flip180Degrees: Boolean) {
        if (image == null) {
            imageView.setImageDrawable(null)
        } else {
            val drawable = scaleToWidth(resources.getBitmapDrawable(image), width, flip180Degrees)
            drawable.tileModeY = Shader.TileMode.REPEAT
            imageView.setImageDrawable(drawable)
        }
    }

    private fun scaleToWidth(drawable: BitmapDrawable, width: Int, flip180Degrees: Boolean): BitmapDrawable {
        val m = Matrix()
        val scale = width.toFloat() / drawable.bitmap.width
        m.postScale(scale, scale)
        if (flip180Degrees) m.postRotate(180f)
        val bitmap = Bitmap.createBitmap(
            drawable.bitmap, 0, 0,
            drawable.bitmap.width, drawable.bitmap.height, m, true
        )
        return bitmap.toDrawable(resources)
    }
}

private fun View.animateFallDown() {
    (parent as View).bringToFront()
    scaleX = 3f
    scaleY = 3f
    animate().scaleX(1f).scaleY(1f)
}

interface StreetRotateable {
    var streetRotation: Float
}
