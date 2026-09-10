package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.listSaver
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary

class FeatureSaver(
    private val featureDictionary: FeatureDictionary
) : Saver<Feature?, String> {
    override fun SaverScope.save(value: Feature?): String? = value?.id
    override fun restore(value: String): Feature? =
        featureDictionary.getById(
            id = value,
            languages = getLanguagesForFeatureDictionary()
        )
}

class FeatureListSaver(
    private val featureDictionary: FeatureDictionary
) : Saver<List<Feature>, List<String>> {
    override fun SaverScope.save(value: List<Feature>) = value.map { it.id }
    override fun restore(value: List<String>): List<Feature> {
        val languages = getLanguagesForFeatureDictionary()
        return value.mapNotNull { featureDictionary.getById(id = it, languages = languages) }
    }
}
