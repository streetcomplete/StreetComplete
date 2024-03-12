package de.westnordost.streetcomplete.view

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.widget.TextView
import androidx.core.text.HtmlCompat
import de.westnordost.streetcomplete.util.ktx.spToPx
import org.xml.sax.XMLReader

fun TextView.setHtml(html: String) {
    val spanned = HtmlCompat.fromHtml(
        html,
        HtmlCompat.FROM_HTML_MODE_LEGACY,
        null,
        ::handleList
    )
    text = spanned.replaceUglyBulletSpans(resources.spToPx(2.5f))
    movementMethod = LinkMovementMethod.getInstance()
}

private object Bullet

private fun handleList(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
    if (tag != "li") return

    if (opening) {
        output.setSpan(Bullet, output.length, output.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    } else {
        output.append("\n")
        val lastMark = output.getSpans(0, output.length, Bullet::class.java).lastOrNull() ?: return
        val start = output.getSpanStart(lastMark)
        output.removeSpan(lastMark)
        if (start != output.length) {
            val bullet = BulletSpan()
            output.setSpan(bullet, start, output.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }
}

private fun Spanned.replaceUglyBulletSpans(radius: Float): Spanned {
    val builder = SpannableStringBuilder(this)
    val bullets = builder.getSpans(0, builder.length, BulletSpan::class.java)
    for (bullet in bullets) {
        val start = builder.getSpanStart(bullet)
        val end = builder.getSpanEnd(bullet)
        builder.removeSpan(bullet)
        builder.setSpan(NonUglyBulletSpan(radius), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    return builder
}

class NonUglyBulletSpan(private val radius: Float) : LeadingMarginSpan {

    override fun getLeadingMargin(first: Boolean): Int = (5 * radius).toInt()

    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?,
    ) {
        if ((text as Spanned).getSpanStart(this) != start) return

        val style = paint.style
        paint.style = Paint.Style.FILL

        val xPosition = x + dir * radius
        val yPosition = (top + bottom) / 2f
        canvas.drawCircle(xPosition, yPosition, radius, paint)

        paint.style = style
    }
}
