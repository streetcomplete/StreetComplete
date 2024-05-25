package de.westnordost.streetcomplete.util

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import java.util.Locale

data class DummyFeature(
    override val id: String,
    override val name: String,
    override val icon: String?,
    override val tags: Map<String, String>
) : Feature {
    override val geometry = listOf(GeometryType.POINT, GeometryType.AREA)
    override val imageURL = null
    override val names = listOf(name)
    override val terms = emptyList<String>()
    override val includeCountryCodes = emptyList<String>()
    override val excludeCountryCodes = emptyList<String>()
    override val isSearchable = false
    override val matchScore = 1.0f
    override val addTags = tags
    override val removeTags = emptyMap<String, String>()
    override val canonicalNames = emptyList<String>()
    override val canonicalTerms = emptyList<String>()
    override val isSuggestion = false
    override val language: String? = Locale.getDefault().toLanguageTag()
}
