package de.westnordost.streetcomplete.data.elementfilter

class TestBooleanExpressionValue(private val value: String) : Matcher<String> {
    override fun matches(obj: String?) = obj == value
    override fun toString() = value
}
