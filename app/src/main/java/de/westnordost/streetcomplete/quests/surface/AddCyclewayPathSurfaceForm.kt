package de.westnordost.streetcomplete.quests.surface

class AddCyclewayPathSurfaceForm : AddPathSurfaceForm() {
    override fun determinePathType(tags: Map<String, String>) = "cycleway"
}
