package de.westnordost.streetcomplete.quests.tree

sealed interface TreeAnswer

class Tree(val name: String, val isSpecies: Boolean, val localName: String?): TreeAnswer {

    // should be equal if name and isSpecies are the same, don't care about localName
    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + name.hashCode()
        hash = 31 * hash + isSpecies.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tree) return false
        if (name != other.name) return false
        if (isSpecies != other.isSpecies) return false
        return true
    }

    fun toDisplayString() =
        if (localName != null) "$name ($localName)"
        else name
}

data object NotTreeButStump : TreeAnswer
