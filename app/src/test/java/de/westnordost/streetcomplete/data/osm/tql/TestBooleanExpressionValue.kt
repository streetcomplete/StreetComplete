package de.westnordost.streetcomplete.data.osm.tql

class TestBooleanExpressionValue(private val value: String) : Matcher<String> {
    override fun matches(obj: String?) = obj == value
    override fun toString() = value
}
