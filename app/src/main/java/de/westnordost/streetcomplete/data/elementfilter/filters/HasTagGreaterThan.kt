package de.westnordost.streetcomplete.data.elementfilter.filters

/** key > value */
class HasTagGreaterThan(key: String, value: Float): CompareTagValue(key, value) {
    override fun toString() = "$key > $value"
    override fun compareTo(tagValue: Float) = tagValue > value
}
