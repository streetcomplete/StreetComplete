package de.westnordost.streetcomplete.data.meta

interface AssetAccess {
    abstract fun list(basepath: String): Array<out String>?
    abstract fun open(s: String): java.io.InputStream
}
