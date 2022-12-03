package de.westnordost.streetcomplete.view

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import de.westnordost.streetcomplete.util.math.normalizeDegrees

data class Transforms(
    val translationX: Float,
    val translationY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val rotation: Float,
    val rotationX: Float,
    val rotationY: Float
) {
    constructor(view: View) : this(view.translationX, view.translationY, view.scaleX, view.scaleY, view.rotation, view.rotationX, view.rotationY)

    companion object {
        val IDENTITY = Transforms(0f, 0f, 1f, 1f, 0f, 0f, 0f)
    }
}

fun View.getTransformationsTo(other: View): Transforms? {
    val otherRect = Rect()
    other.getHitRect(otherRect)
    val commonParent = other.rootView as ViewGroup
    if (rootView != commonParent) return null
    commonParent.offsetDescendantRectToMyCoords(other, otherRect)
    commonParent.offsetRectIntoDescendantCoords(this, otherRect)
    val rect = Rect()
    getHitRect(rect)

    val scaleX = otherRect.width().toFloat() / rect.width().toFloat()
    val scaleY = otherRect.height().toFloat() / rect.height().toFloat()
    val posX = otherRect.left - (1f - otherRect.width().toFloat() / rect.width().toFloat()) * pivotX
    val posY = otherRect.top - (1f - otherRect.height().toFloat() / rect.height().toFloat()) * pivotY
    val rotation = normalizeDegrees(other.globalRotation - globalRotation, -180f)
    val rotationX = normalizeDegrees(other.globalRotationX - globalRotationX, -180f)
    val rotationY = normalizeDegrees(other.globalRotationY - globalRotationY, -180f)

    return Transforms(posX, posY, scaleX, scaleY, rotation, rotationX, rotationY)
}

val View.globalRotation: Float get() =
    rotation + parents.sumOf { it.rotation.toDouble() }.toFloat()

val View.globalRotationX: Float get() =
    rotationX + parents.sumOf { it.rotationX.toDouble() }.toFloat()

val View.globalRotationY: Float get() =
    rotationY + parents.sumOf { it.rotationY.toDouble() }.toFloat()

private val View.parents: Iterable<ViewGroup> get() = object : Iterable<ViewGroup> {
    override fun iterator() = iterator<ViewGroup> {
        var p = parent
        while (p != null && p is ViewGroup) {
            yield(p)
            p = p.parent
        }
    }
}

fun View.applyTransforms(transforms: Transforms) {
    translationX = transforms.translationX
    translationY = transforms.translationY
    scaleX = transforms.scaleX
    scaleY = transforms.scaleY
    rotation = transforms.rotation
    rotationX = transforms.rotationX
    rotationY = transforms.rotationY
}

fun ViewPropertyAnimator.transforms(transforms: Transforms): ViewPropertyAnimator {
    translationX(transforms.translationX)
    translationY(transforms.translationY)
    scaleX(transforms.scaleX)
    scaleY(transforms.scaleY)
    rotation(transforms.rotation)
    rotationX(transforms.rotationX)
    rotationY(transforms.rotationY)
    return this
}

fun View.animateFrom(other: View): ViewPropertyAnimator {
    val transforms = getTransformationsTo(other)
    if (transforms == null) {
        alpha = 0f
        return animate().alpha(1f)
    }
    val currentTransforms = Transforms(this)
    applyTransforms(transforms)
    return animate().transforms(currentTransforms)
}

fun View.animateTo(other: View): ViewPropertyAnimator {
    val transforms = getTransformationsTo(other) ?: return animate().alpha(0f)
    return animate().transforms(transforms)
}
