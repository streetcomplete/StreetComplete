package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotatedStringKtTest {
    @Test
    fun `formatAnnotated no args`() {
        assertEquals(
            AnnotatedString(""),
            "".formatAnnotated()
        )
        assertEquals(
            AnnotatedString("hey"),
            "hey".formatAnnotated()
        )
        assertEquals(
            AnnotatedString("hey "),
            "hey %1\$s".formatAnnotated()
        )
    }

    @Test
    fun `formatAnnotated unused args`() {
        assertEquals(
            AnnotatedString("hey"),
            "hey".formatAnnotated("man")
        )
    }

    @Test
    fun `formatAnnotated string arg`() {
        assertEquals(
            AnnotatedString("hey man"),
            "hey %1\$s".formatAnnotated("man")
        )
        assertEquals(
            AnnotatedString("man, hey!"),
            "%1\$s, hey!".formatAnnotated("man")
        )
        assertEquals(
            AnnotatedString("hey man, how is it going?"),
            "hey %1\$s, how is it going?".formatAnnotated("man")
        )
    }

    @Test
    fun `formatAnnotated string args`() {
        assertEquals(
            AnnotatedString("hey Flobby Fasel"),
            "hey %1\$s %2\$s".formatAnnotated("Flobby", "Fasel")
        )
        assertEquals(
            AnnotatedString("hey Fasel Flobby"),
            "hey %2\$s %1\$s".formatAnnotated("Flobby", "Fasel")
        )
    }

    @Test
    fun `formatAnnotated annotated string arg`() {
        assertEquals(
            buildAnnotatedString {
                append("hey ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("man")
                }
            },
            "hey %1\$s".formatAnnotated(AnnotatedString("man", SpanStyle(fontWeight = FontWeight.Bold)))
        )
    }
}
