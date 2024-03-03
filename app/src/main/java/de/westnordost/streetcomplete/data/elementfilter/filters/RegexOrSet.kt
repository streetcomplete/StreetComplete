package de.westnordost.streetcomplete.data.elementfilter.filters

/** Either works like a regex if there is a real regex in the string or otherwise as a set if the
 *  regex only consists of a string with pipes, e.g. bakery|pharmacy|clock */
sealed class RegexOrSet {
    abstract fun matches(string: String): Boolean

    companion object {
        private val anyRegexStuffExceptPipe = Regex("[.\\[\\]{}()<>*+-=!?^$]")

        fun from(string: String): RegexOrSet =
            if (!string.contains(anyRegexStuffExceptPipe)) {
                val split = string.split('|')
                SetRegex(HashSet<String>(split.size, 0.9f).apply { split.forEach { add(it.intern()) } })
            } else {
                RealRegex(string.toRegex())
            }
    }
}

class RealRegex(private val regex: Regex) : RegexOrSet() {
    override fun matches(string: String) = regex.matches(string)
}

class SetRegex(private val set: Set<String>) : RegexOrSet() {
    override fun matches(string: String) = set.contains(string)
}
