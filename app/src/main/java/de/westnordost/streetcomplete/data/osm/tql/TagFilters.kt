package de.westnordost.streetcomplete.data.osm.tql

typealias Tags = Map<String, String>

interface TagFilter : BooleanExpressionValue<Tags> {
    fun toOverpassQLString(): String
}

class KeyFilter(val key: String, val eq: Boolean) : TagFilter {
	private val op get() = if (eq) "" else "!"

	override fun toOverpassQLString() = op + key.quote()

	override fun matches(obj: Tags?) = (obj?.containsKey(key) ?: false) == eq
}

class KeyValueFilter(val key: String, val value: String, val eq: Boolean) : TagFilter {
	private val op get() = if (eq) " = " else " != "

	override fun toOverpassQLString() = key.quote() + op + value.quote()

	override fun matches(obj: Tags?) = (obj?.get(key) == value) == eq
}

class KeyRegexValueFilter(val key: String, value: String, val eq: Boolean) : TagFilter {
	val value = value.toRegex()
	private val op get() = if (eq) " ~ " else " !~ "

	override fun toOverpassQLString() = key.quote() + op + "^${value.pattern}$".quote()

	override fun matches(obj: Tags?) = (obj?.get(key)?.matches(value) ?: false) == eq
}

class RegexKeyRegexValueFilter(key: String, value: String) : TagFilter {
	val key = key.toRegex()
	val value = value.toRegex()

	override fun toOverpassQLString() =
		"~" + "^${key.pattern}$".quote() + " ~ " + "^${value.pattern}$".quote()

	override fun matches(obj: Tags?) =
		obj?.entries?.find { it.key.matches(key) && it.value.matches(value) } != null
}

private val QUOTES_NOT_REQUIRED = "[a-zA-Z_][a-zA-Z0-9_]*|-?[0-9]+".toRegex()

private fun String.quote() =
	if (QUOTES_NOT_REQUIRED.matches(this)) this else "'${this.replace("'", "\'")}'"
