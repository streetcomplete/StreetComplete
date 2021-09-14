package de.westnordost.streetcomplete.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.doOnPreDraw

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.ktx.showTapHint
import kotlinx.android.synthetic.main.side_select_puzzle.view.*
import kotlin.math.*

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

    var onClickSideListener: ((isRight: Boolean) -> Unit)? = null
    set(value) {
        field = value
        if (value == null) {
            leftSideContainer.setOnClickListener(null)
            rightSideContainer.setOnClickListener(null)
            leftSideContainer.isClickable = false
            rightSideContainer.isClickable = false
        } else {
            rotateContainer.isClickable = false
            leftSideContainer.setOnClickListener { value.invoke(false) }
            rightSideContainer.setOnClickListener { value.invoke(true) }
        }
    }

    var onClickListener: (() -> Unit)? = null
    set(value) {
        field = value
        if (value == null) {
            rotateContainer.setOnClickListener(null)
            rotateContainer.isClickable = false
        } else {
            leftSideContainer.isClickable = false
            rightSideContainer.isClickable = false
            rotateContainer.setOnClickListener { value.invoke() }
        }
    }

    private var leftImage: Image? = null
    private var rightImage: Image? = null
    private var isLeftImageSet: Boolean = false
    private var isRightImageSet: Boolean = false
    private var onlyShowingOneSide: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.side_select_puzzle, this, true)

        doOnPreDraw {
            leftSideImage.pivotX = leftSideContainer.width.toFloat()
            rightSideImage.pivotX = 0f
        }

        addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val width = min(bottom - top, right - left)
            val height = max(bottom - top, right - left)
            val params = rotateContainer.layoutParams
            if(width != params.width || height != params.height) {
                params.width = width
                params.height = height
                rotateContainer.layoutParams = params
            }

            val streetWidth = if (onlyShowingOneSide) width else width / 2
            val leftImage = leftImage
            if (!isLeftImageSet && leftImage != null) {
                setStreetDrawable(leftImage, streetWidth, leftSideImage, true)
                isLeftImageSet = true
            }
            val rightImage = rightImage
            if (!isRightImageSet && rightImage != null) {
                setStreetDrawable(rightImage, streetWidth, rightSideImage, false)
                isRightImageSet = true
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        foreground = if (enabled) null else context.getDrawable(R.drawable.background_transparent_grey)
        leftSideContainer.isEnabled = enabled
        rightSideContainer.isEnabled = enabled
    }

    override fun setStreetRotation(rotation: Float) {
        rotateContainer.rotation = rotation
        val scale = abs(cos(rotation * PI / 180)).toFloat()
        rotateContainer.scaleX = 1 + scale * 2 / 3f
        rotateContainer.scaleY = 1 + scale * 2 / 3f
    }

    fun setLeftSideImage(image: Image?) {
        leftImage = image
        replace(image, leftSideImage, true)
    }

    fun setRightSideImage(image: Image?) {
        rightImage = image
        replace(image, rightSideImage, false)
    }

    fun replaceLeftSideImage(image: Image?) {
        leftImage = image
        replaceAnimated(image, leftSideImage, true)
    }

    fun replaceRightSideImage(image: Image?) {
        rightImage = image
        replaceAnimated(image, rightSideImage, false)
    }

    fun setLeftSideText(text: String?) {
        leftSideTextView.setText(text)
    }

    fun setRightSideText(text: String?) {
        rightSideTextView.setText(text)
    }

    fun showLeftSideTapHint() {
        leftSideContainer.showTapHint(300)
    }

    fun showRightSideTapHint() {
        rightSideContainer.showTapHint(1200)
    }

    fun showOnlyRightSide() {
        isRightImageSet = false
        onlyShowingOneSide = true
        val params = RelativeLayout.LayoutParams(0, 0)
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        strut.layoutParams = params
    }

    fun showOnlyLeftSide() {
        isLeftImageSet = false
        onlyShowingOneSide = true
        val params = RelativeLayout.LayoutParams(0, 0)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        strut.layoutParams = params
    }

    fun showBothSides() {
        isRightImageSet = false
        isLeftImageSet = isRightImageSet
        onlyShowingOneSide = false
        val params = RelativeLayout.LayoutParams(0, 0)
        params.addRule(RelativeLayout.CENTER_HORIZONTAL)
        strut.layoutParams = params
    }

    private fun replace(image: Image?, imgView: ImageView, flip180Degrees: Boolean) {
        val width = if (onlyShowingOneSide) rotateContainer.width else rotateContainer.width / 2
        if (width == 0) return
        setStreetDrawable(image, width, imgView, flip180Degrees)
    }

    private fun replaceAnimated(image: Image?, imgView: ImageView, flip180Degrees: Boolean) {
        replace(image, imgView, flip180Degrees)

        (imgView.parent as View).bringToFront()
        imgView.scaleX = 3f
        imgView.scaleY = 3f
        imgView.animate().scaleX(1f).scaleY(1f)
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
        val scale = width.toFloat() / drawable.intrinsicWidth
        m.postScale(scale, scale)
        if (flip180Degrees) m.postRotate(180f)
        val bitmap = Bitmap.createBitmap(
            drawable.bitmap, 0, 0,
            drawable.intrinsicWidth, drawable.intrinsicHeight, m, true
        )
        return BitmapDrawable(resources, bitmap)
    }
}

interface StreetRotateable {
    fun setStreetRotation(rotation: Float)
}
