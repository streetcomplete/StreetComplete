package de.westnordost.streetcomplete.osm.address

/** Structured representation of an `addr:housenumber` string */
data class StructuredHouseNumbers(val list: List<StructuredHouseNumber>) {
    override fun toString() = list.joinToString(",")

    /** Return the previous/next house number, e.g. with [step] = +1, "3c" becomes
     *  "4", "1/7" becomes "2", "2-9" becomes "3" etc.
     *  If no step exists, null is returned. */
    fun step(step: Int): StructuredHouseNumber.Simple? {
        return boundingHouseNumber(step)?.step(step)
    }

    /** Return the previous/next house number with a minor step, e.g. with [step] = +1, "3c" becomes
     *  "3d", "1/7" becomes "1/8" etc.
     *  If no minor step exists, null is returned. */
    fun minorStep(step: Int): StructuredHouseNumber? {
        return boundingHouseNumber(step)?.minorStep(step)
    }

    private fun boundingHouseNumber(step: Int): StructuredHouseNumber? {
        return when {
            step > 0 -> list.max()
            step < 0 -> list.min()
            else -> null
        }
    }
}

//region StructuredHouseNumber

sealed interface StructuredHouseNumber : Comparable<StructuredHouseNumber> {
    val number: Int

    fun step(step: Int): Simple? {
        val n = number + step
        return if (n < 1) null else Simple(n)
    }

    fun minorStep(step: Int): StructuredHouseNumber?

    override fun compareTo(other: StructuredHouseNumber): Int {
        val diffNumber = number - other.number
        if (diffNumber != 0) return diffNumber
        when {
            this is WithLetter && other !is WithLetter -> {
                return +1
            }
            this !is WithLetter && other is WithLetter -> {
                return -1
            }
            this is WithLetter && other is WithLetter -> {
                if (letter < other.letter) {
                    return -1
                } else if (letter > other.letter) {
                    return +1
                }
            }
            this is WithNumber && other !is WithNumber -> {
                return +1
            }
            this !is WithNumber && other is WithNumber -> {
                return -1
            }
            this is WithNumber && other is WithNumber -> {
                val diffNumber2 = number2 - other.number2
                if (diffNumber2 != 0) return diffNumber2
            }
        }
        return 0
    }

    /** e.g. 12 */
    data class Simple(override val number: Int) : StructuredHouseNumber {
        override fun toString() = number.toString()
        override fun minorStep(step: Int): StructuredHouseNumber? = null
    }
    /** e.g. 12c */
    data class WithLetter(
        override val number: Int,
        val separator: String,
        val letter: String
    ) : StructuredHouseNumber {
        override fun toString() = "$number$separator$letter"
        override fun minorStep(step: Int): StructuredHouseNumber? {
            val c = letter[0] + step
            val isUpperOrLowercaseLetter =
                c.category == CharCategory.LOWERCASE_LETTER ||
                c.category == CharCategory.UPPERCASE_LETTER
            if (!isUpperOrLowercaseLetter) {
                return if (step < 0) Simple(number) else null
            }
            return copy(letter = c.toString())
        }
    }
    /** e.g. 12/3 */
    data class WithNumber(
        override val number: Int,
        val separator: String,
        val number2: Int
    ) : StructuredHouseNumber {
        override fun toString() = "$number$separator$number2"
        override fun minorStep(step: Int): StructuredHouseNumber? {
            val n = number2 + step
            if (n < 1) {
                return if (step < 0) Simple(number) else null
            }
            return copy(number2 = n)
        }
    }
}

//endregion
