package de.westnordost.streetcomplete.util.html

class HtmlParseException(val cursor: Int, message: String?) : Exception(message) {
    override fun toString() = "At $cursor: $message"
}
