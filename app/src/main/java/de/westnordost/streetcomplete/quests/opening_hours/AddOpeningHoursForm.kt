package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.Injector

class AddOpeningHoursForm : OpeningHoursForm() {
    init {
        Injector.instance.applicationComponent.inject(this)
    }


    override fun onClickOk() {
        applyAnswer(RegularOpeningHours(openingHoursAdapter.createOpeningMonthsRows()))
    }


    override fun isFormComplete() = openingHoursAdapter.createOpeningMonths().joinToString(";").isNotEmpty()

    companion object {
        private const val OPENING_HOURS_DATA = "oh_data"
        private const val IS_ADD_MONTHS_MODE = "oh_add_months"
    }
}
