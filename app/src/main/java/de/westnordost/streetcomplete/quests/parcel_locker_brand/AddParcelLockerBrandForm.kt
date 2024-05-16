package de.westnordost.streetcomplete.quests.parcel_locker_brand

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddParcelLockerBrandForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.parcelLockerBrand

    override fun onClickOk() {
        applyAnswer(name!!)
    }
}
