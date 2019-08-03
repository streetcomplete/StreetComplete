package de.westnordost.streetcomplete.data.osm.tql

class TestBooleanExpressionValue(private val value: String) : BooleanExpressionValue {
    override fun matches(ele: Any?) = ele == value
    override fun toString() = value
    fun getValue() = value
}
