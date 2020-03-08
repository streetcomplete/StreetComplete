package de.westnordost.streetcomplete.data.tagfilters

class TestBooleanExpressionValue(private val value: String) : Matcher<String> {
    override fun matches(obj: String?) = obj == value
    override fun toString() = value
}
