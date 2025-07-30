package de.westnordost.streetcomplete.osm.address

/** Structured representation of an `addr:housenumber` string */
data class StructuredHouseNumbers(val list: List<StructuredHouseNumbersPart>) {
    override fun toString() = list.joinToString(",")

    /** Return the previous/next house number, e.g. with [step] = +1, "3c" becomes
     *  "4", "1/7" becomes "2", "1-9" becomes "10" etc.
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
            step > 0 -> allHouseNumbers().max()
            step < 0 -> allHouseNumbers().min()
            else -> null
        }
    }

    private fun allHouseNumbers(): Collection<StructuredHouseNumber> {
        return list.flatMap {
            when (it) {
                is StructuredHouseNumbersPart.Range -> listOf(it.start, it.end)
                is StructuredHouseNumbersPart.Single -> listOf(it.single)
            }
        }
    }
}

//region HouseNumbersPart

sealed interface StructuredHouseNumbersPart : Comparable<StructuredHouseNumbersPart> {
    override fun compareTo(other: StructuredHouseNumbersPart): Int = when (this) {
        is Single -> when (other) {
            // e.g. 12c < 15
            is Single -> sign(
                single.compareTo(other.single)
            )
            // e.g. 10 < 12-14, but 10 ≮ 8-12 (and also not bigger than)
            is Range -> sign(
                single.compareTo(other.start),
                single.compareTo(other.end)
            )
        }
        is Range -> when (other) {
            // e.g. 12-14 < 16a, but 12-14 ≮ 13 (and also not bigger than)
            is Single -> sign(
                start.compareTo(other.single),
                end.compareTo(other.single)
            )
            // e.g. 8-14 < 16-18 but not if the ranges intersect
            is Range -> sign(
                start.compareTo(other.start),
                start.compareTo(other.end),
                end.compareTo(other.start),
                end.compareTo(other.end)
            )
        }
    }

    /** e.g. 5a-12, 100-20/1 */
    data class Range(
        val start: StructuredHouseNumber,
        val end: StructuredHouseNumber
    ) : StructuredHouseNumbersPart {
        override fun toString() = "$start-$end"
    }

    /** e.g. 5a */
    data class Single(val single: StructuredHouseNumber) : StructuredHouseNumbersPart {
        override fun toString() = "$single"
    }
}

private fun sign(vararg numbers: Int): Int = when {
    numbers.all { it > 0 } -> 1
    numbers.all { it < 0 } -> -1
    else -> 0
}

//endregion

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
            return if (isUpperOrLowercaseLetter) copy(letter = c.toString()) else null
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
            return if (n >= 1) copy(number2 = n) else null
        }
    }
}

//endregion
