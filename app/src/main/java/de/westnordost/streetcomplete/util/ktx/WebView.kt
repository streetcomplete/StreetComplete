package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import android.graphics.Color
import android.webkit.WebView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import de.westnordost.streetcomplete.R
import kotlin.math.roundToInt

fun WebView.setHtmlBody(body: String) {
    val textColor = resources.getHexColor(R.color.text)
    val linkColor = resources.getHexColor(R.color.accent)
    val dividerColor = resources.getHexColor(R.color.divider)

    val textSize = resources.getDimensionInSp(androidx.appcompat.R.dimen.abc_text_size_body_1_material)
    val h2Size = resources.getDimensionInSp(androidx.appcompat.R.dimen.abc_text_size_headline_material)
    val h3Size = resources.getDimensionInSp(androidx.appcompat.R.dimen.abc_text_size_medium_material)
    val h4Size = resources.getDimensionInSp(androidx.appcompat.R.dimen.abc_text_size_subhead_material)

    val verticalMargin = resources.getDimensionInDp(R.dimen.activity_vertical_margin)
    val horizontalMargin = resources.getDimensionInDp(R.dimen.activity_horizontal_margin)

    val html = """
        <html>
            <head>
                <meta name="color-scheme" content="dark light">
                <style>
                    body {
                        margin: ${verticalMargin}px ${horizontalMargin}px;
                        color: $textColor;
                        font-size: ${textSize}px;
                    }

                    :link { color: $linkColor; }

                    h2, h3, h4 { font-family: sans-serif-condensed; }

                    h2 { font-size: ${h2Size}px; }
                    h3 { font-size: ${h3Size}px; }
                    h4 { font-size: ${h4Size}px; }

                    h2:not(:first-child) {
                        border-top: 1px solid $dividerColor;
                        padding-top: 1rem;
                    }

                    @media (prefers-color-scheme: dark) {}
                </style>
            </head>
            <body>$body</body>
        </html>
    """.trimIndent()

    loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    setBackgroundColor(Color.TRANSPARENT)
}

private fun Resources.getHexColor(@ColorRes resId: Int) =
    String.format("#%06X", 0xffffff and getColor(resId))

private fun Resources.getDimensionInSp(@DimenRes resId: Int) = pxToSp(getDimension(resId)).roundToInt()

private fun Resources.getDimensionInDp(@DimenRes resId: Int) = pxToDp(getDimension(resId)).roundToInt()
