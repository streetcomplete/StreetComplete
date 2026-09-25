package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.location.CurrentSeason.getCurrentSeason
import de.westnordost.streetcomplete.data.osm.mapdata.Element

// A filter that evalutes based on additional context. The filter is evaluated once, and then cached for performance reasons.
// Current context filters:
// __season__ : matches the current season, in the users' location.

abstract class ContextFilter(val key: String) : ElementFilter {
}

class ContextIs(key: String = "__season__", val value: String) : ContextFilter(key) {
    override fun toString() = "$key = $value"
    override fun matches(el: Element): Boolean {
        return getCurrentSeason.equals(value, ignoreCase = true)
    }
}

class ContextIsNot(key: String = "__season__", val value: String) : ContextFilter(key) {
    override fun toString() = "$key != $value"
    override fun matches(el: Element): Boolean {
        return !getCurrentSeason.equals(value, ignoreCase = true)
    }
}

class ContextLike(key: String = "__season__", val value: String) : ContextFilter(key) {
    private val regex = RegexOrSet.from(value)

    override fun toString() = "$key ~ $value"
    override fun matches(el: Element) = regex.matches(getCurrentSeason)
}

class ContextNotLike(key: String = "__season__", val value: String) : ContextFilter(key) {
    private val regex = RegexOrSet.from(value)

    override fun toString() = "$key !~ $value"
    override fun matches(el: Element) = value != "unknown" && !regex.matches(getCurrentSeason)
}
