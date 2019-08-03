package de.westnordost.streetcomplete.data.osm.tql

class TestBooleanExpressionValue(val value: String) : BooleanExpressionValue<String> {
    override fun matches(obj: String?) = obj == value
    override fun toString() = value
}
