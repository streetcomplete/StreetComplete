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

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.BitmapUtil
import kotlinx.android.synthetic.main.side_select_puzzle.view.*

class StreetSideSelectPuzzle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    var listener: ((isRight:Boolean) -> Unit)? = null
    set(value) {
        field = value
        leftSideContainer.setOnClickListener { listener?.invoke(false) }
        rightSideContainer.setOnClickListener { listener?.invoke(true) }
    }

    private var leftImageResId: Int = 0
    private var rightImageResId: Int = 0
    private var isLeftImageSet: Boolean = false
    private var isRightImageSet: Boolean = false
    private var onlyShowingOneSide: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.side_select_puzzle, this, true)

        addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val width = Math.min(bottom - top, right - left)
            val height = Math.max(bottom - top, right - left)
            val params = rotateContainer.layoutParams
            if(width != params.width || height != params.height) {
                params.width = width
                params.height = height
                rotateContainer.layoutParams = params
            }

            val streetWidth = if (onlyShowingOneSide) width else width / 2
            if (!isLeftImageSet && leftImageResId != 0) {
                setStreetDrawable(leftImageResId, streetWidth, leftSideImage, true)
                isLeftImageSet = true
            }
            if (!isRightImageSet && rightImageResId != 0) {
                setStreetDrawable(rightImageResId, streetWidth, rightSideImage, false)
                isRightImageSet = true
            }
        }
    }

    fun setStreetRotation(rotation: Float) {
        rotateContainer.rotation = rotation
        val scale = Math.abs(Math.cos(rotation * Math.PI / 180)).toFloat()
        rotateContainer.scaleX = 1 + scale * 2 / 3f
        rotateContainer.scaleY = 1 + scale * 2 / 3f
    }

    fun setLeftSideImageResource(resId: Int) {
        leftImageResId = resId
    }

    fun setRightSideImageResource(resId: Int) {
        rightImageResId = resId
    }

    fun replaceLeftSideImageResource(resId: Int) {
        leftImageResId = resId
        replaceAnimated(resId, leftSideImage, true)
    }

    fun replaceRightSideImageResource(resId: Int) {
        rightImageResId = resId
        replaceAnimated(resId, rightSideImage, false)
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

    private fun replaceAnimated(resId: Int, imgView: ImageView, flip180Degrees: Boolean) {
        val width = if (onlyShowingOneSide) rotateContainer.width else rotateContainer.width / 2
        setStreetDrawable(resId, width, imgView, flip180Degrees)

        (imgView.parent as View).bringToFront()

        imgView.scaleX = 3f
        imgView.scaleY = 3f
        imgView.animate().scaleX(1f).scaleY(1f)
    }

    private fun setStreetDrawable(resId: Int, width: Int, imageView: ImageView, flip180Degrees: Boolean) {
        val drawable = scaleToWidth(BitmapUtil.asBitmapDrawable(resources, resId), width, flip180Degrees)
        drawable.tileModeY = Shader.TileMode.REPEAT
        imageView.setImageDrawable(drawable)
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
