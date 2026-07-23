package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
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
