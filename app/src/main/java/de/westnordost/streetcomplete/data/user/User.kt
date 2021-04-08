package de.westnordost.streetcomplete.data.user

data class User(val id: Long, val displayName: String) {
    companion object {
        fun from (id: Long?, displayName: String?): User? =
            if (id == null || displayName == null) null
            else User(id, displayName)
    }
}

