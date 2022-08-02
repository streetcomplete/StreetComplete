package de.westnordost.streetcomplete.quests.police_type

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.Item

class AddPoliceTypeForm : AImageListQuestForm<PoliceType, PoliceType>() {

    override val items = listOf(
        Item(PoliceType.CARABINIERI,            R.drawable.ic_italian_police_type_carabinieri, R.string.quest_policeType_type_it_carabinieri),
        Item(PoliceType.POLIZIA_DI_STATO,       R.drawable.ic_italian_police_type_polizia,     R.string.quest_policeType_type_it_polizia_di_stato),
        Item(PoliceType.POLIZIA_MUNICIPALE,     R.drawable.ic_italian_police_type_municipale,  R.string.quest_policeType_type_it_polizia_municipale),
        Item(PoliceType.POLIZIA_LOCALE,         R.drawable.ic_italian_police_type_locale,      R.string.quest_policeType_type_it_polizia_locale),
        Item(PoliceType.GUARDIA_DI_FINANZA,     R.drawable.ic_italian_police_type_finanza,     R.string.quest_policeType_type_it_guardia_di_finanza),
        Item(PoliceType.GUARDIA_COSTIERA,       R.drawable.ic_italian_police_type_costiera,    R.string.quest_policeType_type_it_guardia_costiera)
    )

    override val itemsPerRow = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<PoliceType>) {
        applyAnswer(selectedItems.single())
    }
}
