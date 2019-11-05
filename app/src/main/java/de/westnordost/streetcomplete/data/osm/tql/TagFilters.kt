package de.westnordost.streetcomplete.data.osm.tql

typealias Tags = Map<String, String>

interface TagFilter : Matcher<Tags> {
    fun toOverpassQLString(): String
}

class HasKey(val key: String) : TagFilter {
    override fun toOverpassQLString() = key.quote()
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Tags?) = obj?.containsKey(key) ?: false
}

class NotHasKey(val key: String) : TagFilter {
    override fun toOverpassQLString() = "!" + key.quote()
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Tags?) = !(obj?.containsKey(key) ?: true)
}

class HasTag(val key: String, val value: String) : TagFilter {
    override fun toOverpassQLString() = key.quote() + " = " + value.quote()
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Tags?) = obj?.get(key) == value
}

class NotHasTag(val key: String, val value: String) : TagFilter {
    override fun toOverpassQLString() = key.quote() + " != " + value.quote()
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Tags?) = obj?.get(key) != value
}

class HasTagValueLike(val key: String, value: String) : TagFilter {
    val value = value.toRegex()

    override fun toOverpassQLString() = key.quote() + " ~ " + "^(${value.pattern})$".quote()
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Tags?) = obj?.get(key)?.matches(value) ?: false
}

class NotHasTagValueLike(val key: String, value: String) : TagFilter {
    val value = value.toRegex()

    override fun toOverpassQLString() = key.quote() + " !~ " + "^(${value.pattern})$".quote()
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Tags?) = !(obj?.get(key)?.matches(value) ?: false)
}

class HasTagLike(key: String, value: String) : TagFilter {
    val key = key.toRegex()
    val value = value.toRegex()

    override fun toOverpassQLString() =
        "~" + "^(${key.pattern})$".quote() + " ~ " + "^(${value.pattern})$".quote()
    override fun toString() = toOverpassQLString()

    override fun matches(obj: Tags?) =
        obj?.entries?.find { it.key.matches(key) && it.value.matches(value) } != null
}

private val QUOTES_NOT_REQUIRED = "[a-zA-Z_][a-zA-Z0-9_]*|-?[0-9]+".toRegex()

private fun String.quote() =
    if (QUOTES_NOT_REQUIRED.matches(this)) this else "'${this.replace("'", "\'")}'"
