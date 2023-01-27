package de.westnordost.streetcomplete.overlays.shops

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import java.util.Locale

data class DummyFeature(
    private val id: String,
    private val name: String,
    private val icon: String,
    private val addTags: Map<String, String>
) : Feature {
    override fun getId() = id
    override fun getTags() = addTags
    override fun getGeometry() = listOf(GeometryType.POINT, GeometryType.AREA)
    override fun getName() = name
    override fun getIcon() = icon
    override fun getImageURL() = null
    override fun getNames() = listOf(name)
    override fun getTerms() = emptyList<String>()
    override fun getIncludeCountryCodes() = emptyList<String>()
    override fun getExcludeCountryCodes() = emptyList<String>()
    override fun isSearchable() = false
    override fun getMatchScore() = 1.0
    override fun getAddTags() = addTags
    override fun getRemoveTags() = emptyMap<String, String>()
    override fun getCanonicalNames() = emptyList<String>()
    override fun getCanonicalTerms() = emptyList<String>()
    override fun isSuggestion() = false
    override fun getLocale(): Locale = Locale.getDefault()
}
