package de.westnordost.streetcomplete.quests.bench_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial.WOOD
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial.METAL
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial.PLASTIC
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial.CONCRETE
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial.STONE
import de.westnordost.streetcomplete.quests.bench_material.BenchMaterial.BRICK
import de.westnordost.streetcomplete.view.image_select.Item

class AddBenchMaterialForm : AImageListQuestForm<BenchMaterial, BenchMaterial>() {

    override val items = listOf(
        Item(WOOD, R.drawable.bench_wood, R.string.quest_benchMaterial_wood),
        Item(METAL, R.drawable.bench_metal, R.string.quest_benchMaterial_metal),
        Item(PLASTIC, R.drawable.bench_plastic, R.string.quest_benchMaterial_plastic),
        Item(CONCRETE, R.drawable.bench_concrete, R.string.quest_benchMaterial_concrete),
        Item(STONE, R.drawable.bench_stone, R.string.quest_benchMaterial_stone),
        Item(BRICK, R.drawable.bench_brick, R.string.quest_benchMaterial_brick)
    )

    override val otherAnswers by lazy { if (element.tags["amenity"] == "bench")
        listOf(AnswerItem(R.string.quest_bench_answer_picnic_table) { applyAnswer(BenchMaterial.PICNIC) })
    else emptyList() }

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BenchMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
