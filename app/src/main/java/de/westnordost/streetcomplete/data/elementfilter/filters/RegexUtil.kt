package de.westnordost.streetcomplete.data.elementfilter.filters

/** Either works like a regex if there is a real regex in the string or otherwise as a set if the
 *  regex only consists of a string with pipes, f.e. bakery|pharmacy|clock */
sealed class RegexOrSet {
    abstract fun matches(string: String): Boolean

    companion object {
        private val anyRegexStuffExceptPipe = Regex("[.\\[\\]{}()<>*+-=!?^$]")

        fun from(string: String): RegexOrSet {
            return if (!string.contains(anyRegexStuffExceptPipe)) {
                SetRegex(string.split('|').toSet())
            } else {
                RealRegex(string.toRegex())
            }
        }
    }
}

class RealRegex(private val regex: Regex) : RegexOrSet() {
    override fun matches(string: String) = regex.matches(string)
}

class SetRegex(private val set: Set<String>) : RegexOrSet() {
    override fun matches(string: String) = set.contains(string)
}
