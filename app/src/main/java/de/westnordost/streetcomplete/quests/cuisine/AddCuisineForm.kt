package de.westnordost.streetcomplete.quests.cuisine

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestCuisineSuggestionBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.mostCommonWithin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddCuisineForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    private val binding by contentViewBinding(QuestCuisineSuggestionBinding::bind)

    val cuisines = mutableListOf<String>()

    val cuisine get() = binding.cuisineInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cuisineInput.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                (lastPickedAnswers + suggestions).distinct()
            )
        )

        binding.cuisineInput.doAfterTextChanged { checkIsFormComplete() }

        binding.addCuisineButton.setOnClickListener {
            if (isFormComplete() && binding.cuisineInput.text.isNotBlank()) {
                cuisines.add(cuisine)
                binding.currentCuisines.text = cuisines.joinToString(";")
                binding.cuisineInput.text.clear()
            }
            viewLifecycleScope.launch {
                delay(20) // delay, because otherwise dropdown disappears immediately
                binding.cuisineInput.showDropDown()
            }
        }
    }

    override fun onClickOk() {
        cuisines.removeAll { it.isBlank() }
        if (cuisines.isNotEmpty()) favs.add(cuisines)
        if (cuisine.isNotBlank()) favs.add(cuisine)
        if (cuisine.isBlank())
            applyAnswer(cuisines.joinToString(";"))
        else
            applyAnswer((cuisines + listOf(cuisine)).joinToString(";"))
    }

    override fun isFormComplete() = (cuisine.isNotBlank() || cuisines.isNotEmpty()) && !cuisine.contains(";") && !cuisines.contains(cuisine)

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it },
            deserialize = { it },
        )
    }

    private lateinit var favs: LastPickedValuesStore<String>

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 10, historyCount = 50, first = 1)
            .toList()
    }

    companion object {
        private val suggestions = cuisineValues.split("\n").mapNotNull {
            if (it.isBlank()) null
            else it.trim()
        }.toTypedArray()
    }
}

const val cuisineValues = """
afghan
african
american
arab
argentinian
armenian
asian
austrian
bagel
bakery
balkan
barbecue
basque
bavarian
bbq
beef
beef_bowl
beef_noodle
belgian
bistro
bolivian
brasserie
brazilian
breakfast
british
brunch
bubble_tea
buffet
burger
buschenschank
cafe
cafetaria
cajun
cake
canteen
cantonese
caribbean
chicken
chili
chimney_cake
chinese
chocolate
churro
coffee
coffee_shop
couscous
crepe
crepes
croatian
cuban
curry
czech
danish
deli
dessert
diner
donut
dumplings
empanada
english
escalope
ethiopian
european
falafel
filipino
fine_dining
fish
fish_and_chips
fondue
french
fried_chicken
fried_food
fries
friture
frozen_yogurt
georgian
german
greek
grill
gyros
hawaiian
heuriger
hot_dog
hotpot
hungarian
ice_cream
indian
indonesian
international
irish
italian
italian_pizza
jamaican
japanese
juice
kebab
korean
langos
lao
latin_american
lebanese
malagasy
malay
malaysian
meat
mediterranean
mexican
middle_eastern
mongolian
moroccan
nepalese
noodle
noodles
organic
oriental
pakistani
pancake
pasta
pastel
pastry
persian
peruvian
piadina
pie
pita
pizza
poke
polish
portuguese
potato
pretzel
pub
ramen
regional
romanian
russian
salad
sandwich
sausage
savory_pancakes
seafood
shawarma
smoothie
smørrebrød
snack
snackbar
soba
soup
southern
souvlaki
spanish
steak_house
sushi
swedish
swiss
syrian
tacos
taiwanese
takoyaki
tapas
teahouse
teppanyaki
tex-mex
thai
traditional
turkish
udon
ukrainian
uzbek
vietnamese
waffle
western
wings
yakiniku
yakitori
"""
